package com.guvnoh.binl.data



data class ReceiptData(
    val productQty: String,
    val productName: String,
    var productTotal: Double,
)

data class ReceiptDisplay(
    val productQty: String,
    val productName: String,
    var productTotal: String,

)

