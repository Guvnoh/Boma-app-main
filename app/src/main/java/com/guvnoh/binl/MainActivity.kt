package com.guvnoh.binl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.guvnoh.binl.databinding.ProductsBinding

class MainActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,  // Pass the Toolbar here
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        replaceFragment(ChangePrice(), "Change Price" )

        //setting up navigation logic
        val navView = findViewById<NavigationView>(R.id.navView) //refers to navigation menu (App's side menu)
        navView.setNavigationItemSelectedListener { //set what happens when menu button is clicked
            it.isChecked = true
            when(it.itemId){
                R.id.products ->{
                    supportActionBar?.show()
                    replaceFragment(Products(), it.title.toString())
                }
                R.id.change_price ->{
                    replaceFragment(ChangePrice(), it.title.toString())
                }
                R.id.records ->{
                    replaceFragment(Records(), it.title.toString())
                }
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment, title:String){
        val fragmentManager = supportFragmentManager
        val fragtrans = fragmentManager.beginTransaction()
        fragtrans.replace(R.id.frame1, fragment)
        fragtrans.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

}