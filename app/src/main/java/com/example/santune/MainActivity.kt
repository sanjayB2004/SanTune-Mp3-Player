package com.example.santune

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.santune.ui.theme.FavoriteColor
import com.example.santune.ui.theme.MusicGradientBottom
import com.example.santune.ui.theme.MusicGradientMidLower
import com.example.santune.ui.theme.MusicGradientMidUpper
import com.example.santune.ui.theme.MusicGradientTop
import com.example.santune.ui.theme.PlayButton
import com.example.santune.ui.theme.SanTuneTheme

// ============================================================
// SANTUNE UI COLORS
// ============================================================

private val WhiteText = Color.White
private val SecondaryWhite = Color.White.copy(alpha = 0.72f)
private val MutedWhite = Color.White.copy(alpha = 0.52f)

private val CardBackground =
    Color(0xFF202123).copy(alpha = 0.82f)

private val SoftButtonBackground =
    Color.White.copy(alpha = 0.10f)

private val SoftButtonPressed =
    Color.White.copy(alpha = 0.16f)

// ============================================================
// MINI PLAYER COLORS
// ============================================================

private val MiniPlayerWhite = Color(0xFFFFFFFF)
private val MiniPlayerText = Color(0xFF17191C)
private val MiniPlayerSecondary = Color(0xFF62666C)
private val MiniPlayerIconBackground = Color(0xFFF0F1F3)


// ============================================================
// MUSIC GRADIENT
// ============================================================

private fun sanTuneGradient(): Brush {

    return Brush.verticalGradient(
        colors = listOf(
            MusicGradientTop,
            MusicGradientMidUpper,
            MusicGradientMidLower,
            MusicGradientBottom
        )
    )
}


// ============================================================
// MAIN ACTIVITY
// ============================================================

class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()

    private lateinit var playerManager: MusicPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        playerManager = MusicPlayerManager(this)

        setContent {

            SanTuneTheme {

                val songs by musicViewModel.songs.collectAsState()

                val favoriteIds by
                musicViewModel.favoriteIds.collectAsState()

                val currentSong by
                playerManager.currentSong.collectAsState()

                val currentPosition by
                playerManager.currentPosition.collectAsState()

                val duration by
                playerManager.duration.collectAsState()

                val isPlaying by
                playerManager.isPlaying.collectAsState()

                val queue by
                playerManager.queue.collectAsState()

                val currentQueueIndex by
                playerManager.currentQueueIndex.collectAsState()

                val shuffleEnabled by
                playerManager.shuffleEnabled.collectAsState()

                val repeatMode by
                playerManager.repeatMode.collectAsState()

                LaunchedEffect(songs) {

                    if (songs.isNotEmpty()) {

                        playerManager.updateSongs(songs)
                    }
                }

                LaunchedEffect(Unit) {

                    if (
                        MusicPermission.hasPermission(
                            this@MainActivity
                        )
                    ) {

                        musicViewModel.loadSongs()

                        MusicPermission.requestNotificationPermission(
                            this@MainActivity
                        )

                    } else {

                        MusicPermission.requestPermission(
                            this@MainActivity
                        )
                    }
                }

                MusicLibraryScreen(

                    songs = songs,

                    favoriteIds = favoriteIds,

                    currentSong = currentSong,

                    currentPosition = currentPosition,

                    duration = duration,

                    isPlaying = isPlaying,

                    queue = queue,

                    currentQueueIndex = currentQueueIndex,

                    shuffleEnabled = shuffleEnabled,

                    repeatMode = repeatMode,

                    onPlaySong = { song ->

                        playerManager.play(
                            song,
                            songs
                        )
                    },

                    onAddToQueue = { song ->

                        playerManager.addToQueue(song)
                    },

                    onPlayNext = { song ->

                        playerManager.playNextSong(song)
                    },

                    onToggleFavorite = { songId ->

                        musicViewModel.toggleFavorite(songId)
                    },

                    onPlayPause = {

                        playerManager.togglePlayPause()
                    },

                    onPrevious = {

                        playerManager.playPrevious()
                    },

                    onNext = {

                        playerManager.playNext()
                    },

                    onSeek = { position ->

                        playerManager.seekTo(position)
                    },

                    onPlayQueueItem = { index ->

                        playerManager.playQueueItem(index)
                    },

                    onRemoveQueueItem = { index ->

                        playerManager.removeFromQueue(index)
                    },

                    onClearQueue = {

                        playerManager.clearQueue()
                    },

                    onShuffle = {

                        playerManager.toggleShuffle()
                    },

                    onRepeat = {

                        playerManager.cycleRepeatMode()
                    }
                )
            }
        }
    }

    override fun onDestroy() {

        if (::playerManager.isInitialized) {

            playerManager.release()
        }

        super.onDestroy()
    }
}


