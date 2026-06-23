package com.tvxargtec.online.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tvxargtec.online.R
import com.tvxargtec.online.base.BaseActivity

class PinAty : BaseActivity() {

    companion object {
        private const val PREF_PIN = "app_pin"
        private const val PIN_KEY = "saved_pin"
        private const val DEFAULT_PIN = "525452"
    }

    override fun getLayoutResId(): Int = R.layout.activity_pin

    private lateinit var etPin1: EditText
    private lateinit var etPin2: EditText
    private lateinit var etPin3: EditText
    private lateinit var etPin4: EditText
    private lateinit var etPin5: EditText
    private lateinit var etPin6: EditText
    private lateinit var tvError: TextView
    private var currentPin = ""

    override fun initView() {
        etPin1 = findViewById(R.id.etPin1)
        etPin2 = findViewById(R.id.etPin2)
        etPin3 = findViewById(R.id.etPin3)
        etPin4 = findViewById(R.id.etPin4)
        etPin5 = findViewById(R.id.etPin5)
        etPin6 = findViewById(R.id.etPin6)
        tvError = findViewById(R.id.tvPinError)
    }

    override fun initData() {
        val prefs = getSharedPreferences(PREF_PIN, Context.MODE_PRIVATE)
        if (!prefs.contains(PIN_KEY)) {
            prefs.edit().putString(PIN_KEY, DEFAULT_PIN).apply()
        }
        setupPinInputs()
    }

    private fun setupPinInputs() {
        val pins = arrayOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6)
        for (i in pins.indices) {
            pins[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < pins.size - 1) {
                        pins[i + 1].requestFocus()
                    }
                    checkPin()
                }
            })
        }
    }

    private fun checkPin() {
        val entered = etPin1.text.toString() + etPin2.text.toString() +
                etPin3.text.toString() + etPin4.text.toString() +
                etPin5.text.toString() + etPin6.text.toString()

        if (entered.length < 6) return

        currentPin = entered
        val prefs = getSharedPreferences(PREF_PIN, Context.MODE_PRIVATE)
        val savedPin = prefs.getString(PIN_KEY, DEFAULT_PIN)

        if (entered == savedPin) {
            prefs.edit().putBoolean("pin_verified", true).apply()
            startActivity(Intent(this, MainAty::class.java))
            finish()
        } else {
            tvError.visibility = TextView.VISIBLE
            clearPins()
            Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearPins() {
        val pins = arrayOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6)
        for (et in pins) et.text.clear()
        etPin1.requestFocus()
    }
}
