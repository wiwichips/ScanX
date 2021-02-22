package com.dynamsoft.sample.dbrcamerapreview.util

import android.content.Context
import android.os.Process
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class DBRCache private constructor(cacheDir: File, max_size: Long, max_count: Int) {

    private val mCache: ACacheManager

    fun put(key: String, value: String?) {
        val file = mCache.newFile(key)
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mCache.put(file)
        }
    }

    fun getAsString(key: String): String? {
        val file = mCache[key]
        if (!file.exists()) return null
        var removeFile = false
        var `in`: BufferedReader? = null
        return try {
            `in` = BufferedReader(FileReader(file))
            var readString = ""
            var currentLine: String
            while (`in`.readLine().also { currentLine = it } != null) {
                readString += currentLine
            }
            if (!Utils.isDue(readString)) {
                Utils.clearDateInfo(readString)
            } else {
                removeFile = true
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (removeFile) remove(key)
        }
    }

    private fun remove(key: String): Boolean {
        return mCache.remove(key)
    }

    fun clear() {
        mCache.clear()
    }


    private object Utils {

        private const val mSeparator = ' '

        fun isDue(str: String): Boolean {
            return isDue(str.toByteArray())
        }

        fun isDue(data: ByteArray): Boolean {
            val dateInfo = getDateInfoFromDate(data)
            if (dateInfo.size == 2) {
                var saveTimeStr = dateInfo[0]
                while (saveTimeStr.startsWith("0")) {
                    saveTimeStr = saveTimeStr.substring(1)
                }
                val saveTime = java.lang.Long.valueOf(saveTimeStr)
                val deleteAfter = java.lang.Long.valueOf(dateInfo[1])
                return System.currentTimeMillis() > saveTime + deleteAfter * 1000
            }
            return false
        }

        fun clearDateInfo(strInfo: String): String {
            return if (hasDateInfo(strInfo.toByteArray())) {
                strInfo.substring(strInfo.indexOf(mSeparator) + 1)
            } else {
                strInfo
            }
        }

        private fun hasDateInfo(data: ByteArray?): Boolean {
            return data != null
                    && data.size > 15 && data[13] == '-'.toByte()
                    && indexOf(data) > 14
        }

        private fun getDateInfoFromDate(data: ByteArray): Array<String> {
            if (hasDateInfo(data)) {
                val saveDate = String(copyOfRange(data, 0, 13))
                val deleteAfter = String(copyOfRange(data, 14, indexOf(data)))
                return arrayOf(saveDate, deleteAfter)
            }
            return emptyArray()
        }

        private fun indexOf(data: ByteArray, c: Char = mSeparator): Int {
            for (i in data.indices) {
                if (data[i] == c.toByte()) {
                    return i
                }
            }
            return -1
        }

        private fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
            val newLength = to - from
            require(newLength >= 0) { "$from > $to" }
            val copy = ByteArray(newLength)
            System.arraycopy(original, from, copy, 0,
                    min(original.size - from, newLength))
            return copy
        }

        private fun createDateInfo(second: Int): String {
            var currentTime = System.currentTimeMillis().toString() + ""
            while (currentTime.length < 13) {
                currentTime = "0$currentTime"
            }
            return "$currentTime-$second$mSeparator"
        }
    }


    private inner class ACacheManager constructor(
            private var cacheDir: File,
            private val sizeLimit: Long,
            private val countLimit: Int,
    ) {
        private val cacheSize: AtomicLong = AtomicLong()
        private val cacheCount: AtomicInteger = AtomicInteger()
        private val lastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        private fun calculateCacheSizeAndCacheCount() {
            Thread {
                var size = 0
                var count = 0
                val cachedFiles = cacheDir.listFiles()
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += calculateSize(cachedFile).toInt()
                        count += 1
                        lastUsageDates[cachedFile] = cachedFile.lastModified()
                    }
                    cacheSize.set(size.toLong())
                    cacheCount.set(count)
                }
            }.start()
        }

        fun put(file: File) {
            var curCacheCount = cacheCount.get()
            while (curCacheCount + 1 > countLimit) {
                val freedSize = removeNext()
                cacheSize.addAndGet(-freedSize)
                curCacheCount = cacheCount.addAndGet(-1)
            }
            cacheCount.addAndGet(1)
            val valueSize = calculateSize(file)
            var curCacheSize = cacheSize.get()
            while (curCacheSize + valueSize > sizeLimit) {
                val freedSize = removeNext()
                curCacheSize = cacheSize.addAndGet(-freedSize)
            }
            cacheSize.addAndGet(valueSize)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime
        }

        operator fun get(key: String): File {
            val file = newFile(key)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime
            return file
        }

        fun newFile(key: String): File {
            return File(cacheDir, key.hashCode().toString())
        }

        fun remove(key: String): Boolean {
            val image = get(key)
            return image.delete()
        }

        fun clear() {
            lastUsageDates.clear()
            cacheSize.set(0)
            cacheDir.listFiles().forEach { it?.delete() }
        }

        fun removeNext(): Long {
            if (lastUsageDates.isEmpty()) {
                return 0
            }
            var oldestUsage: Long? = null
            var mostLongUsedFile: File? = null
            val entries: Set<Map.Entry<File, Long>> = lastUsageDates.entries
            synchronized(lastUsageDates) {
                for ((key, lastValueUsage) in entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = key
                        oldestUsage = lastValueUsage
                    } else {
                        if (lastValueUsage < oldestUsage!!) {
                            oldestUsage = lastValueUsage
                            mostLongUsedFile = key
                        }
                    }
                }
            }
            val fileSize = calculateSize(mostLongUsedFile!!)
            if (mostLongUsedFile!!.delete()) {
                lastUsageDates.remove(mostLongUsedFile)
            }
            return fileSize
        }

        fun calculateSize(file: File): Long {
            return file.length()
        }

        init {
            calculateCacheSizeAndCacheCount()
        }
    }

    companion object {

        private const val MAX_SIZE = 1000 * 1000 * 50
        private const val MAX_COUNT = Int.MAX_VALUE
        private val mInstanceMap: MutableMap<String, DBRCache> = HashMap()

        @JvmOverloads
        operator fun get(ctx: Context, cacheName: String? = "DBRCache"): DBRCache {
            val f = File(ctx.cacheDir, cacheName)
            return Companion[f, MAX_SIZE.toLong(), MAX_COUNT]
        }

        operator fun get(ctx: Context, maxSize: Long, max_count: Int): DBRCache {
            val f = File(ctx.cacheDir, "DBRCache")
            return Companion[f, maxSize, max_count]
        }

        @JvmOverloads
        operator fun get(cacheDir: File, maxSize: Long = MAX_SIZE.toLong(), max_count: Int = MAX_COUNT): DBRCache {
            var manager = mInstanceMap[cacheDir.absoluteFile.toString() + myPid()]
            if (manager == null) {
                manager = DBRCache(cacheDir, maxSize, max_count)
                mInstanceMap[cacheDir.absolutePath + myPid()] = manager
            }
            return manager
        }

        private fun myPid(): String {
            return "_" + Process.myPid()
        }
    }

    init {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw RuntimeException("can't make dirs in " + cacheDir.absolutePath)
        }
        mCache = ACacheManager(cacheDir, max_size, max_count)
    }
}