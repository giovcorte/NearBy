package com.nearbyapp.nearby.loader.cache.diskcache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DiskCache(val folder: File, private val maxSize: Long) {

    private var size = 0L
    private val entries = LinkedHashMap<String, Entry>(0, 0.75f, true)

    private val coroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

    init {
        folder.listFiles()?.forEach { file ->
            val entry = Entry(file.name)
            entry.readable = true
            entries[file.name] = entry
        }
    }

    fun edit(key: String): Editor? {
        var entry = entries[key]

        if (entry?.editor != null) {
            return null
        }

        if (entry != null && entry.snapshotOpenedCount != 0) {
            return null
        }

        if (entry == null) {
            entry = Entry(key)
            entries[key] = entry
        }
        val editor = Editor(entry)
        entry.editor = editor
        return editor
    }

    fun get(key: String): Snapshot? {
        return entries[key]?.snapshot()
    }

    @Synchronized
    private fun commitEntry(editor: Editor, success: Boolean) {
        val entry = editor.entry

        if (success && !entry.zombie) {
            entry.readable = true

            val oldSize = entry.cleanFile().length()
            val newSize = entry.dirtyFile().length()
            size = size - oldSize + newSize

            rename(entry.dirtyFile(), entry.cleanFile())
        } else {
            entry.dirtyFile().delete()
        }

        entry.editor = null
        if (entry.zombie) {
            removeEntry(entry)
        }

        if (size > maxSize) {
            launchCleanup()
        }
    }

    private fun removeEntry(entry: Entry) {
        if (entry.editor != null || entry.snapshotOpenedCount > 0) {
            entry.zombie = true
            return
        }

        entries.remove(entry.key)
        size -= entry.cleanFile().length()

        entry.cleanFile().delete()
        entry.dirtyFile().delete()
    }

    private fun launchCleanup() {
        coroutineScope.launch {
            synchronized(this@DiskCache) {
                cleanupEntries()
            }
        }
    }

    private fun cleanupEntries() {
        for (entry in entries.values.toTypedArray()) {
            removeEntry(entry)
            if (size < maxSize * 0.8) {
                break
            }
        }
    }

    private fun rename(from: File, to: File) {
        if (to.exists()) {
            to.delete()
        }

        from.renameTo(to)
    }

    inner class Entry(val key: String) {
        var snapshotOpenedCount = 0
        var zombie = false
        var readable = false

        var editor: Editor? = null

        fun snapshot() : Snapshot? {
            if (!readable || zombie) {
                return null
            }
            snapshotOpenedCount++
            return Snapshot(this)
        }

        fun cleanFile(): File {
            return File(folder.absolutePath + File.separator + key)
        }

        fun dirtyFile(): File {
            return File(folder.absolutePath + File.separator + key + FILE_TMP)
        }
    }

    inner class Editor(val entry: Entry) {

        private var closed = false

        fun file(): File {
            val file = entry.dirtyFile()

            if (!file.exists()) {
                folder.mkdirs()
                file.createNewFile()
            }

            return file
        }

        fun abort() {
            complete(false)
        }

        fun commit() {
            complete(true)
        }

        private fun complete(success: Boolean) {
            synchronized(this@DiskCache) {
                if (entry.editor == this && !closed) {
                    commitEntry(this, success)
                }
                closed = true
            }
        }

        fun detach() {
            if (entry.editor == this) {
                entry.zombie = true
            }
        }
    }

    inner class Snapshot(private val entry: Entry) {

        fun file(): File {
            return entry.cleanFile()
        }

        fun close() {
            synchronized(this@DiskCache) {
                entry.snapshotOpenedCount--

                if (entry.zombie && entry.snapshotOpenedCount == 0) {
                    removeEntry(entry)
                }
            }
        }
    }

    companion object {
        const val FILE_TMP = ".tmp"
    }
}