// ============================================================
// ALBUM ARTWORK
// ============================================================

@Composable
fun AlbumArtwork(
    artworkUri: String?,
    modifier: Modifier = Modifier
) {

    val defaultArtwork =
        painterResource(
            R.drawable.default_album_art
        )

    AsyncImage(
        model = artworkUri,
        contentDescription = "Album artwork",
        placeholder = defaultArtwork,
        error = defaultArtwork,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}


// ============================================================
// LIBRARY SCREEN
// ============================================================

@Composable
fun MusicLibraryScreen(
    songs: List<Song>,
    favoriteIds: Set<Long>,
    currentSong: Song?,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    queue: List<Song>,
    currentQueueIndex: Int,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onPlaySong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayQueueItem: (Int) -> Unit,
    onRemoveQueueItem: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {

    var selectedTab by remember {
        mutableStateOf(LibraryTab.SONGS)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedAlbum by remember {
        mutableStateOf<AlbumInfo?>(null)
    }

    var selectedArtist by remember {
        mutableStateOf<ArtistInfo?>(null)
    }

    var showNowPlaying by remember {
        mutableStateOf(false)
    }

    var showQueue by remember {
        mutableStateOf(false)
    }

    val filteredSongs =
        remember(songs, searchText) {

            if (searchText.isBlank()) {

                songs

            } else {

                songs.filter {

                    it.title.contains(
                        searchText,
                        ignoreCase = true
                    ) ||

                            it.artist.contains(
                                searchText,
                                ignoreCase = true
                            ) ||

                            it.album.contains(
                                searchText,
                                ignoreCase = true
                            )
                }
            }
        }

    val favoriteSongs =
        songs.filter {
            favoriteIds.contains(it.id)
        }

    // =========================================================
    // QUEUE
    // =========================================================

    if (showQueue) {

        QueueScreen(
            queue = queue,
            currentQueueIndex = currentQueueIndex,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            onShuffle = onShuffle,
            onRepeat = onRepeat,
            onBack = {
                showQueue = false
            },
            onPlaySong = onPlayQueueItem,
            onRemoveSong = onRemoveQueueItem,
            onClearQueue = onClearQueue
        )

        return
    }

    // =========================================================
    // NOW PLAYING
    // =========================================================

    if (
        showNowPlaying &&
        currentSong != null
    ) {

        NowPlayingScreen(
            song = currentSong,
            currentPosition = currentPosition,
            duration = duration,
            isPlaying = isPlaying,
            isFavorite =
                favoriteIds.contains(
                    currentSong.id
                ),

            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,

            onBackClick = {

                showNowPlaying = false
            },

            onPlayPauseClick = onPlayPause,

            onPreviousClick = onPrevious,

            onNextClick = onNext,

            onSeek = onSeek,

            onFavoriteClick = {

                onToggleFavorite(
                    currentSong.id
                )
            },

            onShuffle = onShuffle,

            onRepeat = onRepeat,

            onQueueClick = {

                showQueue = true
            }
        )

        return
    }

    // =========================================================
    // ALBUM DETAIL
    // =========================================================

    if (selectedAlbum != null) {

        AlbumDetailScreen(
            album = selectedAlbum!!,
            favoriteIds = favoriteIds,
            onBack = {
                selectedAlbum = null
            },
            onPlaySong = onPlaySong,
            onAddToQueue = onAddToQueue,
            onPlayNext = onPlayNext,
            onToggleFavorite = onToggleFavorite
        )

        return
    }

    // =========================================================
    // ARTIST DETAIL
    // =========================================================

    if (selectedArtist != null) {

        ArtistDetailScreen(
            artist = selectedArtist!!,
            favoriteIds = favoriteIds,
            onBack = {
                selectedArtist = null
            },
            onPlaySong = onPlaySong,
            onAddToQueue = onAddToQueue,
            onPlayNext = onPlayNext,
            onToggleFavorite = onToggleFavorite
        )

        return
    }

    // =========================================================
    // MAIN LIBRARY
    // =========================================================

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    sanTuneGradient()
                )
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // =================================================
                // HEADER
                // =================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 16.dp,
                            top = 18.dp,
                            bottom = 12.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        // =========================================
                        // STYLISH SANTUNE WORDMARK
                        // =========================================

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = "San",
                                fontSize = 32.sp,
                                fontWeight =
                                    FontWeight.ExtraBold,
                                fontStyle =
                                    FontStyle.Normal,
                                letterSpacing = (-1.2).sp,
                                color = WhiteText
                            )

                            Text(
                                text = "Tune",
                                fontSize = 32.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                fontStyle =
                                    FontStyle.Italic,
                                letterSpacing = (-1.4).sp,
                                color =
                                    Color.White.copy(
                                        alpha = 0.82f
                                    )
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )

                        // =========================================
                        // PREMIUM SUBTITLE
                        // =========================================

                        Text(
                            text =
                                "LISTEN  •  DISCOVER  •  ENJOY",
                            fontSize = 10.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color =
                                Color.White.copy(
                                    alpha = 0.60f
                                )
                        )
                    }

                    // =============================================
                    // STYLISH MUSIC SYMBOL
                    // =============================================

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                            .background(
                                Color.White.copy(
                                    alpha = 0.07f
                                )
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(
                                    RoundedCornerShape(
                                        15.dp
                                    )
                                )
                                .background(
                                    Color.White.copy(
                                        alpha = 0.08f
                                    )
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text = "𝄞",
                                fontSize = 29.sp,
                                fontWeight =
                                    FontWeight.Normal,
                                color = WhiteText
                            )
                        }
                    }
                }

                // =================================================
                // SEARCH
                // =================================================

                if (
                    selectedTab ==
                    LibraryTab.SONGS ||
                    selectedTab ==
                    LibraryTab.FAVORITES
                ) {

                    OutlinedTextField(

                        value = searchText,

                        onValueChange = {
                            searchText = it
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 6.dp
                            ),

                        singleLine = true,

                        shape =
                            RoundedCornerShape(20.dp),

                        placeholder = {

                            Text(
                                text =
                                    "Search songs, artists or albums",
                                color =
                                    SecondaryWhite
                            )
                        },

                        textStyle =
                            MaterialTheme
                                .typography
                                .bodyLarge
                                .copy(
                                    color = WhiteText
                                ),

                        colors =
                            androidx.compose.material3
                                .OutlinedTextFieldDefaults
                                .colors(
                                    focusedTextColor =
                                        WhiteText,

                                    unfocusedTextColor =
                                        WhiteText,

                                    focusedBorderColor =
                                        WhiteText.copy(
                                            alpha = 0.65f
                                        ),

                                    unfocusedBorderColor =
                                        WhiteText.copy(
                                            alpha = 0.28f
                                        ),

                                    cursorColor =
                                        WhiteText,

                                    focusedContainerColor =
                                        Color.White.copy(
                                            alpha = 0.06f
                                        ),

                                    unfocusedContainerColor =
                                        Color.White.copy(
                                            alpha = 0.05f
                                        )
                                )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                // =================================================
                // TABS
                // =================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 10.dp
                        ),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {

                    LibraryTabButton(
                        text = "Songs",
                        emoji = "🎵",
                        selected =
                            selectedTab ==
                                    LibraryTab.SONGS
                    ) {

                        selectedTab =
                            LibraryTab.SONGS
                    }

                    LibraryTabButton(
                        text = "Favorites",
                        emoji = "♥",
                        selected =
                            selectedTab ==
                                    LibraryTab.FAVORITES
                    ) {

                        selectedTab =
                            LibraryTab.FAVORITES
                    }

                    LibraryTabButton(
                        text = "Albums",
                        emoji = "💿",
                        selected =
                            selectedTab ==
                                    LibraryTab.ALBUMS
                    ) {

                        selectedTab =
                            LibraryTab.ALBUMS

                        searchText = ""
                    }

                    LibraryTabButton(
                        text = "Artists",
                        emoji = "👤",
                        selected =
                            selectedTab ==
                                    LibraryTab.ARTISTS
                    ) {

                        selectedTab =
                            LibraryTab.ARTISTS

                        searchText = ""
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                // =================================================
                // CONTENT
                // =================================================

                when (selectedTab) {

                    LibraryTab.SONGS -> {

                        SongListScreen(
                            songs = filteredSongs,
                            favoriteIds = favoriteIds,
                            onPlaySong = onPlaySong,
                            onAddToQueue = onAddToQueue,
                            onPlayNext = onPlayNext,
                            onToggleFavorite =
                                onToggleFavorite
                        )
                    }

                    LibraryTab.FAVORITES -> {

                        val displayedFavorites =
                            favoriteSongs.filter {

                                if (
                                    searchText.isBlank()
                                ) {

                                    true

                                } else {

                                    it.title.contains(
                                        searchText,
                                        ignoreCase = true
                                    ) ||

                                            it.artist.contains(
                                                searchText,
                                                ignoreCase = true
                                            ) ||

                                            it.album.contains(
                                                searchText,
                                                ignoreCase = true
                                            )
                                }
                            }

                        SongListScreen(
                            songs =
                                displayedFavorites,

                            favoriteIds =
                                favoriteIds,

                            onPlaySong =
                                onPlaySong,

                            onAddToQueue =
                                onAddToQueue,

                            onPlayNext =
                                onPlayNext,

                            onToggleFavorite =
                                onToggleFavorite
                        )
                    }

                    LibraryTab.ALBUMS -> {

                        AlbumListScreen(
                            songs = songs,
                            onAlbumClick = {
                                selectedAlbum = it
                            }
                        )
                    }

                    LibraryTab.ARTISTS -> {

                        ArtistListScreen(
                            songs = songs,
                            onArtistClick = {
                                selectedArtist = it
                            }
                        )
                    }
                }
            }

            // =================================================
            // MINI PLAYER
            // =================================================

            if (currentSong != null) {

                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,

                    onClick = {
                        showNowPlaying = true
                    },

                    onPlayPause =
                        onPlayPause,

                    onQueueClick = {
                        showQueue = true
                    },

                    modifier =
                        Modifier.align(
                            Alignment.BottomCenter
                        )
                )
            }
        }
    }
}


