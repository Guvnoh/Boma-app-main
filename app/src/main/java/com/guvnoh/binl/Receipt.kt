package com.guvnoh.binl

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.guvnoh.binl.databinding.ActivityReceiptBinding
import com.guvnoh.binl.databinding.ReceiptCardBinding
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Receipt : AppCompatActivity() {
    private lateinit var customerName: TextView
    private lateinit var dateView: TextView
    private lateinit var grandTotal: TextView
    private lateinit var displayData: MutableList<ReceiptData>
    private lateinit var receiptBinding: ActivityReceiptBinding
    private val formatter = DecimalFormat("#,###")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptBinding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(receiptBinding.root)
        val watZoneId = ZoneId.of("Africa/Lagos")
        val watTime = ZonedDateTime.now(watZoneId)
        val formattedTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        customerName = findViewById(R.id.rCustomerName)
        grandTotal = findViewById(R.id.rGrandTotal)
        dateView = findViewById(R.id.rDate)
        dateView.text = "Date/time: " + watTime.format(formattedTime).toString()
        val customer = intent.getStringExtra("customer")

        ReceiptCardBinding.inflate(layoutInflater)
        val receipt = intent.getParcelableArrayListExtra<ReceiptData>("receipt")
        val rData = mutableMapOf<String, MutableList<String>>()
        displayData= mutableListOf()
        if (receipt != null ) {
            var len = receipt.size -1
            if (len>=0){
                while (len>-1) {
                    for (i in receipt) {
                        val productqty = i.product_qty
                        val productname = i.product_name
                        val producttotal = ("₦" + formatter.format(i.product_total.toDouble()))
                        rData[productname] = mutableListOf(productqty, producttotal)
                        len--
                    }
                }
            }

        }
        for( (k, v) in rData){
            displayData.add(ReceiptData(
                v[0],
                k,
                v[1],
            ))
        }
        load(displayData)

        val gtots = intent.getDoubleExtra("grandTotal", 0.00)

        customerName.text = "customer:  $customer"
        grandTotal.text = "₦" + formatter.format(gtots)

    }
    private fun createReceiptData(data: ReceiptData, index: Int): View {
        val cardBinding = ReceiptCardBinding.inflate(layoutInflater)
        with(cardBinding) {
            Totallabel.text = data.product_total
            label.text = data.product_name
            kwantity.text = data.product_qty

        }
        return  cardBinding.root
    }
    private fun load(display: MutableList<ReceiptData>) {
        display.forEachIndexed { index, product ->
            val receiptData = createReceiptData(product, index)
            receiptBinding.receiptContainer.addView(receiptData)

        }
    }
}