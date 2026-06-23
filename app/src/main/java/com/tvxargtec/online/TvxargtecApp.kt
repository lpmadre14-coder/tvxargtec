package com.tvxargtec.online

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import com.tvxargtec.online.utils.NotificationHelper

class TvxargtecApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        try {
            CastContext.getSharedInstance(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
