package com.guvnoh.binl

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guvnoh.binl.databinding.ChangePriceBinding
import com.guvnoh.binl.databinding.PriceCardsBinding
import java.io.FileNotFoundException
import java.text.DecimalFormat

class ChangePrice : Fragment() {
//    val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
//    val firstBranch : DatabaseReference = dataBase.reference.child("Boma")
//    val bomaBrands: DatabaseReference = dataBase.reference.child("Boma").child("brandData")
    private val formatter = DecimalFormat("#,###")
    private lateinit var priceCardsBinding: PriceCardsBinding
    private lateinit var brandData: MutableList<Product>
    private lateinit var dataMap: MutableMap<String, Product>
    private lateinit var display: MutableList<Product>
    private var _binding: ChangePriceBinding? = null
    private val changePriceBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChangePriceBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return changePriceBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var newData: MutableList<Product>
//        var displayData : MutableList<Product>
        brandData = mutableListOf(
            //coca cola
            Product("35cl", 3800.0, R.drawable.coke),
            Product("50cl", 6300.0, R.drawable.coke),
            //international breweries
            Product("Budweiser", 9800.0, R.drawable.budweiser),
            Product("Flying fish", 12500.0, R.drawable.fish),
            Product("Hero", 8300.0, R.drawable.hero),
            Product("Trophy", 8500.0, R.drawable.trophy),
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
            Product("Small stout", 19000.0, R.drawable.guinness),
            //Pets & cans
            Product("Beta Malt", 11000.0, R.drawable.beta_malt),
            Product("Grand Malt", 11000.0, R.drawable.grand_malt),
            Product("Amstel can", 13500.0, R.drawable.amstel),
            Product("Life can", 15000.0, R.drawable.life),
            Product("Star can", 15000.0, R.drawable.star),
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
//        getBrandData()
        dataMap = getUpdatedBrandData(requireContext(), "displayData")

        if(dataMap.isNotEmpty()){
            newData = dataMap.values.toMutableList()

            display = newData
            populatePriceCards(display,layoutInflater)

        }else {
            display = brandData
            for (i in brandData){
                dataMap[i.product_name]= i
            }

            saveDataBase(requireContext(), "displayData", dataMap)
            populatePriceCards(display,layoutInflater)
        }



        val done = changePriceBinding.doneChanging
        done.setOnClickListener {
            updatePrices(display)
            saveDataBase(requireContext(), "displayData", dataMap)
            populatePriceCards(display,layoutInflater)
        }

    }
//    private fun getBrandData(){
//        bomaBrands.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (eachBrand in snapshot.children){
//                    val brand = eachBrand.getValue(Product::class.java)
//                    if (brand != null) {
//                        display.add(brand)
//                        val size = display.size
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }
//    private fun updateDatabase(){
//        bomaBrands.removeValue()
//        for (n in display){
//            bomaBrands.child(n.product_name).setValue(n)
//        }
//    }

    private fun saveDataBase(context: Context, key: String, map: MutableMap<String, Product>) {
        val sharedPreferences = context.getSharedPreferences("myDB", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonString = gson.toJson(map)

        editor.putString(key, jsonString)
        editor.apply()

    }

    private fun populatePriceCards(list: MutableList<Product>, inflater: LayoutInflater): View {
        val container = changePriceBinding.container
        container.removeAllViews() // Clear previous views if any

        fun createProductCard(product: Product, position: Int): View {
            val cardBinding = PriceCardsBinding.inflate(layoutInflater)
            with(cardBinding) {
                brandLabel.text = product.product_name
                currentPrice.text = "₦" + formatter.format(product.product_price)
                productImage.setImageResource(product.product_image)
            }
            return cardBinding.root
        }
        list.forEachIndexed { index, products ->
            val productCard = createProductCard(products, index)
            container.addView(productCard)
        }
        return container
    }



    private fun updatePrices(list: MutableList<Product>) {
        val container = changePriceBinding.container

        for (i in 0 until container.childCount) {
            val cardView = container.getChildAt(i)

            val newPriceEntry = cardView.findViewById<EditText>(R.id.new_price_Entry)
            val newPriceText = newPriceEntry.text.toString()

            if (newPriceText.isNotEmpty()) {
                val newPrice = newPriceText.toDoubleOrNull()
                if (newPrice != null) {
                    priceCardsBinding= PriceCardsBinding.inflate(layoutInflater)
                    brandData[i].product_price = newPrice
                    //updates the brandData with the new price
                    var pName = brandData[i].product_name
                    //updates the price in the UI
                    priceCardsBinding.currentPrice.text = newPriceText
                    //saves the new prices to the datamap
                    dataMap[pName] = Product(pName, newPrice, brandData[i].product_image)
                    display = mutableListOf()
                    display = dataMap.values.toMutableList()
                    display = brandData
                }

            }
        }
        // Refresh UI

    }


    private fun getUpdatedBrandData(context: Context, key: String): MutableMap<String, Product> {
        val sharedPreferences = context.getSharedPreferences("myDB", Context.MODE_PRIVATE)

        val jsonString = sharedPreferences.getString(key, null)?: return mutableMapOf()

        val gson = Gson()
        val type = object : TypeToken<MutableMap<String, Product>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}

