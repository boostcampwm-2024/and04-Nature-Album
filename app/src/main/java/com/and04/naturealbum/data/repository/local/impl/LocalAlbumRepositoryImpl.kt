package com.and04.naturealbum.data.repository.local.impl

import com.and04.naturealbum.data.datasource.local.AlbumDataSource
import com.and04.naturealbum.data.dto.AlbumDto
import com.and04.naturealbum.data.localdata.room.Album
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalAlbumRepositoryImpl @Inject constructor(
    private val albumDataSource: AlbumDataSource,
) : LocalAlbumRepository {
    override fun getAllAlbum(): Flow<List<AlbumDto>> {
        return albumDataSource.getAllAlbum()
    }

    override suspend fun getAlbumByLabelId(labelId: Int): List<Album> {
        return albumDataSource.getAlbumByLabelId(labelId)
    }

    override suspend fun updateAlbum(album: Album) {
        return albumDataSource.updateAlbum(album)
    }

    override suspend fun updateAlbumPhotoDetailByAlbumId(photoDetailId: Int) {
        return albumDataSource.updateAlbumPhotoDetailByAlbumId(photoDetailId)
    }

    override suspend fun insertPhotoInAlbum(album: Album): Long {
        return albumDataSource.insertAlbum(album)
    }
}
