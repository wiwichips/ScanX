package com.dynamsoft.sample.dbrcamerapreview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class ScannedItemActivity : AppCompatActivity() {

    private var editing = false

    private lateinit var barcode: String
    private lateinit var editTexts: Map<String, EditText>
    private lateinit var editButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var decrementButton: Button
    private lateinit var incrementButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_item)
        barcode = intent.getStringExtra("barcode")
        findViewById<TextView>(R.id.barcodeTitle).text = barcode
        // TODO: API call to see if item already exists with this barcode ID.
        //  If yes, populate EditTexts with appropriate info from Item object (class was made for this).
        //  If not, start from scratch.
        setEditTexts()
        setEditButton()
        setCancelButton()
        setSaveButton()
        setIncrementButton()
        setDecrementButton()
    }

    private fun setEditTexts() {
        editTexts = mapOf(
                "name" to findViewById(R.id.itemName),
                "price" to findViewById(R.id.itemPrice),
                "notifyQuantity" to findViewById(R.id.itemNotificationQoH),
                "quantity" to findViewById(R.id.itemQoH))
    }

    private fun setEditButton() {
        editButton = findViewById(R.id.editItem)
        editButton.setOnClickListener {
            if (editing) onFinishEdit() else onEdit()
            editing = !editing
        }
    }

    private fun onEdit() {
        editButton.text = getString(R.string.finishEditing)
        editTexts.values.forEach { it.isEnabled = true }
    }

    private fun onFinishEdit() {
        editButton.text = getString(R.string.editItem)
        editTexts.values.forEach { it.isEnabled = false }
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
        // TODO: API call to update DB with contents of the EditTexts and Barcode ID
    }

    private fun setIncrementButton() {
        incrementButton = findViewById(R.id.incrementQoH)
        incrementButton.setOnClickListener { incrementQuantityEditText() }
    }

    private fun incrementQuantityEditText() {
        editTexts["quantity"]?.apply {
            text = Editable.Factory().newEditable(text.toString()
                    .let { if (length() == 0) "0" else it }
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
        editTexts["quantity"]?.apply {
            text = Editable.Factory().newEditable(text.toString()
                    .let { if (length() == 0) "0" else it }
                    .toLong()
                    .minus(1)
                    .coerceAtLeast(0)
                    .toString())
        }
    }
}