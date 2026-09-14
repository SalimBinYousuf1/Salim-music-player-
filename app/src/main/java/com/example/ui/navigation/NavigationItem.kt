package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Songs : Screen("songs", "Songs", Icons.Default.MusicNote)
    object Albums : Screen("albums", "Albums", Icons.Default.Album)
    object Artists : Screen("artists", "Artists", Icons.Default.Person)
    object Genres : Screen("genres", "Genres", Icons.Default.Category)
    object Playlists : Screen("playlists", "Playlists", Icons.Default.PlaylistPlay)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object RecentlyPlayed : Screen("recently_played", "Recent", Icons.Default.History)
    object MostPlayed : Screen("most_played", "Top Played", Icons.Default.TrendingUp)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Queue : Screen("queue", "Queue", Icons.AutoMirrored.Filled.QueueMusic)
    object Lyrics : Screen("lyrics", "Lyrics", Icons.Default.Subtitles)
    object Equalizer : Screen("equalizer", "Equalizer", Icons.Default.Tune)
    object Visualizer : Screen("visualizer", "Visualizer", Icons.Default.GraphicEq)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)

    // Detail screens
    object NowPlaying : Screen("now_playing", "Now Playing", Icons.Default.PlayCircle)
    object PlaylistDetail : Screen("playlist_detail/{playlistId}", "Playlist", Icons.Default.PlaylistPlay) {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
    object AlbumDetail : Screen("album_detail/{albumName}", "Album", Icons.Default.Album) {
        fun createRoute(albumName: String) = "album_detail/${android.net.Uri.encode(albumName)}"
    }
    object ArtistDetail : Screen("artist_detail/{artistName}", "Artist", Icons.Default.Person) {
        fun createRoute(artistName: String) = "artist_detail/${android.net.Uri.encode(artistName)}"
    }
}

val BottomNavScreens = listOf(
    Screen.Home,
    Screen.Songs,
    Screen.Playlists,
    Screen.Search,
    Screen.Settings
)

val DrawerScreens = listOf(
    Screen.Home,
    Screen.Songs,
    Screen.Albums,
    Screen.Artists,
    Screen.Genres,
    Screen.Playlists,
    Screen.Favorites,
    Screen.RecentlyPlayed,
    Screen.MostPlayed,
    Screen.Queue,
    Screen.Lyrics,
    Screen.Equalizer,
    Screen.Visualizer,
    Screen.Settings,
    Screen.About
)
