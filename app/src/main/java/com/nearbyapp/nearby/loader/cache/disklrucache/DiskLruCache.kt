package com.nearbyapp.nearby.loader.cache.disklrucache

import com.nearbyapp.nearby.loader.cache.disklrucache.DiskLruCache.Editor
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import java.util.regex.Pattern

/**
 * A cache that uses a bounded amount of space on a filesystem. Each cache
 * entry has a string key and a fixed number of values. Each key must match
 * the regex **[a-z0-9_-]{1,120}**. Values are byte sequences,
 * accessible as streams or files. Each value must be between `0` and
 * `Integer.MAX_VALUE` bytes in length.
 *
 *
 * The cache stores its data in a directory on the filesystem. This
 * directory must be exclusive to the cache; the cache may delete or overwrite
 * files from its directory. It is an error for multiple processes to use the
 * same cache directory at the same time.
 *
 *
 * This cache limits the number of bytes that it will store on the
 * filesystem. When the number of stored bytes exceeds the limit, the cache will
 * remove entries in the background until the limit is satisfied. The limit is
 * not strict: the cache may temporarily exceed it while waiting for files to be
 * deleted. The limit does not include filesystem overhead or the cache
 * journal so space-sensitive applications should set a conservative limit.
 *
 *
 * Clients call [.edit] to create or update the values of an entry. An
 * entry may have only one editor at one time; if a value is not available to be
 * edited then [.edit] will return null.
 *
 *  * When an entry is being **created** it is necessary to
 * supply a full set of values; the empty value should be used as a
 * placeholder if necessary.
 *  * When an entry is being **edited**, it is not necessary
 * to supply data for every value; values default to their previous
 * value.
 *
 * Every [.edit] call must be matched by a call to [Editor.commit]
 * or [Editor.abort]. Committing is atomic: a read observes the full set
 * of values as they were before or after the commit, but never a mix of values.
 *
 *
 * Clients call [.get] to read a snapshot of an entry. The read will
 * observe the value at the time that [.get] was called. Updates and
 * removals after the call do not impact ongoing reads.
 *
 *
 * This class is tolerant of some I/O errors. If files are missing from the
 * filesystem, the corresponding entries will be dropped from the cache. If
 * an error occurs while writing a cache value, the edit will fail silently.
 * Callers should handle other problems by catching `IOException` and
 * responding appropriately.
 */
