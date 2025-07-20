package com.guvnoh.binl.pages

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guvnoh.binl.databinding.ReceiptCardDesignBinding
import com.guvnoh.binl.databinding.RecordLayoutBinding
import androidx.fragment.app.activityViewModels
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.viewmodels.ReceiptViewModel
import com.guvnoh.binl.viewmodels.RecordsViewModel


class Records: Fragment() {
    private lateinit var binding: RecordLayoutBinding
    private val recordsViewModel: RecordsViewModel by activityViewModels()
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecordLayoutBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecord()
    }

    private fun createRecord(data: ReceiptData, index: Int): View {
        val cardBinding = ReceiptCardDesignBinding.inflate(layoutInflater)
        with(cardBinding) {
            Totallabel.text = data.productTotal.toString()
            label.text = data.productName
            kwantity.text = data.productQty

        }
        return  cardBinding.root
    }
    private fun getRecord(){
        val receipt = ReceiptViewModel(application = Application())
        val record = receipt.record.value
        //val recordList = record.value
        val customer = "Customer: ${receipt.customerName}"
        val grandTotal = receipt.grandTotal.value
        binding.customerName.text = customer
        binding.rGrandTotal.text = String.format(grandTotal.toString())
        record.forEachIndexed{ index, receiptData ->
            val recordEntry = createRecord(receiptData, index)
            binding.recordContainer.addView(recordEntry)
        }
    }
}