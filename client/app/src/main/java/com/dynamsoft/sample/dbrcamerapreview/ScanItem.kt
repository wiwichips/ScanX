package com.dynamsoft.sample.dbrcamerapreview

import org.json.JSONObject

class ScanItem () {
    var serialNumber: String = ""
    var productName: String = ""
    var price: String = ""
    var quantity: String = ""

    constructor(serialNumber: String, productName: String, price: String, quantity: String): this() {
        this.serialNumber  =     serialNumber
        this.productName   =     productName
        this.price         =     price
        this.quantity      =     quantity
    }

    constructor (obj: JSONObject): this(){
        this.serialNumber  =  obj.getString("SERIAL_NUMBER")
        this.productName   =  obj.getString("PRODUCT_TITLE")
        this.price         =  obj.getString("PRICE")
        this.quantity      =  obj.getString("QUANTITY_ON_HAND")
    }

    override fun toString (): String {
        return "Name: $productName\nQty: $quantity"
    }

    fun startsWith (needle: String): Boolean {
        if (productName.length >= needle.length) {
            if (productName.toLowerCase().startsWith(needle.toLowerCase())) {
                return true
            }
        }

        if (serialNumber.length >= needle.length) {
            if (serialNumber.toLowerCase().startsWith(needle.toLowerCase())) {
                return true
            }
        }

        return false
    }

    fun contains (needle: String): Boolean {
        if (productName.length >= needle.length) {
            if (productName.contains(needle)) {
                return true
            }
        }

        if (serialNumber.length >= needle.length) {
            if (serialNumber.contains(needle)) {
                return true
            }
        }

        return false
    }


}
