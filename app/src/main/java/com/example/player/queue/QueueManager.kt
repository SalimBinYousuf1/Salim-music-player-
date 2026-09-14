package com.example.player.queue

import com.example.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections

class QueueManager {

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(0) // 0: off, 1: repeat one, 2: repeat all
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    val currentSong: Song?
        get() {
            val idx = _currentIndex.value
            val list = _queue.value
            return if (idx in list.indices) list[idx] else null
        }

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        _queue.value = songs
        _currentIndex.value = if (songs.isNotEmpty()) startIndex.coerceIn(0, songs.size - 1) else -1
    }

    fun playNext(song: Song) {
        val list = _queue.value.toMutableList()
        val curIdx = _currentIndex.value
        if (curIdx >= 0 && curIdx < list.size) {
            list.add(curIdx + 1, song)
        } else {
            list.add(song)
            if (_currentIndex.value == -1) _currentIndex.value = 0
        }
        _queue.value = list
    }

    fun addToQueue(song: Song) {
        val list = _queue.value.toMutableList()
        list.add(song)
        if (_currentIndex.value == -1) {
            _currentIndex.value = 0
        }
        _queue.value = list
    }

    fun addToQueue(songs: List<Song>) {
        val list = _queue.value.toMutableList()
        list.addAll(songs)
        if (_currentIndex.value == -1 && list.isNotEmpty()) {
            _currentIndex.value = 0
        }
        _queue.value = list
    }

    fun removeAt(index: Int) {
        val list = _queue.value.toMutableList()
        if (index in list.indices) {
            val removedCurrent = index == _currentIndex.value
            list.removeAt(index)
            _queue.value = list

            if (list.isEmpty()) {
                _currentIndex.value = -1
            } else if (removedCurrent) {
                _currentIndex.value = index.coerceIn(0, list.size - 1)
            } else if (index < _currentIndex.value) {
                _currentIndex.value = _currentIndex.value - 1
            }
        }
    }

    fun removeUpcoming() {
        val curIdx = _currentIndex.value
        val list = _queue.value
        if (curIdx in list.indices) {
            _queue.value = list.subList(0, curIdx + 1)
        }
    }

    fun reorder(fromIndex: Int, toIndex: Int) {
        val list = _queue.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val current = currentSong
            Collections.swap(list, fromIndex, toIndex)
            _queue.value = list
            if (current != null) {
                _currentIndex.value = list.indexOfFirst { it.id == current.id }
            }
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
    }

    fun moveToNext(): Song? {
        val list = _queue.value
        if (list.isEmpty()) return null

        if (_repeatMode.value == 1) { // Repeat one
            return currentSong
        }

        if (_isShuffle.value && list.size > 1) {
            var nextIdx = (list.indices).random()
            if (nextIdx == _currentIndex.value && list.size > 1) {
                nextIdx = (nextIdx + 1) % list.size
            }
            _currentIndex.value = nextIdx
            return list[nextIdx]
        }

        val nextIdx = _currentIndex.value + 1
        return if (nextIdx < list.size) {
            _currentIndex.value = nextIdx
            list[nextIdx]
        } else if (_repeatMode.value == 2) { // Repeat all
            _currentIndex.value = 0
            list[0]
        } else {
            null
        }
    }

    fun moveToPrevious(): Song? {
        val list = _queue.value
        if (list.isEmpty()) return null

        if (_repeatMode.value == 1) {
            return currentSong
        }

        val prevIdx = _currentIndex.value - 1
        return if (prevIdx >= 0) {
            _currentIndex.value = prevIdx
            list[prevIdx]
        } else if (_repeatMode.value == 2) {
            _currentIndex.value = list.size - 1
            list[list.size - 1]
        } else {
            _currentIndex.value = 0
            list[0]
        }
    }

    fun setShuffle(shuffle: Boolean) {
        _isShuffle.value = shuffle
    }

    fun setRepeatMode(mode: Int) {
        _repeatMode.value = mode.coerceIn(0, 2)
    }

    fun setCurrentIndex(index: Int): Song? {
        val list = _queue.value
        if (index in list.indices) {
            _currentIndex.value = index
            return list[index]
        }
        return null
    }
}
