package com.guvnoh.binl.data

import com.guvnoh.binl.R
import java.lang.StringBuilder
import java.text.DecimalFormat


val formatter = DecimalFormat("#,###")
//formats Doubles & Ints with commas for clarity
//syntax: formatter.format(number)

fun getFormattedTotal(total: Double): String{
    val localTotal: String =
        if (total>0 ){
            "₦ ${formatter.format(total)}"
        } else{
            "₦0.00"
        }
    return localTotal
}

fun halfAndQuarter(num: Double): String{
    //converts 0.5 to display as '½', same for 0.25 (quarter)
    //also converts them when they have integer companions e.g '1½' etc...
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