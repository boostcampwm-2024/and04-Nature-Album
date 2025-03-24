package com.and04.naturealbum.data.repository.local.impl

import com.and04.naturealbum.data.datasource.local.LocalAlbumDataSource
import com.and04.naturealbum.data.dto.AlbumDto
import com.and04.naturealbum.data.localdata.room.Album
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalAlbumRepositoryImpl @Inject constructor(
    private val localAlbumDataSource: LocalAlbumDataSource,
) : LocalAlbumRepository {
    override fun getAllAlbum(): Flow<List<AlbumDto>> {
        return localAlbumDataSource.getAllAlbum()
    }

    override suspend fun getAlbumByLabelId(labelId: Int): List<Album> {
        return localAlbumDataSource.getAlbumByLabelId(labelId)
    }

    override suspend fun updateAlbum(album: Album) {
        return localAlbumDataSource.updateAlbum(album)
    }

    override suspend fun updateAlbumPhotoDetailByAlbumId(photoDetailId: Int) {
        return localAlbumDataSource.updateAlbumPhotoDetailByAlbumId(photoDetailId)
    }

    override suspend fun insertPhotoInAlbum(album: Album): Long {
        return localAlbumDataSource.insertAlbum(album)
    }
}
