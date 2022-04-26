package com.nearbyapp.nearby.loader.cache.diskcache

import java.io.*

class DiskCache(private val folder: File, private val maxSize: Long) {

    private var size: Long = 0
    private val entries = LinkedHashMap<String, Entry>(0, 0.75f, true)

    private var journalNUOCount = 0 // not useful operations count
    private lateinit var journalFile: File
    private lateinit var journalWriter: BufferedWriter

    init {
        try {
            openJournalFile()
            readJournalFile()
        } catch (e: IOException) {
            rebuildJournalFile()
        }
    }

    @Synchronized
    fun get(key: String): Snapshot? {
        val realKey = getFormattedKey(key)
        val entry = entries[realKey] ?: return null

        if (entry.readable && entry.editor == null) {
            writeJournalLine(CLEAN_ENTRY, entry)

            return Snapshot(entry)
        }

        return null
    }

    fun contains(key: String): Boolean {
        val realKey = getFormattedKey(key)
        val entry = entries[realKey] ?: return false

        return entry.editor == null
    }

    @Synchronized
    fun edit(key: String) : Editor? {
        val realKey = getFormattedKey(key)

        var entry = entries[realKey]
        if (entry == null) {
            entry = Entry(realKey)
            entries[realKey] = entry
        }

        if (entry.editor == null) {
            writeJournalLine(DIRTY_ENTRY, entry)
            journalNUOCount++
            return Editor(entry)
        }

        return null
    }

    @Synchronized
    private fun commitEdit(editor: Editor, success: Boolean) {
        val entry = editor.entry
        val cleanFile = entry.getCleanFile()
        val dirtyFile = entry.getDirtyFile()

        if (success) {
            renameFile(dirtyFile, cleanFile)
            entry.readable = true
            size += getFileSize(entry.key)
            writeJournalLine(CLEAN_ENTRY, entry)
        } else {
            removeEntry(entry)
        }

        clearLastRecentlyUsed()
    }

    private fun clearLastRecentlyUsed() {
        if (size <= maxSize) {
            return
        }

        for (entry in entries.values) {
            removeEntry(entry)
            if (maxSize > size) {
                break
            }
        }

        if (isJournalRebuildRequired()) {
            rebuildJournalFile()
        }
    }

    @Throws(IOException::class)
    private fun openJournalFile() {
        val file = getOrCreateFile(JOURNAL_FILE_NAME, false)

        journalFile = file
        journalWriter = FileOutputStream(journalFile, true).bufferedWriter()
    }

    @Throws(IOException::class)
    private fun readJournalFile() {
        BufferedReader(FileReader(journalFile)).use { bufferedReader ->
            var errorsPresent = false

            bufferedReader.lines().forEach { line ->
                val type = line.substring(0, line.indexOf(" "))
                val key = line.substring(line.indexOf(" ") + 1)

                if (type == CLEAN_ENTRY && isEntryKeyValid(key)) {
                    entries[key] = Entry(key)
                    size += getFileSize(key)
                } else if (type == DIRTY_ENTRY && isEntryKeyValid(key)) {
                    entries[key] = Entry(key)
                    size -= getFileSize(key)
                    journalNUOCount++
                } else if (type == REMOVE_ENTRY && isEntryKeyValid(key)) {
                    entries.remove(key)
                    journalNUOCount++
                } else {
                    errorsPresent = true
                }
            }

            if (isJournalRebuildRequired() || errorsPresent) {
                rebuildJournalFile()
            }
        }
    }

    private fun isEntryKeyValid(key: String) : Boolean {
        return key.isNotBlank() && key.isNotEmpty()
    }

    private fun isJournalRebuildRequired() : Boolean {
        return journalNUOCount > JOURNAL_NUO_THRESHOLD
    }

    @Synchronized
    private fun rebuildJournalFile() {
        journalWriter.flush()
        journalWriter.close()

        val file = getOrCreateFile(JOURNAL_FILE_NAME, true)
        journalFile = file
        journalWriter = FileOutputStream(journalFile, true).bufferedWriter()

        entries.values.forEach { entry ->
            if (entry.editor == null) {
                writeJournalLine(CLEAN_ENTRY, entry)
            } else if (entry.editor != null) {
                writeJournalLine(DIRTY_ENTRY, entry)
            }
        }
    }

    private fun writeJournalLine(entryState: String, entry: Entry) {
        synchronized(this.journalWriter) {
            journalWriter.append(entryState + " " + entry.key)
            journalWriter.newLine()
            journalWriter.flush()
        }
    }

