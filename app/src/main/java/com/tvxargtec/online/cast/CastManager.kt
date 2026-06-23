package com.tvxargtec.online.cast

import android.content.Context
import android.net.Uri
import android.view.MenuItem
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.tvxargtec.online.R

class CastManager(private val context: Context) {

    private var castContext: CastContext? = null
    private var remoteMediaClient: RemoteMediaClient? = null
    private var currentSession: CastSession? = null
    private var listener: CastListener? = null

    interface CastListener {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionFailed()
    }

    @Suppress("DEPRECATION")
    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            onSessionConnected(session)
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            onSessionConnected(session)
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            currentSession = null
            remoteMediaClient = null
            listener?.onDisconnected()
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            listener?.onConnectionFailed()
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    fun initialize() {
        castContext = CastContext.getSharedInstance(context)
        castContext?.sessionManager?.addSessionManagerListener(sessionListener, CastSession::class.java)
        currentSession = castContext?.sessionManager?.currentCastSession
        currentSession?.let { onSessionConnected(it) }
    }

    fun destroy() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionListener, CastSession::class.java)
    }

    fun isConnected(): Boolean {
        return currentSession?.isConnected == true
    }

    fun castVideo(
        videoUrl: String,
        title: String,
        subtitle: String = "",
        imageUrl: String = ""
    ) {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        metadata.putString(MediaMetadata.KEY_TITLE, title)
        metadata.putString(MediaMetadata.KEY_SUBTITLE, subtitle)
        if (imageUrl.isNotEmpty()) {
            metadata.addImage(WebImage(Uri.parse(imageUrl)))
        }

        val mediaInfo = MediaInfo.Builder(videoUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("application/x-mpegurl")
            .setMetadata(metadata)
            .build()

        val mediaLoadOptions = MediaLoadOptions.Builder()
            .setAutoplay(true)
            .build()

        remoteMediaClient?.load(mediaInfo, mediaLoadOptions)
    }

    fun setListener(listener: CastListener) {
        this.listener = listener
    }

    private fun onSessionConnected(session: CastSession) {
        currentSession = session
        remoteMediaClient = session.remoteMediaClient
        listener?.onConnected()
    }

    companion object {
        fun setupCastButton(context: Context, menu: android.view.Menu, menuItemId: Int) {
            CastButtonFactory.setUpMediaRouteButton(context, menu, menuItemId)
        }
    }
}
