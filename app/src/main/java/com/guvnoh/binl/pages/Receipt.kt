package com.guvnoh.binl.pages

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guvnoh.binl.App
import com.guvnoh.binl.R
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.RecordData
import com.guvnoh.binl.data.bomaRecords
import com.guvnoh.binl.databinding.ReceiptCardDesignBinding
import com.guvnoh.binl.databinding.ReceiptLayoutBinding
import com.guvnoh.binl.data.formatter
import com.guvnoh.binl.data.getFormattedTotal
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.text.StringBuilder

class Receipt: AppCompatActivity() {
    private lateinit var customerName: TextView
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var grandTotalView: TextView
    private lateinit var saveScreenshot: Button
    private lateinit var displayData: MutableList<ReceiptData>
    private lateinit var receiptBinding: ReceiptLayoutBinding
    private lateinit var copyBtn: Button
    private lateinit var balanceEntry: TextView
    private val storagePermissionCode  = 1001
    private val vm by lazy{
        (application as App).viewModel
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptBinding = ReceiptLayoutBinding.inflate(layoutInflater)
        setContentView(receiptBinding.root)
        val watZoneId = ZoneId.of("Africa/Lagos")
        val watTime = ZonedDateTime.now(watZoneId)
        val formattedDate = DateTimeFormatter.ofPattern("d-MMM-yyyy")
        val formattedTime = DateTimeFormatter.ofPattern("H:mma")
        val dateNow = watTime.format(formattedDate).toString()
        val timeNow = watTime.format(formattedTime).toString()

        saveScreenshot = findViewById(R.id.savescrnsht)
        customerName = findViewById(R.id.rCustomerName)
        grandTotalView = findViewById(R.id.rGrandTotal)
        dateView = findViewById(R.id.rDate)
        timeView = findViewById(R.id.rTime)
        copyBtn = findViewById(R.id.copyReceipt)
        balanceEntry = receiptBinding.balanceEntry

        dateView.text = StringBuilder()
            .append("Date: ")
            .append(dateNow)
        timeView.text = StringBuilder()
            .append("Time: ")
            .append(timeNow)
        val customer = vm.customerName.value

        //code below displays the main receipt layout
        ReceiptCardDesignBinding.inflate(layoutInflater)

        displayData= vm.record.value


        load(displayData)


        val grandTotal = vm.grandTotal.value

        balanceEntry.text = getFormattedTotal(vm.grandTotal.value.toDouble())

        customerName.text = StringBuilder()
            .append("Customer: $customer")

        grandTotalView.text = getFormattedTotal(grandTotal.toDouble())

        getBalance(grandTotal)

        saveScreenshot.setOnClickListener{
            if (hasStoragePermission(this)){
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                val bitmap = takeScreenshot(rootView)
                val message = saveScreenshot(this,bitmap)
                Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            }else{
                    requestStoragePermission(this)
            }
        }

        copyBtn.setOnClickListener{
            val textToCopy = copyToClipboard()
            val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show()
        }


        receiptBinding.saveSale.setOnClickListener{

            val newRecord = RecordData(
                primaryKey = "$dateNow$timeNow",
                customerName = vm.customerName.value,
                receipt = vm.record.value,
                grandTotal = vm.grandTotal.value.toString()
            )
            saveRecord(newRecord)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==storagePermissionCode){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecord(record: RecordData){
        bomaRecords.child(bomaRecords.push().key.toString()).setValue(record)
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()
    }

    private fun copyToClipboard(): String{
        //The variable finalText holds the complete text to be sent to the clipboard
        val finalText = StringBuilder()

        for (i in vm.record.value){
            val copiedQuantity: String = i.productQty
            val textToCopy = "$copiedQuantity ${i.productName} ${i.productTotalString}\n"

            finalText.append(textToCopy)
        }
        if (vm.record.value.size>1){
            finalText.append("Total: ${grandTotalView.text}")
        }

        return finalText.toString()
    }


    private fun hasStoragePermission(context:Context):Boolean{

        return if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED
        }else{
            ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
        }

    }
    private fun requestStoragePermission(activity: Activity){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),storagePermissionCode
            )
        }else{
            ActivityCompat.requestPermissions(activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionCode
            )
        }
    }
    private fun takeScreenshot(view: View): Bitmap{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    private fun saveScreenshot(context:Context, bitmap:Bitmap):String{
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply{
            put(MediaStore.Images.Media.DISPLAY_NAME,filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image.png")
            put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outPutStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
            }
            return "Screenshot saved: $filename"
        }
        return "failed to save screenshot"
    }

    private fun createReceiptData(data: ReceiptData): View {
        val cardBinding = ReceiptCardDesignBinding.inflate(layoutInflater)
        with(cardBinding) {
            Totallabel.text = data.productTotalString
            label.text = data.productName
            kwantity.text = data.productQty

        }
        return  cardBinding.root
    }
    private fun getBalance(grandTotal:Int) {
        var rawInput = 0 //input before formatting (needed for calculations)
        val amtPaidEntry: EditText = findViewById(R.id.amt_paid_entry)
        val balanceEntry: TextView = findViewById(R.id.balance_entry)
        amtPaidEntry.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != current) {
                    amtPaidEntry.removeTextChangedListener(this)
                    val cleanString = p0
                        .toString()
                        .replace("₦", "")
                        .replace(",", "")
                        .trim()
                    if (cleanString.isNotEmpty()) {
                        try {
                            rawInput = cleanString.toInt()
                            val formatted = getFormattedTotal(rawInput.toDouble())
                            current = formatted
                            amtPaidEntry.setText(formatted)
                            amtPaidEntry.setSelection(formatted.length)
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    } else {
                        current = ""
                        rawInput = 0
                        amtPaidEntry.setText("")
                    }
                    val balance = grandTotal - rawInput
                    balanceEntry.text = getFormattedTotal(balance.toDouble())
                    amtPaidEntry.addTextChangedListener(this)
                }
            }
        })
    }
    private fun load(display: MutableList<ReceiptData>) {
        display.forEach{ product ->
            val receiptData = createReceiptData(product)
            receiptBinding.receiptContainer.addView(receiptData)

        }
    }
}