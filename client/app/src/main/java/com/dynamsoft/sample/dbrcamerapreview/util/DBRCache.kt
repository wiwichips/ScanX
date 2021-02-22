package com.dynamsoft.sample.dbrcamerapreview.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import org.json.JSONArray
import org.json.JSONObject
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

    private fun put(key: String, value: String, saveTime: Int) {
        put(key, Utils.newStringWithDateInfo(saveTime, value))
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

    fun put(key: String, value: JSONObject) {
        put(key, value.toString())
    }

    fun put(key: String, value: JSONObject, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    fun getAsJSONObject(key: String): JSONObject? {
        val jsonString = getAsString(key)
        return try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun put(key: String, value: JSONArray) {
        put(key, value.toString())
    }

    fun put(key: String, value: JSONArray, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    fun getAsJSONArray(key: String): JSONArray? {
        val jsonString = getAsString(key)
        return try {
            JSONArray(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun put(key: String, value: ByteArray?) {
        val file = mCache.newFile(key)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(value)
        } catch (e: Exception) {
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

    private fun put(key: String, value: ByteArray?, saveTime: Int) {
        put(key, Utils.newByteArrayWithDateInfo(saveTime, value))
    }

    private fun getAsBinary(key: String): ByteArray? {
        var rAFile: RandomAccessFile? = null
        var removeFile = false
        return try {
            val file = mCache.get(key)
            if (!file.exists()) return null
            rAFile = RandomAccessFile(file, "r")
            val byteArray = ByteArray(rAFile.length().toInt())
            rAFile.read(byteArray)
            if (!Utils.isDue(byteArray)) {
                Utils.clearDateInfo(byteArray)
            } else {
                removeFile = true
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            if (rAFile != null) {
                try {
                    rAFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (removeFile) remove(key)
        }
    }

    @JvmOverloads
    fun put(key: String, value: Serializable?, saveTime: Int = -1) {
        val baos = ByteArrayOutputStream()
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(baos)
            oos.writeObject(value)
            val data = baos.toByteArray()
            if (saveTime != -1) {
                put(key, data, saveTime)
            } else {
                put(key, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { oos?.close() } finally {}
        }
    }

    fun getAsObject(key: String): Any? {
        val data = getAsBinary(key)
        if (data != null) {
            var bais: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            return try {
                bais = ByteArrayInputStream(data)
                ois = ObjectInputStream(bais)
                ois.readObject()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    bais?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    ois?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun put(key: String, value: Bitmap) {
        put(key, Utils.bitmap2Bytes(value))
    }

    private fun put(key: String, value: Bitmap, saveTime: Int) {
        put(key, Utils.bitmap2Bytes(value), saveTime)
    }

    fun getAsBitmap(key: String): Bitmap? {
        return getAsBinary(key)?.let { Utils.bytes2Bitmap(it) }
    }

    fun put(key: String, value: Drawable) {
        put(key, Utils.drawable2Bitmap(value))
    }

    fun put(key: String, value: Drawable, saveTime: Int) {
        put(key, Utils.drawable2Bitmap(value), saveTime)
    }

    fun getAsDrawable(key: String): Drawable? {
        return getAsBinary(key)?.let {
            Utils.bytes2Bitmap(it)?.let { bm -> Utils.bitmap2Drawable(bm) }
        }
    }

    fun file(key: String): File? {
        val file = mCache.newFile(key)
        return if (file.exists()) file else null
    }

    fun remove(key: String): Boolean {
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
            if (dateInfo != null && dateInfo.size == 2) {
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

        fun newStringWithDateInfo(second: Int, strInfo: String): String {
            return createDateInfo(second) + strInfo
        }

        fun newByteArrayWithDateInfo(second: Int, data2: ByteArray?): ByteArray {
            val data1 = createDateInfo(second).toByteArray()
            val retdata = ByteArray(data1.size + data2!!.size)
            System.arraycopy(data1, 0, retdata, 0, data1.size)
            System.arraycopy(data2, 0, retdata, data1.size, data2.size)
            return retdata
        }

        fun clearDateInfo(strInfo: String): String? {
            var info: String? = strInfo
            if (info != null && hasDateInfo(info.toByteArray())) {
                info = info.substring(info.indexOf(mSeparator) + 1
                )
            }
            return info
        }

        fun clearDateInfo(data: ByteArray): ByteArray {
            return if (hasDateInfo(data)) {
                copyOfRange(data, indexOf(data, mSeparator) + 1,
                        data.size)
            } else data
        }

        private fun hasDateInfo(data: ByteArray?): Boolean {
            return data != null
                    && data.size > 15 && data[13] == '-'.toByte()
                    && indexOf(data, mSeparator) > 14
        }

        private fun getDateInfoFromDate(data: ByteArray): Array<String>? {
            if (hasDateInfo(data)) {
                val saveDate = String(copyOfRange(data, 0, 13))
                val deleteAfter = String(copyOfRange(data, 14,
                        indexOf(data, mSeparator)))
                return arrayOf(saveDate, deleteAfter)
            }
            return null
        }

        private fun indexOf(data: ByteArray, c: Char): Int {
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

        /*
         * Bitmap → byte[]
         */
        fun bitmap2Bytes(bm: Bitmap): ByteArray {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return baos.toByteArray()
        }

        /*
         * byte[] → Bitmap
         */
        fun bytes2Bitmap(b: ByteArray): Bitmap? {
            return if (b.isEmpty()) null else BitmapFactory.decodeByteArray(b, 0, b.size)
        }

        /*
         * Drawable → Bitmap
         */
        fun drawable2Bitmap(drawable: Drawable): Bitmap {
            val w = drawable.intrinsicWidth
            val h = drawable.intrinsicHeight
            val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            val bitmap = Bitmap.createBitmap(w, h, config)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            return bitmap
        }

        /*
         * Bitmap → Drawable
         */
        fun bitmap2Drawable(bm: Bitmap): Drawable {
            return BitmapDrawable(bm)
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
        private const val TIME_HOUR = 60 * 60
        private const val TIME_DAY = TIME_HOUR * 24
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