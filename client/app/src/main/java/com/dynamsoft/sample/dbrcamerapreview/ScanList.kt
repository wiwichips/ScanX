package com.dynamsoft.sample.dbrcamerapreview

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.annotation.RequiresApi
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
    private var scanHistoryList: MutableList<String> = ArrayList<String>()
    private var currentTab = 0

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
                currentTab = tab.getPosition()
                if (currentTab == 0) {
                    displayInventory()
                }

                else if (currentTab == 1) {
                    displayScanHistory()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Search bar
        val editText: EditText = findViewById(R.id.invSearchBar)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if( -1 != s.toString().indexOf("\n") ){
                    val view: View? = getCurrentFocus()
                    if (view != null) {
                        // escape from keyboard
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)

                        // remove last character if it was newline
                        val fullString = s.toString().substring(0, s.length - 1)
                        editText.setText(fullString);
                    }
                }

                else {
                    val fullString = s.toString()
                    val scansLike: List<ScanItem> = searchLike(fullString)
                    val strScansLike: MutableList<String> = ArrayList()
                    for (si in scansLike) {
                        strScansLike.add(si.toString())
                    }
                    setAdapter(strScansLike as ArrayList<String>)
                }
            }
        })

        // open ScannedItemAcitivy on user click on thing
        val lv : ListView =  findViewById(R.id.lastscanslistview)
        lv.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->

            if (currentTab == 0) {
                println(inventoryList[position].serialNumber)
                startActivity(Intent(this@ScanList, ScannedItemActivity::class.java).apply { putExtra("barcode", inventoryList[position].serialNumber) })
            }

            else {
                println(scanHistoryList[position])
                startActivity(Intent(this@ScanList, ScannedItemActivity::class.java).apply { putExtra("barcode", scanHistoryList[position]) })
            }
        })
    }

    private fun displayScanHistory() {
        scanHistoryList.clear()
        scanHistoryList = readScans(this)
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

        inventoryList.clear()

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

    /**
     * Returns an arraylist of scanned items that contain the user's string
     */
    private fun searchLike(needle: String): List<ScanItem> {
        val startsWith: MutableList<ScanItem> = ArrayList<ScanItem>()
        val containsNotStart: MutableList<ScanItem> = ArrayList<ScanItem>()
        for (si in inventoryList) {
            // ignore case when si.length < needle.length
            if (si.startsWith(needle)) {
                startsWith.add(si)
            }

            else if (si.contains(needle)) {
                containsNotStart.add(si)
            }
        }

        return startsWith + containsNotStart
    }

    private fun setAdapter(list: ArrayList<String>) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
        listView.setAdapter(adapter)
    }
}
