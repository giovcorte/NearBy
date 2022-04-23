package com.nearbyapp.nearby.loader.cache.diskcache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nearbyapp.nearby.loader.cache.IImageCache
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ImageDiskCache(private val folder: File, private val maxSize: Long) : IImageCache {

    private var size: Long = 0
    private val entries = LinkedHashMap<String, Entry>(0, 0.75f, true)

    private val writer = BitmapWriter()

    init {
        loadEntriesFromDisk()
    }

    private fun loadEntriesFromDisk() {
        folder.listFiles()?.forEach {
            entries[it.name] = Entry(it.name, it.length())
            size += it.length()
        }
    }

    inner class Entry(val key: String) {
        var size: Long = 0
        var editing = false

        constructor(key: String, size: Long) : this(key) {
            this.size = size
        }
    }

    inner class BitmapWriter {

        fun write(entry: Entry, bitmap: Bitmap) {
            try {
                val file = createFile(entry.key)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                entry.size = getFileSize(getFileAbsolutePath(entry.key))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun createFile(fileName: String) : File {
            val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)
            folder.mkdirs()
            file.createNewFile()
            return file
        }

    }

    override fun get(key: String): Bitmap? {
        val realKey = getFormattedKey(key)
        val entry = entries[realKey] ?: return null

        if (!entry.editing) {
            return getBitmapFromFileName(entry.key)
        }

        return null
    }

    override fun contains(key: String): Boolean {
        val realKey = getFormattedKey(key)
        val entry = entries[realKey] ?: return false

        return !entry.editing && entry.size > 0
    }

    override fun put(key: String, bitmap: Bitmap) {
        synchronized(writer) {
            val realKey = getFormattedKey(key)

            if (size + getFileSize(bitmap) > maxSize) {
                clearLastRecentlyUsed(getFileSize(bitmap))
            }

            var entry = entries[realKey]
            if (entry == null) {
                entry = Entry(realKey)
                entries[realKey] = entry
            }
            entry.editing = true
            writer.write(entry, bitmap)
            entry.editing = false
        }
    }

    private fun clearLastRecentlyUsed(spaceRequired: Long) {
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next().value
            size -= entry.size
            deleteFile(entry.key)

            if (maxSize - size > spaceRequired) {
                break
            }
        }
        if (maxSize - size < spaceRequired) {
            clear()
        }
    }

    private fun deleteFile(fileName: String) {
        val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)
        file.delete()
    }

    private fun getFileSize(fileName: String) : Long {
        val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)
        return file.length()
    }

    private fun getFileAbsolutePath(fileName: String) : String {
        return folder.absolutePath + FILE_SEPARATOR + fileName
    }

    private fun getBitmapFromFileName(fileName: String) : Bitmap? {
        return try {
            val file = File(folder.absolutePath + FILE_SEPARATOR + fileName)
            val inputStream = FileInputStream(file)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileSize(bitmap: Bitmap): Long {
        return bitmap.rowBytes.toLong() * bitmap.height
    }

    private fun getFormattedKey(key: String): String {
        val formatted = key.replace(FILE_NAME_REGEX.toRegex(), "").lowercase()
        return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
    }

    override fun clear() {
        folder.listFiles()?.forEach {
            it.delete()
        }
    }

    companion object {
        const val FILE_SEPARATOR = "/"
        const val FILE_NAME_REGEX = "[^a-zA-Z0-9]"
    }

}