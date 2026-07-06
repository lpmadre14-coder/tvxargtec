package com.tvxargtec.online.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tvxargtec.online.R

class TvAty : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_fragment_container, TvBrowseFragment())
                .commit()
        }
    }
}
