package com.tvxargtec.online.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EpgHelper {

    @FunctionalInterface interface Callback {
        fun invoke(current: EpgProgramme?, next: EpgProgramme?)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    @JvmStatic fun fetchNowPlaying(channelName: String, callback: Callback) {
        scope.launch {
            val response = ApiService.getEpgNow(channelName)
            val data = response?.data?.firstOrNull()
            callback.invoke(data?.current, data?.next)
        }
    }
}