// ============================================================
// TAB BUTTON
// ============================================================

enum class LibraryTab {
    SONGS,
    FAVORITES,
    ALBUMS,
    ARTISTS
}

@Composable
fun LibraryTabButton(
    text: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(14.dp)
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = emoji,
            fontSize = 17.sp,
            color =
                if (selected) {
                    PlayButton
                } else {
                    WhiteText
                }
        )

        Spacer(
            modifier =
                Modifier.height(2.dp)
        )

        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            color =
                if (selected) {
                    WhiteText
                } else {
                    SecondaryWhite
                }
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Box(
            modifier = Modifier
                .width(
                    if (selected) {
                        26.dp
                    } else {
                        0.dp
                    }
                )
                .height(3.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    if (selected) {
                        PlayButton
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}


// ============================================================
// SONG LIST
// ============================================================

@Composable
fun SongListScreen(
    songs: List<Song>,
    favoriteIds: Set<Long>,
    onPlaySong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {

    if (songs.isEmpty()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),

            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🎵",
                    fontSize = 42.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = "No songs found",
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color = WhiteText
                )
            }
        }

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = 10.dp
            )
    ) {

        items(
            items = songs,
            key = { it.id }
        ) { song ->

            SongItem(
                song = song,

                isFavorite =
                    favoriteIds.contains(
                        song.id
                    ),

                onClick = {
                    onPlaySong(song)
                },

                onAddToQueue = {
                    onAddToQueue(song)
                },

                onPlayNext = {
                    onPlayNext(song)
                },

                onFavoriteClick = {
                    onToggleFavorite(song.id)
                }
            )
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(120.dp)
            )
        }
    }
}


