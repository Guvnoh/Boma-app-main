package com.guvnoh.binl.pages
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.guvnoh.binl.data.Product
import com.guvnoh.binl.R
import com.guvnoh.binl.data.bomaBrands
import com.guvnoh.binl.data.getDatabaseProductList
import com.guvnoh.binl.data.getSortedBrandData
import com.guvnoh.binl.data.getUpdatedDisplayList
import com.guvnoh.binl.databinding.ChangePriceBinding
import com.guvnoh.binl.databinding.PriceCardsBinding
import java.text.DecimalFormat

class ChangePrice : Fragment() {

    private val formatter = DecimalFormat("#,###")

    private lateinit var brandData: MutableList<Product>
    private lateinit var display: MutableList<Product>

    private var _binding: ChangePriceBinding? = null
    private val changePriceBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChangePriceBinding.inflate(inflater, container, false)
        return changePriceBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        brandData = getSortedBrandData()

        getDatabaseProductList {
            getUpdatedDisplayList(it, brandData)
            display = brandData

            if (isAdded && view != null) {  // ✅ robust guard
                populatePriceCards(display)
            }

            changePriceBinding.doneChanging.setOnClickListener {
                updatePrices()
                updateDatabase()
                Toast.makeText(requireContext(), "Prices Updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getDatabaseProductList {
            getUpdatedDisplayList(it, brandData)
            display = brandData

            if (isAdded && view != null) {  // ✅ robust guard
                populatePriceCards(display)
            }

        }
    }

    private fun updatePrices() {
        val container = changePriceBinding.container
        for (i in 0 until container.childCount) {
            val cardView = container.getChildAt(i)
            val newPriceEntry = cardView.findViewById<EditText>(R.id.new_price_Entry)
            val newPriceText = newPriceEntry.text.toString()
            if (newPriceText.isNotEmpty()) {
                val newPrice = newPriceText.toDoubleOrNull()
                if (newPrice != null) {
                    display[i].productPrice = newPrice
                }
            }
        }
    }

    private fun updateDatabase() {
        for (n in display) {
            bomaBrands.child(n.productName).setValue(n.productPrice)
        }
    }

    private fun populatePriceCards(list: MutableList<Product>) {
        val container = changePriceBinding.container
        container.removeAllViews()

        list.forEachIndexed { index, product ->
            val cardBinding = PriceCardsBinding.inflate(layoutInflater, container, false)
            with(cardBinding) {
                brandLabel.text = product.productName
                currentPrice.text = "₦${formatter.format(product.productPrice)}"
                productImage.setImageResource(product.productImage)
            }
            container.addView(cardBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
