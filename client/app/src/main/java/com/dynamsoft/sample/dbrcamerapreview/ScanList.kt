package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ScanList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)

        val listView: ListView = findViewById(R.id.lastscanslistview)
        val jsonResponses: MutableList<String> = ArrayList()
        val url = "http://173.34.40.62:5000/"
        val callback = { response : JSONArray ->
            try {
                val jsonArray = response

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val barcode_ID = jsonObject.getString("BARCODE_ID")
                    // jsonResponses.add(barcode_ID)
                    volleyGetJsonObject(url + "getinfo?serial=" + barcode_ID) { itemResponse : JSONObject ->
                        jsonResponses.add(
                                "Name: " + itemResponse.getString("PRODUCT_TITLE") + "\n" + "Barcode: " + itemResponse.getString("SERIAL_NUMBER")
                        )
                        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, jsonResponses)
                        listView.setAdapter(adapter)
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        volleyGetJsonArray(url + "getLastScans", callback)
    }

    private fun volleyGetJsonArray(endPoint : String, callback : (response : JSONArray) -> Unit) {
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, endPoint, null, Response.Listener { response ->
            callback(response)
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue.add(jsonObjectRequest)
    }

    private fun volleyGetJsonObject(endPoint : String, callback : (response : JSONObject) -> Unit) {
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, endPoint, null, Response.Listener { response ->
            callback(response)
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue.add(jsonObjectRequest)
    }
}