// ============================================================
// SONG ITEM
// ============================================================

@Composable
fun SongItem(
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onFavoriteClick: () -> Unit
) {

    var showMenu by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            )
            .clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    CardBackground
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AlbumArtwork(
                artworkUri =
                    song.albumArtUri,

                modifier = Modifier
                    .size(64.dp)
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = song.title,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 15.sp,
                    color = WhiteText,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text = song.artist,
                    fontSize = 13.sp,
                    color = SecondaryWhite,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.album,
                    fontSize = 11.sp,
                    color = MutedWhite,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFavorite) {
                            FavoriteColor.copy(
                                alpha = 0.14f
                            )
                        } else {
                            SoftButtonBackground
                        }
                    )
                    .clickable {
                        onFavoriteClick()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (isFavorite) {
                            "♥"
                        } else {
                            "♡"
                        },

                    fontSize = 23.sp,

                    color =
                        if (isFavorite) {
                            FavoriteColor
                        } else {
                            WhiteText
                        }
                )
            }

            Box {

                IconButton(
                    onClick = {
                        showMenu = true
                    }
                ) {

                    Text(
                        text = "⋮",
                        fontSize = 24.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = WhiteText
                    )
                }

                DropdownMenu(
                    expanded = showMenu,

                    onDismissRequest = {
                        showMenu = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                "▶  Play",
                                color = WhiteText
                            )
                        },

                        onClick = {

                            showMenu = false
                            onClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "⏭  Play Next",
                                color = WhiteText
                            )
                        },

                        onClick = {

                            showMenu = false
                            onPlayNext()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "＋  Add to Queue",
                                color = WhiteText
                            )
                        },

                        onClick = {

                            showMenu = false
                            onAddToQueue()
                        }
                    )

                    DropdownMenuItem(
                        text = {

                            Text(
                                if (isFavorite) {
                                    "♥  Remove Favorite"
                                } else {
                                    "♡  Add to Favorites"
                                },

                                color = WhiteText
                            )
                        },

                        onClick = {

                            showMenu = false
                            onFavoriteClick()
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// ALBUM DATA
// ============================================================

data class AlbumInfo(
    val name: String,
    val artist: String,
    val artworkUri: String?,
    val songs: List<Song>
)

data class ArtistInfo(
    val name: String,
    val songs: List<Song>,
    val albumCount: Int
)


// ============================================================
// ALBUM LIST
// ============================================================

@Composable
fun AlbumListScreen(
    songs: List<Song>,
    onAlbumClick: (AlbumInfo) -> Unit
) {

    val albums =
        remember(songs) {

            songs
                .groupBy {
                    it.album.ifBlank {
                        "Unknown Album"
                    }
                }
                .map { (albumName, albumSongs) ->

                    AlbumInfo(
                        name = albumName,

                        artist =
                            albumSongs
                                .firstOrNull()
                                ?.artist
                                ?: "Unknown Artist",

                        artworkUri =
                            albumSongs
                                .firstOrNull()
                                ?.albumArtUri,

                        songs = albumSongs
                    )
                }
                .sortedBy {
                    it.name.lowercase()
                }
        }

    if (albums.isEmpty()) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = "No albums found",
                color = WhiteText
            )
        }

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = 10.dp
                )
    ) {

        items(
            items = albums,
            key = { it.name }
        ) { album ->

            AlbumItem(
                album = album,
                onClick = {
                    onAlbumClick(album)
                }
            )
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(120.dp)
            )
        }
    }
}