@Suppress("unused")
@OptIn(ExperimentalCoroutinesApi::class)
class DiskLruCache(
    val directory: File,
    private val appVersion: Int,
    private val valueCount: Int,
    private val maxSize: Long
) : Closeable {
    private val journalFile: File = File(directory, JOURNAL_FILE)
    private val journalFileTmp: File = File(directory, JOURNAL_FILE_TEMP)
    private val journalFileBackup: File = File(directory, JOURNAL_FILE_BACKUP)
    private var size: Long = 0
    private var journalWriter: Writer? = null
    private val lruEntries = LinkedHashMap<String, Entry?>(0, 0.75f, true)
    private var redundantOpCount = 0

    /**
     * To differentiate between old and current snapshots, each entry is given
     * a sequence number each time an edit is committed. A snapshot is stale if
     * its sequence number is not equal to its entry's sequence number.
     */
    private var nextSequenceNumber: Long = 0

    /** This cache uses a single background thread to evict entries.  */
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private fun launchCleanup() {
        coroutineScope.launch {
            synchronized(this@DiskLruCache) {
                if (journalWriter == null) {
                    return@launch
                }
                runCatching {
                    trimToSize()
                    if (journalRebuildRequired()) {
                        rebuildJournal()
                        redundantOpCount = 0
                    }
                }
            }
        }
    }

    init {
        check(maxSize > 0) { "maxSize <= 0" }
        check(valueCount > 0) { "valueCount <= 0"}
        initialize()
    }

    private fun initialize() {
        val backupFile = File(directory, JOURNAL_FILE_BACKUP)
        if (backupFile.exists()) {
            val journalFile = File(directory, JOURNAL_FILE)

            if (journalFile.exists()) {
                backupFile.delete()
            } else {
                renameTo(backupFile, journalFile, false)
            }
        }

        if (journalFile.exists()) {
            try {
                readJournal()
                processJournal()
                return
            } catch (journalIsCorrupt: IOException) {
                // cache corrupted
                delete()
            }
        }

        // Create a new empty cache.
        directory.mkdirs()
        rebuildJournal()
    }

    @Throws(IOException::class)
    private fun readJournal() {
        val reader = StrictLineReader(FileInputStream(journalFile), Util.US_ASCII)
        try {
            val magic = reader.readLine()
            val version = reader.readLine()
            val appVersionString = reader.readLine()
            val valueCountString = reader.readLine()
            val blank = reader.readLine()
            if (MAGIC != magic
                || VERSION_1 != version
                || appVersion.toString() != appVersionString
                || valueCount.toString() != valueCountString
                || "" != blank
            ) {
                throw IOException(
                    "unexpected journal header: [" + magic + ", " + version + ", "
                            + valueCountString + ", " + blank + "]"
                )
            }
            var lineCount = 0
            while (true) {
                try {
                    readJournalLine(reader.readLine())
                    lineCount++
                } catch (endOfJournal: EOFException) {
                    break
                }
            }
            redundantOpCount = lineCount - lruEntries.size

            // If we ended on a truncated line, rebuild the journal before appending to it.
            if (reader.hasUnterminatedLine()) {
                rebuildJournal()
            } else {
                journalWriter = BufferedWriter(
                    OutputStreamWriter(
                        FileOutputStream(journalFile, true), Util.US_ASCII
                    )
                )
            }
        } finally {
            Util.closeQuietly(reader)
        }
    }

    @Throws(IOException::class)
    private fun readJournalLine(line: String) {
        val firstSpace = line.indexOf(' ')
        if (firstSpace == -1) {
            throw IOException("unexpected journal line: $line")
        }
        val keyBegin = firstSpace + 1
        val secondSpace = line.indexOf(' ', keyBegin)
        val key: String
        if (secondSpace == -1) {
            key = line.substring(keyBegin)
            if (firstSpace == REMOVE.length && line.startsWith(REMOVE)) {
                lruEntries.remove(key)
                return
            }
        } else {
            key = line.substring(keyBegin, secondSpace)
        }
        var entry = lruEntries[key]
        if (entry == null) {
            entry = Entry(key)
            lruEntries[key] = entry
        }
        if ((secondSpace != -1) && (firstSpace == CLEAN.length) && line.startsWith(CLEAN)) {
            val parts = line.substring(secondSpace + 1).split(" ").toTypedArray()
            entry.readable = true
            entry.currentEditor = null
            entry.setLengths(parts)
        } else if ((secondSpace == -1) && (firstSpace == DIRTY.length) && line.startsWith(DIRTY)) {
            entry.currentEditor = Editor(entry)
        } else if ((secondSpace == -1) && (firstSpace == READ.length) && line.startsWith(READ)) {
            lruEntries[key]
        } else {
            throw IOException("unexpected journal line: $line")
        }
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    @Throws(IOException::class)
    private fun processJournal() {
        deleteIfExists(journalFileTmp)
        val i = lruEntries.values.iterator()
        while (i.hasNext()) {
            val entry = i.next()
            if (entry!!.currentEditor == null) {
                for (t in 0 until valueCount) {
                    size += entry.lengths[t]
                }
            } else {
                entry.currentEditor = null
                for (t in 0 until valueCount) {
                    deleteIfExists(
                        entry.cleanFile(t)
                    )
                    deleteIfExists(
                        entry.dirtyFile(t)
                    )
                }
                i.remove()
            }
        }
    }

    /**
     * Creates a new journal that omits redundant information. This replaces the
     * current journal if it exists.
     */
    @Synchronized
    @Throws(IOException::class)
    private fun rebuildJournal() {
        journalWriter?.close()

        BufferedWriter(
            OutputStreamWriter(FileOutputStream(journalFileTmp), Util.US_ASCII)
        ).use { writer ->
            writer.write(MAGIC)
            writer.write("\n")
            writer.write(VERSION_1)
            writer.write("\n")
            writer.write(appVersion.toString())
            writer.write("\n")
            writer.write(valueCount.toString())
            writer.write("\n")
            writer.write("\n")
            for (entry: Entry? in lruEntries.values) {
                if (entry!!.currentEditor != null) {
                    writer.write(DIRTY + ' ' + entry.key + '\n')
                } else {
                    writer.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n')
                }
            }
        }
        if (journalFile.exists()) {
            renameTo(journalFile, journalFileBackup, true)
        }
        renameTo(journalFileTmp, journalFile, false)
        journalFileBackup.delete()
        journalWriter = BufferedWriter(
            OutputStreamWriter(FileOutputStream(journalFile, true), Util.US_ASCII)
        )
    }

    /**
     * Returns a snapshot of the entry named `key`, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     */
    @Synchronized
    @Throws(IOException::class)
    operator fun get(key: String): Snapshot? {
        checkNotClosed()
        validateKey(key)

        val entry = lruEntries[key] ?: return null
        if (!entry.readable) {
            return null
        }

        val ins = arrayOfNulls<InputStream>(valueCount)
        try {
            for (i in 0 until valueCount) {
                ins[i] = FileInputStream(entry.cleanFile(i))
            }
        } catch (e: FileNotFoundException) {
            var i = 0
            while (i < valueCount) {
                if (ins[i] != null) {
                    Util.closeQuietly(ins[i])
                } else {
                    break
                }
                i++
            }
            return null
        }
        redundantOpCount++
        journalWriter!!.append(READ).append(' ').append(key).append('\n'.toString())

        if (journalRebuildRequired()) {
            launchCleanup()
        }

        return Snapshot(key, entry.sequenceNumber, ins.requireNoNulls(), entry.lengths)
    }

    /**
     * Returns an editor for the entry named `key`, or null if another
     * edit is in progress.
     */
    @Throws(IOException::class)
    fun edit(key: String): Editor? {
        return edit(key, ANY_SEQUENCE_NUMBER)
    }

    @Synchronized
    @Throws(IOException::class)
    private fun edit(key: String, expectedSequenceNumber: Long): Editor? {
        checkNotClosed()
        validateKey(key)

        var entry = lruEntries[key]

        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER
            && ((entry == null || entry.sequenceNumber != expectedSequenceNumber))
        ) {
            return null // Snapshot is stale.
        }

        if (entry == null) {
            entry = Entry(key)
            lruEntries[key] = entry
        } else if (entry.currentEditor != null) {
            return null // Another edit is in progress.
        }

        val editor = Editor(entry)
        entry.currentEditor = editor

        journalWriter!!.write("$DIRTY $key\n")
        journalWriter!!.flush()
        return editor
    }

    /**
     * Returns the number of bytes currently being used to store the values in
     * this cache. This may be greater than the max size if a background
     * deletion is pending.
     */
    @Synchronized
    fun size(): Long {
        return size
    }

    @Synchronized
    @Throws(IOException::class)
    private fun completeEdit(editor: Editor, success: Boolean) {
        val entry = editor.entry
        if (entry.currentEditor != editor) {
            throw IllegalStateException()
        }

        // If this edit is creating the entry for the first time, every index must have a value.
        if (success && !entry.readable) {
            for (i in 0 until valueCount) {
                if (!editor.written!![i]) {
                    editor.abort()
                    throw IllegalStateException("Newly created entry didn't create value for index $i")
                }
                if (!entry.dirtyFile(i).exists()) {
                    editor.abort()
                    return
                }
            }
        }

        for (i in 0 until valueCount) {
            val dirty = entry.dirtyFile(i)
            if (success) {
                if (dirty.exists()) {
                    val clean = entry.cleanFile(i)
                    dirty.renameTo(clean)
                    val oldLength = entry.lengths[i]
                    val newLength = clean.length()
                    entry.lengths[i] = newLength
                    size = size - oldLength + newLength
                }
            } else {
                deleteIfExists(dirty)
            }
        }

        redundantOpCount++
        entry.currentEditor = null

        if (entry.readable or success) {
            entry.readable = true
            journalWriter!!.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n')
            if (success) {
                entry.sequenceNumber = nextSequenceNumber++
            }
        } else {
            lruEntries.remove(entry.key)
            journalWriter!!.write(REMOVE + ' ' + entry.key + '\n')
        }

        journalWriter!!.flush()
        if (size > maxSize || journalRebuildRequired()) {
            launchCleanup()
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal
     * and eliminate at least 2000 ops.
     */
    private fun journalRebuildRequired(): Boolean {
        val redundantOpCompactThreshold = 2000
        return (redundantOpCount >= redundantOpCompactThreshold
                && redundantOpCount >= lruEntries.size)
    }

    /**
     * Drops the entry for `key` if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @return true if an entry was removed.
     */
    @Synchronized
    @Throws(IOException::class)
    fun remove(key: String): Boolean {
        checkNotClosed()
        validateKey(key)
        val entry = lruEntries[key]
        if (entry == null || entry.currentEditor != null) {
            return false
        }
        for (i in 0 until valueCount) {
            val file = entry.cleanFile(i)
            if (file.exists() && !file.delete()) {
                throw IOException("failed to delete $file")
            }
            size -= entry.lengths[i]
            entry.lengths[i] = 0
        }
        redundantOpCount++
        journalWriter!!.append("$REMOVE $key\n")
        lruEntries.remove(key)
        if (journalRebuildRequired()) {
            launchCleanup()
        }
        return true
    }

    /** Returns true if this cache has been closed.  */
    @get:Synchronized
    val isClosed: Boolean
        get() = journalWriter == null

    private fun checkNotClosed() {
        if (journalWriter == null) {
            throw IllegalStateException("cache is closed")
        }
    }

    /** Force buffered operations to the filesystem.  */
    @Synchronized
    @Throws(IOException::class)
    fun flush() {
        checkNotClosed()
        trimToSize()
        journalWriter!!.flush()
    }

    /** Closes this cache. Stored values will remain on the filesystem.  */
    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        if (journalWriter == null) {
            return  // Already closed.
        }
        for (entry: Entry? in ArrayList(lruEntries.values)) {
            if (entry!!.currentEditor != null) {
                entry.currentEditor?.abort()
            }
        }
        trimToSize()
        journalWriter!!.close()
        journalWriter = null
    }

    @Throws(IOException::class)
    private fun trimToSize() {
        while (size > maxSize) {
            val toEvict: Map.Entry<String, Entry?> = lruEntries.entries.iterator().next()
            remove(toEvict.key)
        }
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete
     * all files in the cache directory including files that weren't created by
     * the cache.
     */
    @Throws(IOException::class)
    fun delete() {
        close()
        Util.deleteContents(directory)
    }

    private fun validateKey(key: String) {
        val matcher = LEGAL_KEY_PATTERN.matcher(key)
        if (!matcher.matches()) {
            throw IllegalArgumentException(
                ("keys must match regex "
                        + STRING_KEY_PATTERN + ": \"" + key + "\"")
            )
        }
    }

    @Throws(IOException::class)
    private fun deleteIfExists(file: File) {
        if (file.exists() && !file.delete()) {
            throw IOException()
        }
    }

    @Throws(IOException::class)
    private fun renameTo(from: File, to: File, deleteDestination: Boolean) {
        if (deleteDestination) {
            deleteIfExists(to)
        }
        if (!from.renameTo(to)) {
            throw IOException()
        }
    }

    @Throws(IOException::class)
    private fun inputStreamToString(`in`: InputStream): String {
        return Util.readFully(InputStreamReader(`in`, Util.UTF_8))
    }

    /** A snapshot of the values for an entry.  */
    inner class Snapshot(
        private val key: String,
        private val sequenceNumber: Long,
        private val ins: Array<InputStream>,
        private val lengths: LongArray
    ) :
        Closeable {
        /**
         * Returns an editor for this snapshot's entry, or null if either the
         * entry has changed since this snapshot was created or if another edit
         * is in progress.
         */
        @Throws(IOException::class)
        fun edit(): Editor? {
            return this@DiskLruCache.edit(key, sequenceNumber)
        }

        /** Returns the unbuffered stream with the value for `index`.  */
        fun getInputStream(index: Int): InputStream {
            return ins[index]
        }

        /** Returns the string value for `index`.  */
        @Throws(IOException::class)
        fun getString(index: Int): String {
            return inputStreamToString(getInputStream(index))
        }

        /** Returns the byte length of the value for `index`.  */
        fun getLength(index: Int): Long {
            return lengths[index]
        }

        override fun close() {
            for (`in`: InputStream? in ins) {
                Util.closeQuietly(`in`)
            }
        }
    }

    /** Edits the values for an entry.  */
    inner class Editor(val entry: Entry) {
        var written: BooleanArray? = if ((entry.readable)) null else BooleanArray(valueCount)
        private var hasErrors = false
        private var committed = false

        /**
         * Returns an unbuffered input stream to read the last committed value,
         * or null if no value has been committed.
         */
        fun newInputStream(index: Int): InputStream? {
            synchronized(this@DiskLruCache) {
                if (entry.currentEditor != this) {
                    throw IllegalStateException()
                }
                if (!entry.readable) {
                    return null
                }
                try {
                    return FileInputStream(entry.cleanFile(index))
                } catch (e: FileNotFoundException) {
                    return null
                }
            }
        }

        /**
         * Returns the last committed value as a string, or null if no value
         * has been committed.
         */
        @Throws(IOException::class)
        fun getString(index: Int): String? {
            val `in` = newInputStream(index)
            return if (`in` != null) inputStreamToString(`in`) else null
        }

        /**
         * Returns a new unbuffered output stream to write the value at
         * `index`. If the underlying output stream encounters errors
         * when writing to the filesystem, this edit will be aborted when
         * [.commit] is called. The returned output stream does not throw
         * IOExceptions.
         */
        fun newOutputStream(index: Int): OutputStream {
            if (index < 0 || index >= valueCount) {
                throw IllegalArgumentException(
                    ("Expected index " + index + " to "
                            + "be greater than 0 and less than the maximum value count "
                            + "of " + valueCount)
                )
            }
            synchronized(this@DiskLruCache) {
                if (entry.currentEditor != this) {
                    throw IllegalStateException()
                }
                if (!entry.readable) {
                    written!![index] = true
                }
                val dirtyFile: File = entry.dirtyFile(index)
                val outputStream: FileOutputStream = try {
                    FileOutputStream(dirtyFile)
                } catch (e: FileNotFoundException) {
                    // Attempt to recreate the cache directory.
                    directory.mkdirs()
                    try {
                        FileOutputStream(dirtyFile)
                    } catch (e2: FileNotFoundException) {
                        // We are unable to recover. Silently eat the writes.
                        return NULL_OUTPUT_STREAM
                    }
                }
                return FaultHidingOutputStream(outputStream)
            }
        }

        /** Sets the value at `index` to `value`.  */
        @Throws(IOException::class)
        operator fun set(index: Int, value: String?) {
            var writer: Writer? = null
            try {
                writer = OutputStreamWriter(newOutputStream(index), Util.UTF_8)
                writer.write(value)
            } finally {
                Util.closeQuietly(writer)
            }
        }

        /**
         * Commits this edit so it is visible to readers.  This releases the
         * edit lock so another edit may be started on the same key.
         */
        @Throws(IOException::class)
        fun commit() {
            if (hasErrors) {
                completeEdit(this, false)
                remove(entry.key) // The previous entry is stale.
            } else {
                completeEdit(this, true)
            }
            committed = true
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         */
        @Throws(IOException::class)
        fun abort() {
            completeEdit(this, false)
        }

        fun abortUnlessCommitted() {
            if (!committed) {
                try {
                    abort()
                } catch (ignored: IOException) {
                }
            }
        }

        private inner class FaultHidingOutputStream(out: OutputStream) :
            FilterOutputStream(out) {
            override fun write(oneByte: Int) {
                try {
                    out.write(oneByte)
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun write(buffer: ByteArray, offset: Int, length: Int) {
                try {
                    out.write(buffer, offset, length)
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun close() {
                try {
                    out.close()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun flush() {
                try {
                    out.flush()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }
        }

    }

    inner class Entry(val key: String) {
        /** Lengths of this entry's files.  */
        val lengths: LongArray = LongArray(valueCount)

        /** True if this entry has ever been published.  */
        var readable = false

        /** The ongoing edit or null if this entry is not being edited.  */
        var currentEditor: Editor? = null

        /** The sequence number of the most recently committed edit to this entry.  */
        var sequenceNumber: Long = 0
        fun getLengths(): String {
            val result = StringBuilder()
            for (size: Long in lengths) {
                result.append(' ').append(size)
            }
            return result.toString()
        }

        /** Set lengths using decimal numbers like "10123".  */
        @Throws(IOException::class)
        fun setLengths(strings: Array<String>) {
            if (strings.size != valueCount) {
                throw invalidLengths(strings)
            }
            try {
                for (i in strings.indices) {
                    lengths[i] = strings[i].toLong()
                }
            } catch (e: NumberFormatException) {
                throw invalidLengths(strings)
            }
        }

        @Throws(IOException::class)
        private fun invalidLengths(strings: Array<String>): IOException {
            throw IOException("unexpected journal line: " + Arrays.toString(strings))
        }

        fun cleanFile(i: Int): File {
            return File(directory, "$key.$i")
        }

        fun dirtyFile(i: Int): File {
            return File(directory, "$key.$i.tmp")
        }

    }

    companion object {
        const val JOURNAL_FILE = "journal"
        const val JOURNAL_FILE_TEMP = "journal.tmp"
        const val JOURNAL_FILE_BACKUP = "journal.bkp"
        const val MAGIC = "libcore.io.DiskLruCache"
        const val VERSION_1 = "1"
        const val ANY_SEQUENCE_NUMBER: Long = -1
        const val STRING_KEY_PATTERN = "[a-z0-9_-]{1,120}"
        val LEGAL_KEY_PATTERN: Pattern = Pattern.compile(STRING_KEY_PATTERN)
        private const val CLEAN = "CLEAN"
        private const val DIRTY = "DIRTY"
        private const val REMOVE = "REMOVE"
        private const val READ = "READ"

        private val NULL_OUTPUT_STREAM: OutputStream = object : OutputStream() {
            override fun write(b: Int) { }
        }
    }
}