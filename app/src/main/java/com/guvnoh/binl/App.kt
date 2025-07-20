package com.guvnoh.binl

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.guvnoh.binl.viewmodels.ReceiptViewModel

class App: Application() {
    val viewModel: ReceiptViewModel by lazy{
        ViewModelProvider
            .AndroidViewModelFactory
            .getInstance(this)
            .create(ReceiptViewModel::class.java)
    }

}