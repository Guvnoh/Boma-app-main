package com.guvnoh.binl

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
import com.guvnoh.binl.databinding.ActivityReceiptBinding
import com.guvnoh.binl.databinding.ReceiptCardDesignBinding
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Receipt : AppCompatActivity() {
    private lateinit var customerName: TextView
    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private lateinit var grandTotal: TextView
    private lateinit var savescrnsht: Button
    private lateinit var displayData: MutableList<ReceiptData>
    private lateinit var receiptBinding: ActivityReceiptBinding
    private lateinit var copyBtn: Button
    val STORAGE_PERMISSION_CODE  = 1001
    private val formatter = DecimalFormat("#,###")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptBinding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(receiptBinding.root)
        val watZoneId = ZoneId.of("Africa/Lagos")
        val watTime = ZonedDateTime.now(watZoneId)
        val formattedDate = DateTimeFormatter.ofPattern("d-MMM-yyyy")
        val formattedTime = DateTimeFormatter.ofPattern("H:mma")
        savescrnsht = findViewById(R.id.savescrnsht)
        customerName = findViewById(R.id.rCustomerName)
        grandTotal = findViewById(R.id.rGrandTotal)
        dateView = findViewById(R.id.rDate)
        timeView = findViewById(R.id.rTime)
        dateView.text = "Date: " + watTime.format(formattedDate).toString()
        timeView.text = "Time: " +watTime.format(formattedTime).toString()
        val customer = intent.getStringExtra("customer")
        var Grand_Total: Int = 0

        //code below displays the main receipt layout
        ReceiptCardDesignBinding.inflate(layoutInflater)
        val receipt = intent.getParcelableArrayListExtra<ReceiptData>("receipt")
        // The map below holds all the data for every new receipt with the brand name as key,
        //a quantity and total list as value e.g map = {hero: [quantity = 2, total= 18,400]}
        val receiptDataMap = mutableMapOf<String, MutableList<String>>()
        displayData= mutableListOf()
        if (receipt != null ) {
            var len = receipt.size -1
            if (len>=0){
                while (len>-1) {
                    for (i in receipt) {
                        val productqty = formatNum(i.product_qty.toDouble())
                        val productname = i.product_name
                        val producttotal = ("₦" + formatter.format(i.product_total.toDouble()))
                        receiptDataMap[productname] = mutableListOf(productqty, producttotal)
                        len--
                    }
                }
            }

        }
        // The code block below sends the purchase data from the receiptDataMap to the receipt activity(displayData)
        for( (k, v) in receiptDataMap){
            val integerPart = (v[0]).toDouble().toInt()
            val floatPart = (v[0]).toDouble()
            val copiedQuantity: String =
                if (floatPart % 1 == 0.5){
                    if(integerPart==0){
                        "½"
                    } else "$integerPart½"
                }else if (floatPart % 1 == 0.25){
                    if(integerPart==0){
                        "¼"
                    } else "$integerPart¼"
                }
                else v[0]
            displayData.add(ReceiptData(
                copiedQuantity,
                k,
                v[1],
            ))
        }
        load(displayData)

        val gtots = intent.getDoubleExtra("grandTotal", 0.00)
        Grand_Total = gtots.toInt()

        customerName.text = "customer:  $customer"
        grandTotal.text = "₦" + formatter.format(gtots)

        getBalance(Grand_Total)

        savescrnsht.setOnClickListener{
            if (hasStoragePermission(this)){
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                val bitmap = takeScreenshot(rootView)
                val message = saveScreenshot(this,bitmap)
                Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            }else{
                    requestStoragePermission(this)
            }
        }
        val textToCopy = copyToClipboard(receiptDataMap)
        copyBtn = findViewById(R.id.copyReceipt)
        copyBtn.setOnClickListener{
            val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyToClipboard(map: MutableMap<String, MutableList<String>>): String{
        //The variable finalText holds the complete text to be sent to the clipboard
        var finalText: String = ""
        for ((k, v) in map){
            //½¼
            val integerPart = (v[0]).toDouble().toInt()
            val floatPart = (v[0]).toDouble()
            val copiedQuantity: String =
                if (floatPart % 1 == 0.5){
                    if(integerPart==0){
                        "½"
                    } else "$integerPart½"
                }else if (floatPart % 1 == 0.25){
                    if(integerPart==0){
                        "¼"
                    } else "$integerPart¼"
                }else v[0]
            val textToCopy: String = "$copiedQuantity $k ${v[1]}\n"

            finalText+=textToCopy
        }
        if (map.size>1){
            finalText+="Total: ${grandTotal.text}"
        }

        return finalText
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
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),STORAGE_PERMISSION_CODE
            )
        }else{
            ActivityCompat.requestPermissions(activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE
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
        val contentvalues = ContentValues().apply{
            put(MediaStore.Images.Media.DISPLAY_NAME,filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image.png")
            put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentvalues)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outPutStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
            }
            return "Screenshot saved: $filename"
        }
        return "failed to save screenshot"
    }
//    private fun saveScreenshot(bitmap: Bitmap): String{
//        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        val file= File(directory, "screenshot_${System.currentTimeMillis()}.png")
//        try{
//            FileOutputStream(file).use {
//                outputStream ->
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            }
//        }catch (e: IOException){
//            e.printStackTrace()
//        }
//        return ""
//    }
    private fun createReceiptData(data: ReceiptData, index: Int): View {
        val cardBinding = ReceiptCardDesignBinding.inflate(layoutInflater)
        with(cardBinding) {
            Totallabel.text = data.product_total
            label.text = data.product_name
            kwantity.text = data.product_qty

        }
        return  cardBinding.root
    }
    private fun formatNum(value:Double):String{
        return if (value % 1.0 == 0.0){
            value.toInt().toString()
        }else{
            value.toString()
        }
    }
    private fun getBalance(grandTotal:Int){
        val balanceEntry: TextView = findViewById(R.id.balance_entry)
        val amtPaidEntry: EditText = findViewById(R.id.amt_paid_entry)
        amtPaidEntry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                val paidAmount: Int = try {
                    p0?.toString()?.toInt()!!
                }catch (e: NumberFormatException){
                    0
                }
                val balance: Int = grandTotal - paidAmount
                balanceEntry.text = String.format("₦$balance")


            }
        })

    }
    private fun load(display: MutableList<ReceiptData>) {
        display.forEachIndexed { index, product ->
            val receiptData = createReceiptData(product, index)
            receiptBinding.receiptContainer.addView(receiptData)

        }
    }
}