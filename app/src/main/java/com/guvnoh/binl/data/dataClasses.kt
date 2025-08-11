package com.guvnoh.binl.data



data class ReceiptData(
    val productQty: String = "",
    val productName: String = "",
    var productTotal: Double = 0.0,
    var productTotalString: String = ""
)

data class RecordData(
    val primaryKey: String = "",
    val customerName: String = "",
    val receipt: List<ReceiptData> = emptyList(),
    val grandTotal: String = ""
)

