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
import com.guvnoh.binl.App
import com.guvnoh.binl.data.Product
import com.guvnoh.binl.R
import com.guvnoh.binl.data.ReceiptData
import com.guvnoh.binl.data.getDatabaseProductList
import com.guvnoh.binl.data.getSortedBrandData
import com.guvnoh.binl.data.getUpdatedDisplayList
import com.guvnoh.binl.databinding.ProductCardLayoutBinding
import com.guvnoh.binl.databinding.ProductsLayoutBinding
import com.guvnoh.binl.data.formatter
import com.guvnoh.binl.data.getFormattedTotal
import com.guvnoh.binl.data.halfAndQuarter
import kotlinx.coroutines.launch
import kotlin.text.StringBuilder

class Products: Fragment() {
    private val vm by lazy{
        (requireActivity().application as App).viewModel
    }
    private var _binding: ProductsLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var display: MutableList<Product>
    private lateinit var brandData: MutableList<Product>


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
        //productCard = ProductCardLayoutBinding.inflate(layoutInflater)
        setCustomerName()
        display = mutableListOf()
        brandData = getSortedBrandData()

        getDatabaseProductList {
            updatedProductList ->
            //Changes in price and new products present on the database that are not originally
            //on the app are injected into the app here (requires network connection)
            getUpdatedDisplayList(updatedProductList, brandData)
            display = brandData

            loadData()

        }


        binding.clearQtys.setOnClickListener {
            clearData()
        // clears data in all quantity inputs, grand total and customer name when clicked
        }
        binding.doneBtn.setOnClickListener {
            val intent = Intent(requireContext(), Receipt::class.java)
            startActivity(intent)
            //opens the receipt page and sends over all necessary data for the receipt when clicked
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


    private fun createProductCard(product: Product): View {
        val cardBinding = ProductCardLayoutBinding.inflate(layoutInflater)
        with(cardBinding){
            brandLabel.text = product.productName
            priceLabel.text = StringBuilder()
                .append("₦")
                .append(formatter.format(product.productPrice))
            productImage.setImageResource(product.productImage)
            for (i in vm.record.value){
                if (i.productName == product.productName){
                    quantityEntry.setText(i.productQty)
                }
            }
            binding.sumView.text = StringBuilder()
                .append("Grand Total: ${getFormattedTotal(vm.grandTotal.value.toDouble())}").toString()

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

                val productTotalString = getFormattedTotal(productTotal)

                cardItemLayoutBinding.individualTotalLabel.text = StringBuilder()
                    .append("₦")
                    .append(productTotalString)
                val drinkName = cardItemLayoutBinding.brandLabel.text.toString()
                vm.record.value.removeAll{it.productName == drinkName}
                vm.addReceiptItem(
                    ReceiptData(
                        productQty = halfAndQuarter(productQuantity),
                        productTotal = productTotal,
                        productName = drinkName,
                        productTotalString = productTotalString
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
        val grandTotal = vm.grandTotal.value
        binding.sumView.text  = getFormattedTotal(grandTotal.toDouble())
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
        display.forEach{product ->
            val productCard = createProductCard(product)
            binding.container.addView(productCard)
        }
    }
}
