package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


class ScanList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)

        val lastScans: MutableList<String> = ArrayList<String>()

        // populate string arraylist
        lastScans.add("hello")
        lastScans.add("world")
        lastScans.add("today")

        val listView: ListView = findViewById(R.id.lastscanslistview)


        println("volleyGet~~~~~~~~~~~~~~~~~~~~~~ start")
        val url = "http://173.34.40.62:5000/getLastScans"
        val jsonResponses: MutableList<String> = ArrayList()
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->
            println("inside callback function")
            try {
                val jsonArray = response.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val barcode_ID = jsonObject.getString("BARCODE_ID")
                    jsonResponses.add(barcode_ID)
                }

                // count the number of json repsonses

                val adapter = ArrayAdapter<String>(
                        this, android.R.layout.simple_list_item_1, jsonResponses
                )

                listView.setAdapter(adapter)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue.add(jsonObjectRequest)


    }
}