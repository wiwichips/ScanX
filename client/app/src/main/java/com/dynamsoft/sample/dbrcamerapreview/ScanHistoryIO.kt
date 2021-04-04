package com.dynamsoft.sample.dbrcamerapreview

import android.content.Context
import android.util.Log
import java.io.*

fun writeScan(barcode: String, context: Context) {
    try {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput("scan_history.txt", Context.MODE_APPEND))
        outputStreamWriter.append(barcode + '\n')
        outputStreamWriter.close()
    } catch (e: IOException) {
        Log.e("Exception", "File write failed: " + e.toString())
    }
}

fun readScans(context: Context): ArrayList<String> {
    val array = ArrayList<String>()
    try {
        val path = context.getFilesDir()
        val letDirectory = File(path, "")
        letDirectory.mkdirs()
        val file = File(letDirectory, "scan_history.txt")
        val bf = FileInputStream(file).bufferedReader()
        var line: String?

        // add each element to an array
        line = bf.readLine()
        if (line != null) array.add(line)
        while (line != null) {
            line = bf.readLine()
            array.add(line)
        }

        array.removeAt(array.size - 1)
    } catch (e: Exception) { }

    return array
}
