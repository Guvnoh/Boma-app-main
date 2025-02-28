package com.guvnoh.binl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptData(
    val product_qty: String,
    val product_name: String,
    val product_total: String,
):Parcelable

