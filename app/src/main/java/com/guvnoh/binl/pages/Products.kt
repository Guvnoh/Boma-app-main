package com.guvnoh.binl.pages

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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.guvnoh.binl.App
import com.guvnoh.binl.data.Product
import com.guvnoh.binl.R
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.getDatabaseProductList
import com.guvnoh.binl.data.getSortedBrandData
import com.guvnoh.binl.data.getUpdatedDisplayList
import com.guvnoh.binl.databinding.ProductCardLayoutBinding
import com.guvnoh.binl.databinding.ProductsLayoutBinding
import com.guvnoh.binl.formatter
import com.guvnoh.binl.halfAndQuarter
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class Products (): Fragment() {
    private val vm by lazy{
        (requireActivity().application as App).viewModel
    }
    private var _binding: ProductsLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataMap: MutableMap<String, Product>
    private lateinit var display: MutableList<Product>
    private lateinit var brandData: MutableList<Product>
    private lateinit var getDbPrices: MutableMap<String, Double>


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
        setCustomerName()
        display = mutableListOf()
        brandData = getSortedBrandData()

        getDatabaseProductList {
            getUpdatedDisplayList(it, brandData)
            display = brandData

            if (isAdded && view != null) {  // ✅ robust guard
                loadData()
            }

        }
        //loads up dataMap with products and database prices
        // ready to send to display
        //updates display with current data from database

        // holds a list with all products,
        // images and prices and loads them into cards

        binding.clearQtys.setOnClickListener {
            clearData() // clears data in all quantity inputs, grand total and customer name
        }
        binding.doneBtn.setOnClickListener {
            val intent = Intent(requireContext(), Receipt::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getDatabaseProductList {
            getUpdatedDisplayList(it, brandData)
            display = brandData

            if (isAdded && view != null) {  // ✅ robust guard
                loadData()
            }

        }
    }


    private fun setCustomerName(){
        val customer = binding.CustomerNameEntry
        customer.doAfterTextChanged { text->
            vm.getCustomerName(text.toString())
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.customerName.collect{
                text->
                if (customer.text.toString()!= text){
                    customer.setText(text)
                }
            }
        }
    }

    private fun createProductCard(product: Product, position: Int): View {
        val cardBinding = ProductCardLayoutBinding.inflate(layoutInflater)
        with(cardBinding){
            brandLabel.text = product.productName
            priceLabel.text = StringBuilder()
                .append("₦")
                .append(formatter.format(product.productPrice))
            productImage.setImageResource(product.productImage)

            quantityEntry.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    calculateResults(p0?.toString()?: "", product.productPrice, cardBinding)
                }
            })
        }
        return  cardBinding.root
    }

    private fun calculateResults(input: String, productPrice: Double,
                                 cardItemLayoutBinding: ProductCardLayoutBinding){
        if (input.isNotEmpty()){
            try {
                val productQuantity = input.toDouble()

                val productTotal = productQuantity * productPrice
//                calculationResults[position] = productTotal

                cardItemLayoutBinding.individualTotalLabel.text = StringBuilder()
                    .append("₦")
                    .append(formatter.format(productTotal))
                val drinkName = cardItemLayoutBinding.brandLabel.text.toString()
                vm.record.value.removeAll{it.productName == drinkName}
                vm.addReceiptItem(
                    ReceiptData(
                    productQty = halfAndQuarter(productQuantity),
                    productTotal = productTotal,
                    productName = drinkName
                )
                )
            }catch (e: NumberFormatException){
                val brand = cardItemLayoutBinding.brandLabel.text.toString()
                vm.removeReceiptItem(brand)
                cardItemLayoutBinding.individualTotalLabel.text = getString(R.string.default_total)


            }
        }else{
            val brand = cardItemLayoutBinding.brandLabel.text.toString()
            vm.removeReceiptItem(brand)
            cardItemLayoutBinding.individualTotalLabel.text = getString(R.string.default_total)
        }
        vm.getTotal()
        binding.sumView.text  = StringBuilder()
            .append("Grand Total: ₦")
            .append(formatter.format(vm.grandTotal.value))
    }

    private fun updateGrandTotal(){
        vm.getTotal()
        val grandTotal = vm.grandTotal.value
        if (grandTotal == 0) {
            binding.sumView.text = getString(R.string.default_grand_total)
        }else{
            binding.sumView.text = StringBuilder()
                .append("Grand Total: ₦")
                .append(formatter.format(grandTotal))
        }
    }
    private fun clearData(){
        val customerName: EditText = binding.CustomerNameEntry
        customerName.text?.clear()
        vm.clear()
        binding.container.children.forEach { view ->
            val cardItemLayoutBinding = ProductCardLayoutBinding.bind(view)
            cardItemLayoutBinding.quantityEntry.text?.clear()
            cardItemLayoutBinding.individualTotalLabel.text = getString(R.string.default_total)
        }
        vm.clear()
        binding.sumView.text = getString(R.string.default_total)
    }
    private fun loadData(){
        binding.container.removeAllViews()
        display.forEachIndexed{index, product ->
            val productCard = createProductCard(product, index)
            binding.container.addView(productCard)
        }
    }
}
