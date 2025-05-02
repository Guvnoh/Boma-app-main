package com.guvnoh.binl

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.guvnoh.binl.databinding.ActivityMainBinding
import com.guvnoh.binl.databinding.CardItemLayoutBinding
import com.guvnoh.binl.databinding.ProductsLayoutBinding
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class Products : Fragment(R.layout.products_layout) {
    private val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val bomaBrands: DatabaseReference = dataBase.reference.child("Boma").child("bomaPrices")
    private var _binding: ProductsLayoutBinding? = null
    private val binding get() = _binding!!

    private  val  calculationResults = mutableMapOf<Int, Double>()
    private val qtyList = mutableListOf<Double>()
    private val productTotalList = mutableListOf<Double>()
    private val formatter = DecimalFormat("#,###")
    private lateinit var dataMap: MutableMap<String, Product>
    private lateinit var display: MutableList<Product>
    private var brandNameMap = mutableMapOf<String, MutableList<Double>>()
    private lateinit var brandData: MutableList<Product>
    private lateinit var getDataMap: MutableMap<String, Double>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        // Inflate the layout for this fragment
        _binding = ProductsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        display = mutableListOf()
        brandData = mutableListOf(
            //coca cola
            Product("35cl", 3800.0, R.drawable.coke),
            Product("50cl", 6300.0, R.drawable.coke),
            //international breweries
            Product("Budweiser", 9800.0, R.drawable.budweiser),
            Product("Flying fish", 12500.0, R.drawable.fish),
            Product("Hero", 8600.0, R.drawable.hero),
            Product("Trophy", 8700.0, R.drawable.trophy),
            //NBL
            Product("Amstel", 13500.0, R.drawable.amstel),
            Product("Desperados", 15800.0, R.drawable.despy),
            Product("Gulder", 10000.0, R.drawable.gulder),
            Product("Heineken", 11500.0, R.drawable.heineken),
            Product("Legend(big)", 11200.0, R.drawable.legend),
            Product("Life", 8500.0, R.drawable.life),
            Product("Maltina", 13200.0, R.drawable.maltina),
            Product("Radler", 11800.0, R.drawable.radler),
            Product("Star", 9500.0, R.drawable.star),
            Product("Tiger", 14000.0, R.drawable.tiger),
            //Guinness
            Product("Medium stout", 17500.0, R.drawable.guinness),
            Product("Small stout", 19500.0, R.drawable.guinness),
            //Pets & cans
            Product("Beta Malt", 11000.0, R.drawable.beta_malt),
            Product("Grand Malt", 11000.0, R.drawable.grand_malt),
            Product("Amstel can", 13500.0, R.drawable.amstel),
            Product("Life can", 15000.0, R.drawable.life),
            Product("Star can", 12500.0, R.drawable.star),
            Product("Hero can", 10500.0, R.drawable.hero),
            Product("Trophy can", 9500.0, R.drawable.trophy),
            Product("Heineken can", 15500.0, R.drawable.heineken),
            Product("Guinness can", 25000.0, R.drawable.guinness),
            Product("Bigger boy", 4600.0, R.drawable.coke),
            Product("Predator", 5200.0, R.drawable.predator),
            Product("Fearless", 5000.0, R.drawable.fearless),
            Product("Eva water (big)", 3800.0, R.drawable.eva),
            Product("Eva water (small)", 2800.0, R.drawable.eva),
            Product("Aquafina", 2500.0, R.drawable.aquafina),
            Product("Nutri Milk", 6400.0, R.drawable.nutri_milk),
            Product("Nutri Yo", 6900.0, R.drawable.nutri_yo),
            Product("Pop cola (big)", 3700.0, R.drawable.pop_cola),
            Product("Pop cola (small)", 2600.0, R.drawable.pop_cola),
            Product("Pepsi", 4500.0, R.drawable.pepsi),
        )

        getDataMap = mutableMapOf() // placed here to initialize getDataMap
        //getDataMap will contain prices retrieved from the database with the brands as keys
        dataMap = mutableMapOf() // will hold brandData list contents as values and the brand name of each entry as key
        for (i in brandData){
            dataMap[i.product_name] = i //fills up dataMap with brandData Products ready for new price injection
        }
        getBrandPrices() //loads up dataMap with products and database prices ready to send to display
         //updates display with current data from database

        loadData(display) // holds a list with all products, images and prices and loads them into cards

        binding.clearQtys.setOnClickListener {
            clearData() // clears data in all quantity inputs, grand total and customer name
        }
        binding.doneBtn.setOnClickListener {
            val intent = Intent(requireContext(), Receipt::class.java).apply{
                val customerNames = binding.CustomerNameEntry.text.toString()

                // Lists created for all required data in the receipt activity
                val brandList = brandNameMap.keys.toMutableList()
                val qtyList: MutableList<Double> = brandNameMap.values.mapNotNull {
                    it.firstOrNull()
                }.toMutableList()
                val totalList: MutableList<Double> = brandNameMap.values.mapNotNull {
                    it.getOrNull(1)
                }.toMutableList()
                val grandTotal = calculationResults.values.sum()
                val parcelReceipt: ArrayList<ReceiptData> = arrayListOf()
                for (n in qtyList.indices){
                    val name = brandList[n]
                    val qty = qtyList[n].toString()
                    val total = totalList[n].toString()
                    parcelReceipt.add(ReceiptData(qty, name, total))
                }
                putParcelableArrayListExtra("receipt", parcelReceipt )
                putExtra("customer", customerNames)
                putExtra("grandTotal", grandTotal)
            }
            startActivity(intent)

        }
    }

    private fun getBrandPrices() {
        bomaBrands.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getDataMap.clear()
                display.clear() // Clear display list before updating

                for (eachBrand in snapshot.children) {
                    val brand: String = eachBrand.key.toString()
                    val brandPrice: Double = eachBrand.value.toString().toDouble()
                    getDataMap[brand] = brandPrice

                    // Update dataMap with new prices
                    dataMap[brand]?.product_price = brandPrice
                }

                // Refill display list with updated prices
                display.addAll(dataMap.values)
                if (isAdded && view != null) {
                    loadData(display)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun createProductCard(product: Product, position: Int): View {
        val cardBinding = CardItemLayoutBinding.inflate(layoutInflater)
        with(cardBinding){
            brandLabel.text = product.product_name
            priceLabel.text = "₦" + formatter.format(product.product_price)
            productImage.setImageResource(product.product_image)

            quantityEntry.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    calculateResults(p0?.toString()?: "", product.product_price, position, cardBinding)
                }
            })
        }
        return  cardBinding.root
    }

    private fun calculateResults(input: String, productPrice: Double, position: Int,
                                 cardItemLayoutBinding: CardItemLayoutBinding){
        if (input.isNotEmpty()){
            try {
                val productQuantity = input.toDouble()

                val productTotal = productQuantity * productPrice
                calculationResults[position] = productTotal
                qtyList.add(productQuantity)
                productTotalList.add(productTotal)
                cardItemLayoutBinding.individualTotalLabel.text = "₦" + formatter.format(productTotal)
                val drinkName = cardItemLayoutBinding.brandLabel.text.toString()
                brandNameMap[drinkName] = mutableListOf(productQuantity, productTotal )
            }catch (e: NumberFormatException){
                cardItemLayoutBinding.individualTotalLabel.text = "Error"
                calculationResults.remove(position)
                brandNameMap.remove(cardItemLayoutBinding.brandLabel.text.toString())


            }
        }else{
            calculationResults.remove(position)
            cardItemLayoutBinding.individualTotalLabel.text = "₦0.00"
            brandNameMap.remove(cardItemLayoutBinding.brandLabel.text.toString())
        }
        updateGrandTotal()
    }

    private fun updateGrandTotal(){
        val grandTotal = calculationResults.values.sum()
        if (grandTotal == 0.0) {
            binding.sumView.text = "Grand Total: ₦0.00"
        }else{
            binding.sumView.text = "Grand Total: ₦" + formatter.format(grandTotal)
        }
    }
    private fun clearData(){
        val customerName: EditText = binding.CustomerNameEntry
        customerName.text?.clear()
        calculationResults.clear()
        binding.container.children.forEach { view ->
            val cardItemLayoutBinding = CardItemLayoutBinding.bind(view)
            cardItemLayoutBinding.quantityEntry.text?.clear()
            cardItemLayoutBinding.individualTotalLabel.text = "₦0.00"

        }
        updateGrandTotal()
    }
    private fun loadData(brandData: MutableList<Product>){
        this.display

        binding.container.removeAllViews()
        display.forEachIndexed{index, product ->
            val productCard = createProductCard(product, index)
            binding.container.addView(productCard)
        }
    }
}
