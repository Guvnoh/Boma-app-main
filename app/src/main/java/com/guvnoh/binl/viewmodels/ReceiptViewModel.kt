package com.guvnoh.binl.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.formatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReceiptViewModel(application: Application): AndroidViewModel(application) {
    private var _customerName = MutableStateFlow<String>("")
    private var _grandTotal = MutableStateFlow<Int>(0)
    private val _record = MutableStateFlow<MutableList<ReceiptData>>(mutableListOf())
    //private val _record = MutableStateFlow<MutableMap<String,ReceiptData>>(mutableMapOf())

    val record: StateFlow<MutableList<ReceiptData>> = _record.asStateFlow()
    var customerName: StateFlow<String> = _customerName
    var grandTotal: StateFlow<Int> = _grandTotal

    fun getCustomerName(name: String) {
        _customerName.value = name
    }

    fun addReceiptItem(item: ReceiptData){
        val newList:MutableList<ReceiptData> = _record.value.toMutableList()
        newList.add(item)
        _record.value = newList
    }
    fun removeReceiptItem(brand: String){
        val newList:MutableList<ReceiptData> = _record.value.toMutableList()
        newList.removeAll{it.productName == brand}
        _record.value = newList
    }
    fun getTotal(){
        _grandTotal.value = _record.value.sumOf { it.productTotal.toInt() }
    }

    fun clear(){
        val currentGrandTotal = 0
        _grandTotal.value = currentGrandTotal
    }

}