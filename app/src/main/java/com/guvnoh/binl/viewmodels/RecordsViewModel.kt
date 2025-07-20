package com.guvnoh.binl.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guvnoh.binl.data.ReceiptData

class RecordsViewModel(): ViewModel(){
    private var _customerName = MutableLiveData<String>("")
    private var _grandTotal = MutableLiveData<Int>(0)
    private val _record = MutableLiveData<MutableList<ReceiptData>>(mutableListOf())

    val record: LiveData<MutableList<ReceiptData>> = _record
    var customerName: LiveData<String> = _customerName
    var grandTotal: LiveData<Int> = _grandTotal


    fun getNewRecord(recordList: MutableList<ReceiptData>, name: String, total: Int){
        this.record
        _record.value = recordList
        _customerName.value = name
        _grandTotal.value = total
    }
}