    private fun removeEntry(entry: Entry) {
        writeJournalLine(REMOVE_ENTRY, entry)
        journalNUOCount++
        entries.remove(entry.key)

        val cleanFile = entry.getCleanFile()
        val dirtyFile = entry.getDirtyFile()
        size -= cleanFile.length()
        cleanFile.delete()
        dirtyFile.delete()
    }

    private fun renameFile(fileFrom: File, fileTo: File) {
        if (fileTo.exists()) {
            fileTo.delete()
        }

        fileFrom.renameTo(fileTo)
    }

    private fun getOrCreateFile(fileName: String, overwriteIfCreate: Boolean) : File {
        val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)

        if (!file.exists()) {
            folder.mkdirs()
            file.createNewFile()
        } else if (overwriteIfCreate) {
            file.delete()
            folder.mkdirs()
            file.createNewFile()
        }

        return file
    }

    private fun getFileSize(fileName: String) : Long {
        val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)
        return if (file.exists()) {
            file.length()
        } else {
            0
        }
    }

    private fun existFile(key: String) : Boolean {
        val file = File(folder.absolutePath + FILE_SEPARATOR + key)
        return file.exists()
    }

    private fun getFormattedKey(key: String): String {
        val formatted = key.replace(FILE_NAME_REGEX.toRegex(), "").lowercase()
        return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
    }

    @Synchronized
    fun clear() {
        for (entry in entries.values) {
            entry.editor?.abort()
            removeEntry(entry)
        }
        rebuildJournalFile()
    }

    @Synchronized
    fun close() {
        for (entry in entries.values) {
            entry.editor?.abort()
        }
        journalWriter.flush()
        journalWriter.close()
    }

    inner class Entry(val key: String) {
        var readable = existFile(key)
        var editor: Editor? = null

        fun getCleanFile() : File {
            return File(folder.absolutePath + FILE_SEPARATOR + key)
        }

        fun getDirtyFile() : File {
            return File(folder.absolutePath + FILE_SEPARATOR + key + FILE_NAME_TMP_TAG)
        }
    }

    inner class Editor(val entry: Entry) {

        var success = true
        private var outputStream: CacheOutputStream? = null

        init {
            entry.editor = this
        }

        fun outputStream(): OutputStream? {
            synchronized(this@DiskCache) {
                if (entry.editor != this) {
                    return null
                }

                val file = entry.getDirtyFile()

                if (file.exists()) {
                    file.delete()
                }
                folder.mkdirs()
                file.createNewFile()

                outputStream = CacheOutputStream(file)
                return outputStream
            }
        }

        fun commit() {
            outputStream?.let {
                it.flush()
                it.close()
                commitEdit(this, !it.hasError)
            } ?: run {
                commitEdit(this, false)
            }

            entry.editor = null
        }

        fun abort() {
            outputStream?.close()
            commitEdit(this, false)
        }

    }

    private class CacheOutputStream(file: File): FileOutputStream(file) {

        var hasError = false

        override fun write(b: ByteArray?) {
            try {
                super.write(b)
            } catch (e: IOException) {
                hasError = true
            }
        }

        override fun flush() {
            try {
                super.flush()
            } catch (e: IOException) {
                hasError = true
            }
        }

        override fun close() {
            try {
                super.close()
            } catch (e: IOException) {
                hasError = true
            }
        }
    }

    inner class Snapshot(entry: Entry) {

        private val inputStream: InputStream

        init {
            inputStream = createFileInputStream(entry)
        }

        fun inputStream(): InputStream {
            return inputStream
        }

        fun close() {
            try {
                inputStream.close()
            } catch (ignored: IOException) {

            }
        }

        private fun createFileInputStream(entry: Entry): FileInputStream {
            val file = File(folder.absolutePath + FILE_SEPARATOR + entry.key)
            return FileInputStream(file)
        }

    }

    companion object {
        const val CLEAN_ENTRY = "CLEAN"
        const val DIRTY_ENTRY = "DIRTY"
        const val REMOVE_ENTRY = "REMOVE"
        const val JOURNAL_FILE_NAME = "journal"
        const val JOURNAL_NUO_THRESHOLD = 2000
        const val FILE_NAME_TMP_TAG = ".tmp"
        const val FILE_SEPARATOR = "/"
        const val FILE_NAME_REGEX = "[^a-zA-Z0-9]"
    }

}