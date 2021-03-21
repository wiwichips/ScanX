package com.dynamsoft.sample.dbrcamerapreview

data class Item(
        val barcodeId: Int,
        val productName: String,
        val price: Float,
        val quantity: Long,
        val notifyQuantity: Long
)