package com.guvnoh.binl.data

import com.guvnoh.binl.R

fun getSortedBrandData(): MutableList<Product>{
    val sortedbrandList = mutableListOf<Product>()
    sortedbrandList.addAll(cocacolaBottles.sortedBy { it.productName.lowercase() })
    sortedbrandList.addAll(hero.sortedBy { it.productName.lowercase() })
    sortedbrandList.addAll(nbl.sortedBy { it.productName.lowercase() })
    sortedbrandList.addAll(guinness.sortedBy { it.productName.lowercase() })
    sortedbrandList.addAll(pets.sortedBy { it.productName.lowercase() })
    sortedbrandList.addAll(cans.sortedBy { it.productName.lowercase() })
    return sortedbrandList
}
val cocacolaBottles = mutableListOf(
    //coca cola
    Product("35cl", 3800.0, R.drawable.coke, "Coca-cola"),
    Product("50cl", 6300.0, R.drawable.coke, "Coca-cola"),
)

val hero = mutableListOf(
    //international breweries
    Product("Budweiser", 10400.0, R.drawable.budweiser, "Hero"),
    Product("Castle Lite", 8500.0, category = "Hero"),
    Product("Flying fish", 13000.0, R.drawable.fish, "Hero"),
    Product("Hero", 9000.0, R.drawable.hero, "Hero"),
    Product("Trophy", 9000.0, R.drawable.trophy, "Hero"),
    Product("Trophy Stout", 8500.0, category = "Hero"),
)

val nbl = mutableListOf(
    //NBL
    Product("Amstel", 13500.0, R.drawable.amstel, "Nbl"),
    Product("Desperados", 16600.0, R.drawable.despy, "Nbl"),
    Product("Gulder", 10800.0, R.drawable.gulder, "Nbl"),
    Product("Heineken", 11900.0, R.drawable.heineken, "Nbl"),
    Product("Legend(big)", 11200.0, R.drawable.legend, "Nbl"),
    Product("Life", 9200.0, R.drawable.life, "Nbl"),
    Product("Maltina", 13000.0, R.drawable.maltina, "Nbl"),
    Product("Radler", 13000.0, R.drawable.radler, "Nbl"),
    Product("Star", 10500.0, R.drawable.star, "Nbl"),
    Product("Tiger", 15000.0, R.drawable.tiger, "Nbl"),
)

val guinness = mutableListOf(
    //Guinness
    Product("Medium stout", 17500.0, R.drawable.guinness, "Guinness"),
    Product("Small stout", 19000.0, R.drawable.guinness, "Guinness"),
)
val cans = mutableListOf(
    Product("Beta Malt", 10700.0, R.drawable.beta_malt, "Cans"),
    Product("Grand Malt", 10700.0, R.drawable.grand_malt, "Cans"),
    Product("Amstel can", 13000.0, R.drawable.amstel, "Cans"),
    Product("Life can", 15000.0, R.drawable.life, "Cans"),
    Product("Star can", 12000.0, R.drawable.star, "Cans"),
    Product("Hero can", 10500.0, R.drawable.hero, "Cans"),
    Product("Trophy can", 10500.0, R.drawable.trophy, "Cans"),
    Product("Heineken can", 15500.0, R.drawable.heineken, "Cans"),
    Product("Guinness can", 25000.0, R.drawable.guinness, "Cans"),

    )
val pets = mutableListOf(
    //Pets
    Product("Bigger boy", 4600.0, R.drawable.coke, "Pets"),
    Product("Predator", 5400.0, R.drawable.predator, "Pets"),
    Product("Fearless", 5000.0, R.drawable.fearless, "Pets"),
    Product("Eva water (Big)", 3900.0, R.drawable.eva, "Pets"),
    Product("Eva water (75cl)", 2900.0, R.drawable.eva, "Pets"),
    Product("Rex water (75cl)", 2900.0, category = "Pets"),
    Product("Aquafina", 2400.0, R.drawable.aquafina, "Pets"),
    Product("Nutri Milk", 6400.0, R.drawable.nutri_milk, "Pets"),
    Product("Nutri Yo", 7000.0, R.drawable.nutri_yo, "Pets"),
    Product("Pop cola (big)", 3600.0, R.drawable.pop_cola, "Pets"),
    Product("Pop cola (small)", 2600.0, R.drawable.pop_cola, "Pets"),
    Product("Pepsi", 4500.0, R.drawable.pepsi, "Pets"),
)