package com.tvxargtec.online.activity

import android.app.AlertDialog
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tvxargtec.online.R
import com.tvxargtec.online.base.BaseActivity
import com.tvxargtec.online.fragment.*
import com.tvxargtec.online.utils.UpdateManager

class MainAty : BaseActivity() {

    override fun getLayoutResId(): Int = R.layout.activity_main

    private lateinit var bottomNav: BottomNavigationView

    companion object {
        private var instance: MainAty? = null
        @JvmStatic
        fun getInstance(): MainAty? = instance
    }

    private val onNavItemSelected = { item: MenuItem ->
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_live -> LiveTvFragment()
            R.id.nav_series -> SeriesFragment()
            R.id.nav_movies -> MoviesFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }
        
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .commit()
        true
    }

    override fun initView() {
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener(onNavItemSelected)
        instance = this
    }

    override fun initData() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
        checkForUpdatesSilent()
    }

    private fun checkForUpdatesSilent() {
        val prefs = getSharedPreferences("update_prefs", MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0)
        val now = System.currentTimeMillis()
        if (now - lastCheck < 86400000) return

        prefs.edit().putLong("last_update_check", now).apply()

        val updateManager = UpdateManager(this, "lpmadre14-coder", "tvxargtec")
        updateManager.checkForUpdates(object : UpdateManager.UpdateListener {
            override fun onUpdateAvailable(version: String, notes: String, apkUrl: String) {
                AlertDialog.Builder(this@MainAty)
                    .setTitle("Actualización disponible: $version")
                    .setMessage(notes.ifEmpty { "Nueva versión disponible. ¿Descargar?" })
                    .setPositiveButton("Actualizar") { _, _ ->
                        updateManager.downloadAndInstall(apkUrl)
                    }
                    .setNegativeButton("Más tarde", null)
                    .show()
            }
            override fun onUpToDate() {}
            override fun onError(error: String) {}
        })
    }

    fun switchFragment(fragment: Fragment, menuItemId: Int? = null) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .commit()
        
        menuItemId?.let { bottomNav.menu.findItem(it)?.isChecked = true }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
