package com.tvxargtec.online.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        initView()
        initData()
    }

    protected abstract fun getLayoutResId(): Int

    protected open fun initView() {}
    protected open fun initData() {}

    protected fun setupToolbar(toolbar: Toolbar, title: String, showBack: Boolean = true) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        supportActionBar?.title = title
    }

    protected fun showLoading() {}
    protected fun hideLoading() {}

    protected fun showToast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
