package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dynamsoft.sample.dbrcamerapreview.entity.Item
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import org.json.JSONObject
import com.github.kittinunf.result.Result

class ScannedItemActivity : AppCompatActivity() {

    private var editing = false
    private var itemExists = false

    private lateinit var barcode: String
    private lateinit var editButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var decrementButton: Button
    private lateinit var incrementButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var notifyQuantityEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var editTexts: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_item)
        barcode = intent.getStringExtra("barcode")
        // save barcode
        writeScan(barcode, this)
        findViewById<TextView>(R.id.barcodeTitle).text = barcode
        setEditTexts()
        setButtons()
        retrieveItemData()
    }

    private fun setEditTexts() {
        nameEditText = findViewById(R.id.itemName)
        priceEditText = findViewById(R.id.itemPrice)
        notifyQuantityEditText = findViewById(R.id.itemNotificationQoH)
        quantityEditText = findViewById(R.id.itemQoH)
        editTexts = arrayOf(nameEditText, priceEditText, notifyQuantityEditText, quantityEditText)
    }

    private fun setButtons() {
        setEditButton()
        setCancelButton()
        setSaveButton()
        setDecrementButton()
        setIncrementButton()
    }

    private fun setEditButton() {
        editButton = findViewById(R.id.editItem)
        editButton.setOnClickListener {
            if (editing) onFinishEdit() else onEdit()
            editing = !editing
        }
    }

    private fun retrieveItemData() {
        val uri = "http://173.34.40.62:5000/getinfo?serial=${barcode}"
        uri.httpGet().responseString { _, _, result ->
            if (result is Result.Success) {
                itemExists = true
                val jsonItem = JSONObject(result.get())
                jsonItem.put("barcodeID", barcode)
                setTextsForExistingItem(Item.fromJson(jsonItem))
            } else println("Could not find item")
        }.join()
    }

    private fun setTextsForExistingItem(item: Item) {
        nameEditText.text = Editable.Factory().newEditable(item.productName)
        priceEditText.text = Editable.Factory().newEditable("%.2f".format(item.price))
        notifyQuantityEditText.text = Editable.Factory().newEditable("%.0f".format(item.notifyQuantity))
        quantityEditText.text = Editable.Factory().newEditable("%d".format(item.quantity))
    }

    private fun onEdit() {
        editButton.text = getString(R.string.finishEditing)
        editTexts.forEach { it.isEnabled = true }
    }

    private fun onFinishEdit() {
        editButton.text = getString(R.string.editItem)
        editTexts.forEach { it.isEnabled = false }
    }

    private fun setCancelButton() {
        cancelButton = findViewById(R.id.cancelManageInventory)
        cancelButton.setOnClickListener { finish() }
    }

    private fun setSaveButton() {
        saveButton = findViewById(R.id.saveManageInventory)
        saveButton.setOnClickListener { onSave() }
    }

    private fun onSave() {
        if (itemExists) updateExistingItem()
        else createNewItem()
    }

    private fun updateExistingItem() {
        val json = itemFromEditTexts().toJsonString()
        "http://173.34.40.62:5000/editItem".httpPut()
                .jsonBody(json)
                .header(mapOf("Content-Length" to json.length, "Content-Type" to "application/json"))
                .responseString { request, response, result ->
                    if (result is Result.Success) {
                        showSimpleDialogue("Success", "Item updated!", true)
                    } else {
                        showSimpleDialogue("Error", "Something went wrong. The item was not updated.")
                    }
                }.join()
    }

    private fun createNewItem() {
        val json = itemFromEditTexts().toJsonString()
        "http://173.34.40.62:5000/createItem".httpPost()
                .jsonBody(json)
                .header(mapOf("Content-Length" to json.length, "Content-Type" to "application/json"))
                .responseString { request, response, result ->
                    if (result is Result.Success) {
                        showSimpleDialogue("Success", "Your new item has been saved!", true)
                    } else {
                        showSimpleDialogue("Error", "Something went wrong. The item was not saved.")
                    }
                }.join()
    }

    private fun showSimpleDialogue(title: String, message: String, finishOnClose: Boolean = false) {
        runOnUiThread {
            AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(R.drawable.baseline_keyboard_backspace_white_18dp)
                    .setNeutralButton("OK") { dialog, p1 -> if (finishOnClose) finish() }
                    .create()
                    .show()
        }
    }

    private fun itemFromEditTexts(): Item {
        return Item(barcode,
                nameEditText.text.toString(),
                priceEditText.text.toString().asNumeric().toFloat(),
                quantityEditText.text.toString().asNumeric().toLong(),
                notifyQuantityEditText.text.toString().asNumeric().toFloat())
    }

    private fun setIncrementButton() {
        incrementButton = findViewById(R.id.incrementQoH)
        incrementButton.setOnClickListener { incrementQuantityEditText() }
    }

    private fun incrementQuantityEditText() {
        quantityEditText.apply {
            text = Editable.Factory().newEditable(text.toString()
                    .asNumeric()
                    .toLong()
                    .plus(1)
                    .coerceAtMost(9999999999999)
                    .toString())
        }
    }

    private fun setDecrementButton() {
        decrementButton = findViewById(R.id.decrementQoH)
        decrementButton.setOnClickListener { decrementQuantityEditText() }
    }

    private fun decrementQuantityEditText() {
        quantityEditText.apply {
            text = Editable.Factory().newEditable(text.toString()
                    .asNumeric()
                    .toLong()
                    .minus(1)
                    .coerceAtLeast(0)
                    .toString())
        }
    }

    private fun String.asNumeric(): String = if (length == 0) "0" else this
}