package com.cugur24.turkishphonetextinputmask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cugur24.turkish_phone_mask.TurkishPhoneTextWatcher
import com.cugur24.turkishphonetextinputmask.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: ActivityMainBinding
    private lateinit var turkishPhoneMask:TurkishPhoneTextWatcher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater).also {
            turkishPhoneMask = TurkishPhoneTextWatcher(it.etDefaultPhoneNumber)
            it.etDefaultPhoneNumber.addTextChangedListener(turkishPhoneMask)
        }
        setContentView(mainActivityBinding.root)
    }
}