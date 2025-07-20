package com.guvnoh.binl.viewmodels

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.ReceiptDisplay
import com.guvnoh.binl.formatter
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
    fun getReceiptRecord(): MutableList<ReceiptDisplay>{
        //collects data for the receipt page from _record
        //returns the full receipt display data as receiptDisplay
        val currentRecord: MutableList<ReceiptData> = _record.value
        val receiptDisplay: MutableList<ReceiptDisplay> = mutableListOf()
        for (i in currentRecord){
            val total: Double = i.productTotal
            val formattedTotal = formatter.format(total)// changes total to string and formats it with commas
            receiptDisplay.add(ReceiptDisplay(
                //this is a data class dedicated just for receipt page display
                // needed because the total had to be a string instead of a double
                //otherwise the ReceiptData data class would have been used directly
                i.productQty,
                i.productName,
                "₦$formattedTotal"
            ))
        }
        return receiptDisplay
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