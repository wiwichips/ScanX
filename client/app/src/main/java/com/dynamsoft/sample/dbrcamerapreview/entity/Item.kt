package com.dynamsoft.sample.dbrcamerapreview.entity

import org.json.JSONObject

data class Item(
        val barcodeId: String,
        val productName: String,
        val price: Float,
        val quantity: Long,
        val notifyQuantity: Float
) {

    fun toJsonString(): String {
        return "{" +
                "\"barcodeID\": \"$barcodeId\"," +
                "\"name\": \"$productName\"," +
                "\"price\": ${"%.2f".format(price)}," +
                "\"minStock\": ${"%.0f".format(notifyQuantity)}," +
                "\"count\": $quantity" +
                "}"
    }

    companion object {
        fun fromJson(jsonItem: JSONObject): Item {
            return Item(
                    jsonItem["barcodeID"] as String,
                    jsonItem["PRODUCT_TITLE"] as String,
                    (jsonItem["PRICE"] as Double).toFloat(),
                    (jsonItem["QUANTITY_ON_HAND"] as Int).toLong(),
                    (jsonItem["MIN_QUANTITY_BEFORE_NOTIFY"] as Double).toFloat())
        }
    }
}