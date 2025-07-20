package com.guvnoh.binl.data

import com.guvnoh.binl.R

data class Product(
    val productName: String = "",
    var productPrice: Double = 0.0,
    var productImage: Int = R.drawable.bottle,
    var category: String = ""
)
