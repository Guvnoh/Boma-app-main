package com.guvnoh.binl.pages

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guvnoh.binl.databinding.ReceiptCardDesignBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.guvnoh.binl.App
import com.guvnoh.binl.R
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.RecordData
import com.guvnoh.binl.data.bomaRecords
import com.guvnoh.binl.databinding.RecordCardLayoutBinding
import com.guvnoh.binl.databinding.RecordsBinding


class Records: Fragment() {
    private lateinit var binding: RecordsBinding
    private val vm by lazy{
        (requireActivity().application as App).viewModel
    }
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecordsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayRecords()
    }


    private fun displayRecordReceipt(data: ReceiptData, index: Int): View {
        val cardBinding = ReceiptCardDesignBinding.inflate(layoutInflater)
        with(cardBinding) {
            Totallabel.text = String.format(data.productTotal.toString())
            label.text = data.productName
            kwantity.text = data.productQty

        }
        return  cardBinding.root
    }

    private fun createRecordsDisplay(data: RecordData): View {
        val recordCardBinding = RecordCardLayoutBinding.inflate(layoutInflater)
        with(recordCardBinding) {
            ref.text = data.primaryKey
            customerName.text = data.customerName
            rGrandTotal.text = data.grandTotal
        }
        return recordCardBinding.root
    }

    private fun displayRecords(){

        bomaRecords.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = snapshot.children.mapNotNull { it.getValue(RecordData::class.java) }
                for (record in records.asReversed()){

                    if (record!=null) {
                        val recordView = createRecordsDisplay(record)
                        binding.containerLinearLayout.addView(recordView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

}