// ============================================================
// ALBUM ITEM
// ============================================================

@Composable
fun AlbumItem(
    album: AlbumInfo,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                )
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    CardBackground
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AlbumArtwork(
                artworkUri =
                    album.artworkUri,

                modifier =
                    Modifier
                        .size(74.dp)
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )

            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = album.name,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 17.sp,
                    color = WhiteText,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = album.artist,
                    fontSize = 14.sp,
                    color = SecondaryWhite,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        "${album.songs.size} songs",
                    fontSize = 12.sp,
                    color = MutedWhite
                )
            }
        }
    }
}


// ============================================================
// ARTIST LIST
// ============================================================

@Composable
fun ArtistListScreen(
    songs: List<Song>,
    onArtistClick: (ArtistInfo) -> Unit
) {

    val artists =
        remember(songs) {

            songs
                .groupBy {
                    it.artist.ifBlank {
                        "Unknown Artist"
                    }
                }
                .map { (artistName, artistSongs) ->

                    ArtistInfo(
                        name = artistName,

                        songs = artistSongs,

                        albumCount =
                            artistSongs
                                .map {
                                    it.album
                                }
                                .distinct()
                                .size
                    )
                }
                .sortedBy {
                    it.name.lowercase()
                }
        }

    if (artists.isEmpty()) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = "No artists found",
                color = WhiteText
            )
        }

        return
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = 10.dp
                )
    ) {

        items(
            items = artists,
            key = { it.name }
        ) { artist ->

            ArtistItem(
                artist = artist,
                onClick = {
                    onArtistClick(artist)
                }
            )
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(120.dp)
            )
        }
    }
}


// ============================================================
// ARTIST ITEM
// ============================================================

