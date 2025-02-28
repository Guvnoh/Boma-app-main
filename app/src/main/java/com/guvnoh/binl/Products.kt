package com.guvnoh.binl

import android.content.Context
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.children
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guvnoh.binl.databinding.CardItemLayoutBinding
import com.guvnoh.binl.databinding.ProductsBinding
import java.io.FileNotFoundException
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class Products : Fragment() {
    private var _binding: ProductsBinding? = null
    private val binding get() = _binding!!

    private  val  calculationResults = mutableMapOf<Int, Double>()
    private val qtyList = mutableListOf<Double>()
    private val productTotalList = mutableListOf<Double>()
    private val formatter = DecimalFormat("#,###")
    private lateinit var dataMap: MutableMap<String, Product>
    private lateinit var updatedBrandData: MutableList<Product>
    private lateinit var brandData: MutableList<Product>
    private var brandNameMap = mutableMapOf<String, MutableList<Double>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ProductsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updatedBrandData = mutableListOf()

        dataMap = try {
            getUpdatedBrandData(requireContext(), "displayData")
        }catch (
            e: FileNotFoundException
        ){
            mutableMapOf()
        }catch (e: NullPointerException){
            mutableMapOf()
        }
        updatedBrandData = dataMap.values.toMutableList()

        loadData(updatedBrandData) // holds a list with all products, images and prices and loads them into cards


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


    private fun getUpdatedBrandData(context: Context, key: String): MutableMap<String, Product> {
        val sharedPreferences = context.getSharedPreferences("myDB", Context.MODE_PRIVATE)

        val jsonString = sharedPreferences.getString(key, null)?: return mutableMapOf()

        val gson = Gson()
        val type = object : TypeToken<MutableMap<String, Product>>() {}.type
        return gson.fromJson(jsonString, type)
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
        println(brandNameMap)
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
        calculationResults.clear()
        binding.container.children.forEach { view ->
            val cardItemLayoutBinding = CardItemLayoutBinding.bind(view)
            cardItemLayoutBinding.quantityEntry.text?.clear()
            cardItemLayoutBinding.individualTotalLabel.text = "₦0.00"
            val customerName: EditText = binding.CustomerNameEntry
            customerName.text?.clear()
        }
        updateGrandTotal()
    }
    private fun loadData(brandData: MutableList<Product>){
        this.updatedBrandData

        binding.container.removeAllViews()
        updatedBrandData.forEachIndexed{index, product ->
            val productCard = createProductCard(product, index)
            binding.container.addView(productCard)
        }
    }
}
