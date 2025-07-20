package com.guvnoh.binl.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()
val firstBranch : DatabaseReference = dataBase.reference.child("Boma")
val bomaBrands: DatabaseReference = firstBranch.child("bomaPrices")


fun getDatabaseProductList(callback: (MutableList<Product>) -> Unit) {
    val brandList = mutableListOf<Product>()
    bomaBrands.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (eachBrand in snapshot.children) {

                val brand = eachBrand.key.toString()

                if (eachBrand.child("1").exists()){
                    val category = eachBrand
                        .child("1")
                        .getValue(String::class.java).toString()
                    val brandPrice = eachBrand
                        .child("0")
                        .getValue(Double::class.java).toString().toDouble()
                    brandList.add(Product(
                        productName = brand,
                        productPrice = brandPrice,
                        category = category))
                }else {
                    val brandPrice = eachBrand.value.toString().toDouble()
                    brandList.add(Product(productName = brand, productPrice = brandPrice))
                }

            }
            callback(brandList) // <--- Call back with the result
        }

        override fun onCancelled(error: DatabaseError) {
            // handle error if needed
        }
    })
}


fun getUpdatedDisplayList(databaseList: MutableList<Product>, display: MutableList<Product>){
    val newProducts = mutableListOf<Product>()

    for (databaseProduct in databaseList){
        //this code block updates the prices of already existing products
        val productToUpdate = display.find {
            displayProduct ->
            displayProduct.productName == databaseProduct.productName
        }
        if (productToUpdate!=null){
            productToUpdate.productPrice = databaseProduct.productPrice
        }else newProducts.add(databaseProduct)

    }

    for (newProduct in newProducts) {
        //this code block primarily adds new products to the UI
        // and sorts them according to their category
        //val insertIndex = display.indexOfLast { it.category == newProduct.category }
        // Finds the index of the last item with same category
        when(newProduct.category){
            "Nbl" -> {
                nbl.add(newProduct)
                nbl.sortBy { it.productName }
            }
            "Hero" -> {
                hero.add(newProduct)
                hero.sortBy { it.productName }
            }
            "Guinness" -> {
                guinness.add(newProduct)
                guinness.sortBy { it.productName }
            }
            "Coca-cola Bottles" -> {
                cocacolaBottles.add(newProduct)
                cocacolaBottles.sortBy { it.productName }
            }
            "Cans" -> {
                cans.add(newProduct)
                cans.sortBy { it.productName }
            }
            "Pets" -> {
                pets.add(newProduct)
                pets.sortBy { it.productName }
            }
        }
//        if (insertIndex >= 0) {
//            display.add(insertIndex + 1, newProduct)
//        } else {
//            display.add(newProduct)
//        }
        display.clear()
        val newDisplay = getSortedBrandData()
        display.addAll(newDisplay)
    }
}