package com.guvnoh.binl

import java.text.DecimalFormat


val formatter = DecimalFormat("#,###") //only formats doubles

fun halfAndQuarter(num: Double): String{
    val integerPart = (num).toInt()
    return if (num % 1 == 0.5){
        if(integerPart==0){
            "½"
        } else "$integerPart½"
    }else if (num % 1 == 0.25){
        if(integerPart==0){
            "¼"
        } else "$integerPart¼"
    }
    else num.toInt().toString()
}