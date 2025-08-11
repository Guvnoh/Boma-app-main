package com.guvnoh.binl.pages

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.guvnoh.binl.data.Product
import com.guvnoh.binl.data.bomaBrands
import com.guvnoh.binl.databinding.AddProductMainBinding

class AddProduct: Fragment() {
    private var _binding: AddProductMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var brandName: EditText
    private lateinit var productPrice: EditText
    private lateinit var addButton: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddProductMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        brandName = binding.inputName
        productPrice = binding.inputPrice
        addButton = binding.addButton
        //val x = Product()

        val spinner: Spinner = binding.companySelector
        var companySelected = ""

        val spinnerOptions = mutableListOf(
            "Nbl",
            "Hero",
            "Guinness",
            "Cocacola Bottles",
            "Cans",
            "Pets"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            spinnerOptions
        )

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ){
                val selectedItem = spinnerOptions[position]
                companySelected = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        addButton.setOnClickListener {

            if (brandName.text.isNotBlank() &&
                productPrice.text.isNotBlank()){
                val newProduct = Product(
                    brandName.text.toString(),
                    productPrice.text.toString().toDouble(),
                    category = companySelected
                )
                bomaBrands
                    .child(newProduct.productName)
                    .setValue(listOf(newProduct.productPrice, companySelected))
            }
            Toast.makeText(requireContext(), "Product added.\nPlease restart app!", Toast.LENGTH_LONG).show()
            brandName.text.clear()
            productPrice.text.clear()
        }


    }
}