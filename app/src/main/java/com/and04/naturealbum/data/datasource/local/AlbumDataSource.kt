package com.and04.naturealbum.data.datasource.local

import com.and04.naturealbum.data.dto.AlbumDto
import com.and04.naturealbum.data.localdata.room.Album
import com.and04.naturealbum.data.localdata.room.AlbumDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlbumDataSource @Inject constructor(
    private val albumDao: AlbumDao
) {
    fun getAllAlbum(): Flow<List<AlbumDto>> {
        return albumDao.getAllAlbum()
    }

    suspend fun getAlbumByLabelId(labelId: Int): List<Album> {
        return albumDao.getAlbumByLabelId(labelId)
    }

    suspend fun updateAlbum(album: Album) {
        return albumDao.updateAlbum(album)
    }

    suspend fun updateAlbumPhotoDetailByAlbumId(photoDetailId: Int) {
        return albumDao.updateAlbumPhotoDetailByAlbumId(photoDetailId)
    }

    suspend fun insertAlbum(album: Album): Long {
        return albumDao.insertAlbum(album)
    }
}
