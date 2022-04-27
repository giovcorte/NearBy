package com.nearbyapp.nearby.loader.cache.disklrucache

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object Util {

    @JvmField
    val US_ASCII: Charset = StandardCharsets.US_ASCII
    @JvmField
    val UTF_8: Charset = StandardCharsets.UTF_8

    @JvmStatic
    @Throws(IOException::class)
    fun readFully(reader: Reader): String {
        return reader.use { reader1 ->
            val writer = StringWriter()
            val buffer = CharArray(1024)
            var count: Int
            while (reader1.read(buffer).also { count = it } != -1) {
                writer.write(buffer, 0, count)
            }
            writer.toString()
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun deleteContents(dir: File) {
        val files = dir.listFiles() ?: throw IOException("not a readable directory: $dir")
        for (file in files) {
            if (file.isDirectory) {
                deleteContents(file)
            }
            if (!file.delete()) {
                throw IOException("failed to delete file: $file")
            }
        }
    }

    @JvmStatic
    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}