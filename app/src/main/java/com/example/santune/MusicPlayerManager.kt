package com.example.santune

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MusicPlayerManager(
    private val context: Context
) {

    private var controller: MediaController? = null

    private var controllerFuture:
            com.google.common.util.concurrent.ListenableFuture<MediaController>? =
        null

    private val scope =
        CoroutineScope(Dispatchers.Main)

    private var positionJob: Job? = null

    private var playlistSongs: List<Song> =
        emptyList()

    private val _currentSong =
        MutableStateFlow<Song?>(null)

    val currentSong:
            StateFlow<Song?> =
        _currentSong

    private val _currentPosition =
        MutableStateFlow(0L)

    val currentPosition:
            StateFlow<Long> =
        _currentPosition

    private val _duration =
        MutableStateFlow(0L)

    val duration:
            StateFlow<Long> =
        _duration

    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying:
            StateFlow<Boolean> =
        _isPlaying

    private val _queue =
        MutableStateFlow<List<Song>>(emptyList())

    val queue:
            StateFlow<List<Song>> =
        _queue

    private val _currentQueueIndex =
        MutableStateFlow(-1)

    val currentQueueIndex:
            StateFlow<Int> =
        _currentQueueIndex

    private val _shuffleEnabled =
        MutableStateFlow(false)

    val shuffleEnabled:
            StateFlow<Boolean> =
        _shuffleEnabled

    private val _repeatMode =
        MutableStateFlow(
            Player.REPEAT_MODE_OFF
        )

    val repeatMode:
            StateFlow<Int> =
        _repeatMode

    private val playerListener =
        object : Player.Listener {

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {

                _isPlaying.value =
                    isPlaying

                updatePosition()
                updateCurrentSongFromController()
                updateQueueFromController()
            }

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {

                updateCurrentSongFromController()
                updateQueueFromController()
                updatePosition()
            }

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {

                updatePosition()
                updateCurrentSongFromController()
                updateQueueFromController()
            }

            override fun onShuffleModeEnabledChanged(
                shuffleModeEnabled: Boolean
            ) {

                _shuffleEnabled.value =
                    shuffleModeEnabled

                updateQueueFromController()
            }

            override fun onRepeatModeChanged(
                repeatMode: Int
            ) {

                _repeatMode.value =
                    repeatMode
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {

                updatePosition()
                updateCurrentSongFromController()
            }
        }

    init {
        connectToPlaybackService()
    }

    private fun connectToPlaybackService() {

        val sessionToken =
            SessionToken(
                context,
                ComponentName(
                    context,
                    MusicPlaybackService::class.java
                )
            )

        controllerFuture =
            MediaController.Builder(
                context,
                sessionToken
            )
                .buildAsync()

        controllerFuture?.addListener(
            {

                try {

                    val mediaController =
                        controllerFuture?.get()
                            ?: return@addListener

                    controller =
                        mediaController

                    mediaController.addListener(
                        playerListener
                    )

                    updateCurrentSongFromController()
                    updateQueueFromController()
                    updatePosition()

                    startPositionUpdates()

                } catch (exception: Exception) {

                    exception.printStackTrace()
                }

            },
            MoreExecutors.directExecutor()
        )
    }

    fun updateSongs(
        songs: List<Song>
    ) {

        playlistSongs =
            songs

        updateQueueFromController()
        updateCurrentSongFromController()
    }

    /**
     * Creates artwork URI for Media3.
     *
     * If the song has real album artwork,
     * that artwork is used.
     *
     * If there is no artwork,
     * SanTune's default music artwork is used.
     */
    private fun getArtworkUri(
        song: Song
    ): Uri {

        if (
            !song.albumArtUri.isNullOrBlank()
        ) {

            return Uri.parse(
                song.albumArtUri
            )
        }

        return Uri.parse(
            "android.resource://${context.packageName}/${R.drawable.default_album_art}"
        )
    }

    private fun createMediaItem(
        song: Song
    ): MediaItem {

        val artworkUri =
            getArtworkUri(song)

        val metadata =
            MediaMetadata.Builder()
                .setTitle(
                    song.title
                )
                .setArtist(
                    song.artist
                )
                .setAlbumTitle(
                    song.album
                )
                .setArtworkUri(
                    artworkUri
                )
                .build()

        return MediaItem.Builder()
            .setMediaId(
                song.id.toString()
            )
            .setUri(
                song.uri
            )
            .setMediaMetadata(
                metadata
            )
            .build()
    }

    private fun findSongForMediaItem(
        mediaItem: MediaItem?
    ): Song? {

        if (mediaItem == null) {
            return null
        }

        val id =
            mediaItem.mediaId.toLongOrNull()

        if (id != null) {

            val songById =
                playlistSongs.firstOrNull {
                    it.id == id
                }

            if (songById != null) {
                return songById
            }
        }

        return playlistSongs.firstOrNull {

            it.uri ==
                    mediaItem
                        .localConfiguration
                        ?.uri
                        ?.toString()
        }
    }

    private fun updateCurrentSongFromController() {

        val mediaController =
            controller ?: return

        val song =
            findSongForMediaItem(
                mediaController.currentMediaItem
            )

        _currentSong.value =
            song

        _currentQueueIndex.value =
            mediaController.currentMediaItemIndex

        _duration.value =
            mediaController.duration
                .takeIf {
                    it > 0
                }
                ?: song?.duration
                        ?: 0L
    }

    private fun updateQueueFromController() {

        val mediaController =
            controller ?: return

        val itemCount =
            mediaController.mediaItemCount

        if (itemCount == 0) {

            _queue.value =
                emptyList()

            _currentQueueIndex.value =
                -1

            return
        }

        val songs =
            (0 until itemCount)
                .mapNotNull { index ->
                    findSongForMediaItem(
                        mediaController.getMediaItemAt(index)
                    )
                }

        _queue.value =
            songs

        _currentQueueIndex.value =
            mediaController.currentMediaItemIndex

        _shuffleEnabled.value =
            mediaController.shuffleModeEnabled

        _repeatMode.value =
            mediaController.repeatMode
    }

    private fun startPositionUpdates() {

        positionJob?.cancel()

        positionJob =
            scope.launch {

                while (isActive) {

                    updatePosition()

                    delay(500)
                }
            }
    }

    private fun updatePosition() {

        val mediaController =
            controller ?: return

        val position =
            mediaController.currentPosition

        val durationValue =
            mediaController.duration

        _currentPosition.value =
            position.coerceAtLeast(0L)

        if (durationValue > 0L) {

            _duration.value =
                durationValue
        }
    }

    fun play(
        song: Song,
        songs: List<Song>
    ) {

        val mediaController =
            controller ?: return

        val index =
            songs.indexOfFirst {
                it.id == song.id
            }

        if (index == -1) {
            return
        }

        playlistSongs =
            songs

        val mediaItems =
            songs.map {
                createMediaItem(it)
            }

        mediaController.setMediaItems(
            mediaItems
        )

        mediaController.prepare()

        mediaController.seekTo(
            index,
            0L
        )

        _currentSong.value =
            song

        _currentPosition.value =
            0L

        _currentQueueIndex.value =
            index

        _queue.value =
            songs

        mediaController.play()
    }

    fun addToQueue(
        song: Song
    ) {

        val mediaController =
            controller ?: return

        if (
            playlistSongs.none {
                it.id == song.id
            }
        ) {

            playlistSongs =
                playlistSongs + song
        }

        mediaController.addMediaItem(
            createMediaItem(song)
        )

        updateQueueFromController()
    }

    fun playNextSong(
        song: Song
    ) {

        val mediaController =
            controller ?: return

        if (
            mediaController.mediaItemCount == 0
        ) {

            playlistSongs =
                playlistSongs + song

            mediaController.setMediaItem(
                createMediaItem(song)
            )

            mediaController.prepare()
            mediaController.play()

            return
        }

        val currentIndex =
            mediaController.currentMediaItemIndex

        val insertIndex =
            if (currentIndex >= 0) {
                currentIndex + 1
            } else {
                0
            }

        if (
            playlistSongs.none {
                it.id == song.id
            }
        ) {

            playlistSongs =
                playlistSongs + song
        }

        mediaController.addMediaItem(
            insertIndex,
            createMediaItem(song)
        )

        updateQueueFromController()
    }

    fun removeFromQueue(
        index: Int
    ) {

        val mediaController =
            controller ?: return

        if (
            index < 0 ||
            index >= mediaController.mediaItemCount
        ) {
            return
        }

        mediaController.removeMediaItem(
            index
        )

        updateQueueFromController()
    }

    fun clearQueue() {

        val mediaController =
            controller ?: return

        mediaController.clearMediaItems()

        _queue.value =
            emptyList()

        _currentQueueIndex.value =
            -1

        _currentSong.value =
            null

        _currentPosition.value =
            0L

        _duration.value =
            0L
    }

    fun toggleShuffle() {

        val mediaController =
            controller ?: return

        mediaController.shuffleModeEnabled =
            !mediaController.shuffleModeEnabled

        _shuffleEnabled.value =
            mediaController.shuffleModeEnabled

        updateQueueFromController()
    }

    fun cycleRepeatMode() {

        val mediaController =
            controller ?: return

        val nextMode =
            when (
                mediaController.repeatMode
            ) {

                Player.REPEAT_MODE_OFF ->
                    Player.REPEAT_MODE_ONE

                Player.REPEAT_MODE_ONE ->
                    Player.REPEAT_MODE_ALL

                else ->
                    Player.REPEAT_MODE_OFF
            }

        mediaController.repeatMode =
            nextMode

        _repeatMode.value =
            nextMode
    }

    fun togglePlayPause() {

        val mediaController =
            controller ?: return

        if (
            mediaController.isPlaying
        ) {

            mediaController.pause()

        } else {

            mediaController.play()
        }
    }

    fun pause() {

        controller?.pause()
    }

    fun resume() {

        controller?.play()
    }

    fun playNext() {

        controller?.let {

            if (
                it.hasNextMediaItem()
            ) {

                it.seekToNextMediaItem()
                it.play()
            }
        }
    }

    fun playPrevious() {

        controller?.let {

            if (
                it.hasPreviousMediaItem()
            ) {

                it.seekToPreviousMediaItem()
                it.play()

            } else {

                it.seekTo(0L)
            }
        }
    }

    fun playQueueItem(
        index: Int
    ) {

        val mediaController =
            controller ?: return

        if (
            index < 0 ||
            index >= mediaController.mediaItemCount
        ) {
            return
        }

        mediaController.seekTo(
            index,
            0L
        )

        mediaController.play()
    }

    fun seekTo(
        position: Long
    ) {

        controller?.seekTo(
            position.coerceAtLeast(0L)
        )

        updatePosition()
    }

    fun stop() {

        controller?.stop()

        _currentPosition.value =
            0L

        _isPlaying.value =
            false
    }

    fun release() {

        positionJob?.cancel()

        controller?.removeListener(
            playerListener
        )

        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }

        controller =
            null

        controllerFuture =
            null

        scope.cancel()
    }
}