package com.nearbyapp.nearby.loader.cache.disklrucache

import java.io.*
import java.nio.charset.Charset

class StrictLineReader(inputStream: InputStream?, capacity: Int, charset: Charset?) : Closeable {
    private val `in`: InputStream
    private val charset: Charset

    private var buf: ByteArray?
    private var pos = 0
    private var end = 0

    constructor(`in`: InputStream?, charset: Charset?) : this(`in`, 8192, charset)

    @Throws(IOException::class)
    override fun close() {
        synchronized(`in`) {
            if (buf != null) {
                buf = null
                `in`.close()
            }
        }
    }

    @Throws(IOException::class)
    fun readLine(): String {
        synchronized(`in`) {
            if (buf == null) {
                throw IOException("LineReader is closed")
            }

            if (pos >= end) {
                fillBuf()
            }

            for (i in pos until end) {
                if (buf!![i] == LF) {
                    val lineEnd = if (i != pos && buf!![i - 1] == CR) i - 1 else i
                    val res = String(buf!!, pos, lineEnd - pos, charset)
                    pos = i + 1
                    return res
                }
            }

            val out: ByteArrayOutputStream = object : ByteArrayOutputStream(end - pos + 80) {
                override fun toString(): String {
                    val length = if (count > 0 && buf[count - 1] == CR) count - 1 else count
                    return try {
                        String(buf, 0, length, charset)
                    } catch (e: UnsupportedEncodingException) {
                        throw AssertionError(e) // Since we control the charset this will never happen.
                    }
                }
            }
            while (true) {
                out.write(buf!!, pos, end - pos)

                end = -1
                fillBuf()

                for (i in pos until end) {
                    if (buf!![i] == LF) {
                        if (i != pos) {
                            out.write(buf!!, pos, i - pos)
                        }
                        pos = i + 1
                        return out.toString()
                    }
                }
            }
        }
    }

    fun hasUnterminatedLine(): Boolean {
        return end == -1
    }

    @Throws(IOException::class)
    private fun fillBuf() {
        val result = `in`.read(buf, 0, buf!!.size)
        if (result == -1) {
            throw EOFException()
        }
        pos = 0
        end = result
    }

    companion object {
        private const val CR = '\r'.code.toByte()
        private const val LF = '\n'.code.toByte()
    }

    init {
        if (inputStream == null || charset == null) {
            throw NullPointerException()
        }
        require(capacity >= 0) { "capacity <= 0" }
        require(charset == Util.US_ASCII) { "Unsupported encoding" }
        this.`in` = inputStream
        this.charset = charset
        buf = ByteArray(capacity)
    }
}