@Composable
fun ArtistItem(
    artist: ArtistInfo,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                )
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    CardBackground
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(
                            SoftButtonBackground
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        artist.name
                            .take(1)
                            .uppercase(),

                    fontSize = 25.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = WhiteText
                )
            }

            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )

            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = artist.name,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 17.sp,
                    color = WhiteText,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        "${artist.songs.size} songs • " +
                                "${artist.albumCount} albums",

                    fontSize = 13.sp,

                    color = SecondaryWhite
                )
            }
        }
    }
}


// ============================================================
// ALBUM DETAIL
// ============================================================

@Composable
fun AlbumDetailScreen(
    album: AlbumInfo,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                sanTuneGradient()
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← Back",
                    color = WhiteText
                )
            }

            Text(
                text = album.name,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold,
                color = WhiteText,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                modifier =
                    Modifier.fillMaxWidth(
                        0.75f
                    )
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AlbumArtwork(
                artworkUri =
                    album.artworkUri,

                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = album.name,
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color = WhiteText,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = album.artist,
                    color = SecondaryWhite
                )

                Text(
                    text =
                        "${album.songs.size} songs",
                    color = MutedWhite
                )
            }
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
        ) {

            items(
                items = album.songs,
                key = { it.id }
            ) { song ->

                SongItem(
                    song = song,

                    isFavorite =
                        favoriteIds.contains(
                            song.id
                        ),

                    onClick = {
                        onPlaySong(song)
                    },

                    onAddToQueue = {
                        onAddToQueue(song)
                    },

                    onPlayNext = {
                        onPlayNext(song)
                    },

                    onFavoriteClick = {
                        onToggleFavorite(
                            song.id
                        )
                    }
                )
            }
        }
    }
}


// ============================================================
// ARTIST DETAIL
// ============================================================

@Composable
fun ArtistDetailScreen(
    artist: ArtistInfo,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    sanTuneGradient()
                )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← Back",
                    color = WhiteText
                )
            }

            Text(
                text = artist.name,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold,
                color = WhiteText,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                modifier =
                    Modifier.fillMaxWidth(
                        0.75f
                    )
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                SoftButtonBackground
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            artist.name
                                .take(1)
                                .uppercase(),

                        fontSize = 42.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color = WhiteText
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text = artist.name,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color = WhiteText
                )

                Text(
                    text =
                        "${artist.songs.size} songs • " +
                                "${artist.albumCount} albums",

                    fontSize = 14.sp,

                    color = SecondaryWhite
                )
            }
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
        ) {

            items(
                items = artist.songs,
                key = { it.id }
            ) { song ->

                SongItem(
                    song = song,

                    isFavorite =
                        favoriteIds.contains(
                            song.id
                        ),

                    onClick = {
                        onPlaySong(song)
                    },

                    onAddToQueue = {
                        onAddToQueue(song)
                    },

                    onPlayNext = {
                        onPlayNext(song)
                    },

                    onFavoriteClick = {
                        onToggleFavorite(
                            song.id
                        )
                    }
                )
            }
        }
    }
}


// ============================================================
// MINI PLAYER
// ============================================================

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
            .clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(22.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),

        // =====================================================
        // PURE WHITE MINI PLAYER
        // =====================================================

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MiniPlayerWhite
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(9.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AlbumArtwork(
                artworkUri =
                    song.albumArtUri,

                modifier =
                    Modifier
                        .size(58.dp)
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = song.title,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 14.sp,

                    // DARK TEXT ON WHITE
                    color =
                        MiniPlayerText,

                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    fontSize = 12.sp,

                    // DARK GREY SECONDARY TEXT
                    color =
                        MiniPlayerSecondary,

                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            // =================================================
            // QUEUE BUTTON
            // =================================================

            IconButton(
                onClick =
                    onQueueClick
            ) {

                Text(
                    text = "☷",
                    fontSize = 24.sp,

                    // DARK ICON
                    color =
                        MiniPlayerText,

                    fontWeight =
                        FontWeight.Medium
                )
            }

            // =================================================
            // PLAY / PAUSE BUTTON
            // =================================================

            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            PlayButton
                        )
                        .clickable {
                            onPlayPause()
                        },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (isPlaying) {
                            "⏸"
                        } else {
                            "▶"
                        },

                    fontSize = 17.sp,

                    // WHITE ICON ON BLUE BUTTON
                    color = WhiteText,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )
        }
    }
}


