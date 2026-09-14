package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SongDao
import com.example.domain.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var songDao: SongDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    songDao = db.songDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Salim Music Player", appName)
  }

  @Test
  fun `insert and retrieve song from database`() = runBlocking {
    val song = Song(
      title = "Clair de Lune",
      artist = "Claude Debussy",
      album = "Suite Bergamasque",
      duration = 300000L,
      fileUri = "content://media/external/audio/media/101"
    )
    val insertedId = songDao.insertSong(song)
    val retrieved = songDao.getSongByIdDirect(insertedId)

    assertNotNull(retrieved)
    assertEquals("Clair de Lune", retrieved?.title)
    assertEquals("Claude Debussy", retrieved?.artist)
  }
}

