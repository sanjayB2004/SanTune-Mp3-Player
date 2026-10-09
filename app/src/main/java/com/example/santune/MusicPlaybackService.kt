package com.example.santune

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicPlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var pausedForAudioFocus = false

    private val audioFocusListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->

            when (focusChange) {

                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

                    if (player.isPlaying) {
                        player.pause()
                        pausedForAudioFocus = true
                    }
                }

                AudioManager.AUDIOFOCUS_GAIN -> {

                    if (pausedForAudioFocus) {
                        pausedForAudioFocus = false
                        player.play()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {

                    // SanTune pauses instead of lowering volume.
                    if (player.isPlaying) {
                        player.pause()
                        pausedForAudioFocus = true
                    }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        // ---------------------------------------------------------
        // AUDIO
        // ---------------------------------------------------------

        val audioAttributes =
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(
                    C.AUDIO_CONTENT_TYPE_MUSIC
                )
                .build()

        player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(
                    audioAttributes,
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()

        // ---------------------------------------------------------
        // OPEN SAN TUNE WHEN NOTIFICATION IS TAPPED
        // ---------------------------------------------------------

        val intent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        // ---------------------------------------------------------
        // MEDIA SESSION
        //
        // Media3 automatically creates the media notification
        // from this session.
        //
        // Album artwork comes from:
        //
        // MediaItem
        //      ↓
        // MediaMetadata
        //      ↓
        // setArtworkUri(...)
        //
        // Your MusicPlayerManager already provides that.
        // ---------------------------------------------------------

        mediaSession =
            MediaSession.Builder(
                this,
                player
            )
                .setSessionActivity(
                    pendingIntent
                )
                .build()
    }

    // ---------------------------------------------------------
    // PROVIDE MEDIA SESSION TO ANDROID
    // ---------------------------------------------------------

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession {

        return mediaSession
    }

    // ---------------------------------------------------------
    // APP REMOVED FROM RECENTS
    // ---------------------------------------------------------

    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {

        if (!player.isPlaying) {
            stopSelf()
        }

        super.onTaskRemoved(rootIntent)
    }

    // ---------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------

    override fun onDestroy() {

        pausedForAudioFocus = false

        mediaSession.release()

        player.release()

        super.onDestroy()
    }
}