// ============================================================
// QUEUE SCREEN
// ============================================================

@Composable
fun QueueScreen(
    queue: List<Song>,
    currentQueueIndex: Int,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onBack: () -> Unit,
    onPlaySong: (Int) -> Unit,
    onRemoveSong: (Int) -> Unit,
    onClearQueue: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    sanTuneGradient()
                )
    ) {

        // =====================================================
        // TOP BAR
        // =====================================================

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick =
                    onBack
            ) {

                Text(
                    text = "← Back",
                    color = WhiteText
                )
            }

            Text(
                text = "Up Next",
                fontSize = 22.sp,
                fontWeight =
                    FontWeight.Bold,
                color = WhiteText,
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            )
        }

        // =====================================================
        // SHUFFLE / REPEAT
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        if (shuffleEnabled) {
                            PlayButton.copy(
                                alpha = 0.22f
                            )
                        } else {
                            SoftButtonBackground
                        }
                    )
                    .clickable {
                        onShuffle()
                    }
                    .padding(
                        vertical = 12.dp
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (shuffleEnabled) {
                            "🔀  Shuffle On"
                        } else {
                            "🔀  Shuffle"
                        },

                    fontWeight =
                        FontWeight.SemiBold,

                    color = WhiteText
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        if (
                            repeatMode !=
                            Player.REPEAT_MODE_OFF
                        ) {

                            PlayButton.copy(
                                alpha = 0.22f
                            )

                        } else {

                            SoftButtonBackground
                        }
                    )
                    .clickable {
                        onRepeat()
                    }
                    .padding(
                        vertical = 12.dp
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        when (repeatMode) {

                            Player.REPEAT_MODE_ONE ->
                                "🔂  Repeat One"

                            Player.REPEAT_MODE_ALL ->
                                "🔁  Repeat All"

                            else ->
                                "🔁  Repeat"
                        },

                    fontWeight =
                        FontWeight.SemiBold,

                    color = WhiteText
                )
            }
        }

        // =====================================================
        // EMPTY QUEUE
        // =====================================================

        if (queue.isEmpty()) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "Queue is empty",
                    fontSize = 18.sp,
                    color = WhiteText
                )
            }

            return
        }

        // =====================================================
        // QUEUE LIST
        // =====================================================

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
        ) {

            items(
                items = queue,
                key = { song ->

                    "${song.id}_${queue.indexOf(song)}"
                }
            ) { song ->

                val index =
                    queue.indexOf(song)

                val isCurrent =
                    index ==
                            currentQueueIndex

                QueueSongItem(
                    song = song,
                    isCurrent = isCurrent,

                    onClick = {
                        onPlaySong(index)
                    },

                    onRemove = {

                        if (!isCurrent) {

                            onRemoveSong(index)
                        }
                    }
                )
            }
        }

        // =====================================================
        // CLEAR QUEUE
        // =====================================================

        TextButton(
            onClick =
                onClearQueue,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
        ) {

            Text(
                text = "Clear Queue",
                color =
                    FavoriteColor,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ============================================================
// QUEUE SONG ITEM
// ============================================================

@Composable
fun QueueSongItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 4.dp
                )
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isCurrent) {

                        PlayButton.copy(
                            alpha = 0.20f
                        )

                    } else {

                        CardBackground
                    }
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AlbumArtwork(
                artworkUri =
                    song.albumArtUri,

                modifier =
                    Modifier
                        .size(58.dp)
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        if (isCurrent) {

                            "▶ ${song.title}"

                        } else {

                            song.title
                        },

                    fontWeight =
                        if (isCurrent) {

                            FontWeight.Bold

                        } else {

                            FontWeight.Normal
                        },

                    color = WhiteText,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    fontSize = 13.sp,
                    color = SecondaryWhite,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.album,
                    fontSize = 11.sp,
                    color = MutedWhite,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            if (!isCurrent) {

                TextButton(
                    onClick =
                        onRemove
                ) {

                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        color = WhiteText
                    )
                }
            }
        }
    }
}


// ============================================================
// TIME FORMAT
// ============================================================

fun formatTime(
    milliseconds: Long
): String {

    val totalSeconds =
        milliseconds / 1000

    val minutes =
        totalSeconds / 60

    val seconds =
        totalSeconds % 60

    return String.format(
        "%d:%02d",
        minutes,
        seconds
    )
}