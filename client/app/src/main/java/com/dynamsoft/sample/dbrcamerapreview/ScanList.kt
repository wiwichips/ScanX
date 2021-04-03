package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

/**
 * ugly code. Shawn, please don't make fun of me. I hacked this together very early in the morning...
 */

class ScanList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)

        val listView: ListView = findViewById(R.id.lastscanslistview)
        val jsonResponses: MutableList<String> = ArrayList()
        val url = "http://173.34.40.62:5000/"

        // makes a get request on each item to get its name
        val callback = { response: JSONArray ->
            try {
                for (i in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(i)
                    jsonResponses.add(
                            "Name: " + jsonObject.getString("PRODUCT_TITLE") + "\n" + "Qty: " + jsonObject.getString("QUANTITY_ON_HAND")
                    )
                }

                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, jsonResponses)
                listView.setAdapter(adapter)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        volleyGet(url + "getInventory", callback)
    }

    /**
     * JSON Array requests
     */
    private fun volleyGet(path: String, callback: (res: JSONArray) -> Unit) {
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(JsonArrayRequest(Request.Method.GET, path, null, { res ->
            callback(res)
        }, { error -> error.printStackTrace() }))
    }
}
