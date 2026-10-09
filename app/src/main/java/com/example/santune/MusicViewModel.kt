package com.example.santune

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    val songs: StateFlow<List<Song>> = _songs

    private val preferences =
        application.getSharedPreferences(
            "santune_preferences",
            Context.MODE_PRIVATE
        )

    private val _favoriteIds =
        MutableStateFlow<Set<Long>>(
            loadFavoriteIds()
        )

    val favoriteIds: StateFlow<Set<Long>> =
        _favoriteIds

    private fun loadFavoriteIds(): Set<Long> {

        val savedIds =
            preferences.getStringSet(
                "favorite_song_ids",
                emptySet()
            ) ?: emptySet()

        return savedIds.mapNotNull {
            it.toLongOrNull()
        }.toSet()
    }

    fun loadSongs() {

        viewModelScope.launch(Dispatchers.IO) {

            val music =
                repository.getSongs()

            _songs.value = music
        }
    }

    fun toggleFavorite(songId: Long) {

        val currentFavorites =
            _favoriteIds.value.toMutableSet()

        if (currentFavorites.contains(songId)) {

            currentFavorites.remove(songId)

        } else {

            currentFavorites.add(songId)
        }

        _favoriteIds.value =
            currentFavorites

        preferences.edit()
            .putStringSet(
                "favorite_song_ids",
                currentFavorites
                    .map { it.toString() }
                    .toSet()
            )
            .apply()
    }

    fun isFavorite(songId: Long): Boolean {

        return _favoriteIds.value.contains(songId)
    }
}