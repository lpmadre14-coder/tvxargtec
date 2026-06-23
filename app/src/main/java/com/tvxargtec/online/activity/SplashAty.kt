package com.tvxargtec.online.activity

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.tvxargtec.online.R
import com.tvxargtec.online.base.BaseActivity

class SplashAty : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_splash

    override fun initView() {
        val logo = findViewById<ImageView>(R.id.ivLogo)
        val subtitle = findViewById<TextView>(R.id.tvTagline)

        logo.scaleX = 0.3f
        logo.scaleY = 0.3f
        logo.alpha = 0f
        subtitle.alpha = 0f

        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()

        subtitle.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(400)
            .setInterpolator(DecelerateInterpolator())
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
            val completed = prefs.getBoolean("onboarding_completed", false)
            val target = if (completed) PinAty::class.java else OnboardingAty::class.java
            startActivity(Intent(this, target))
            finish()
        }, 2500)
    }
}
