package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.json.JSONArray
import org.json.JSONException

/**
 * ugly code. Shawn, please don't make fun of me. I hacked this together very early in the morning...
 */

class ScanList : AppCompatActivity() {
    private lateinit var listView: ListView
    private var inventoryList: MutableList<ScanItem> = ArrayList<ScanItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)
        listView = findViewById(R.id.lastscanslistview)

        // by defualt, display all the items in the inventory
        displayInventory()

        // set listeners for the tabs
        val tl: TabLayout = findViewById(R.id.tabLayout)
        tl.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabNum = tab.getPosition()
                if (tabNum == 0) {
                    displayInventory()
                }

                else if (tabNum == 1) {
                    displayScanHistory()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun displayScanHistory() {
        val scanHistoryList = readScans(this)
        val scanList : MutableList<String> = ArrayList<String>()
        for (i in 0..scanHistoryList.size - 1) {
            scanList.add(searchBarcode(scanHistoryList[i]).toString())
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scanList)
        listView.setAdapter(adapter)
    }

    private fun displayInventory() {
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
                    inventoryList.add(ScanItem(jsonObject))
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

    /**
     * JSON Object request
     */

    private fun searchBarcode(needle: String): ScanItem {
        for (i in 0..inventoryList.size - 1) {
            if (needle == inventoryList[i].serialNumber)
                return inventoryList[i]
        }
        return ScanItem(
                needle,
                "Item does not exist",
                "0.0",
                "0"
        )
    }
}
