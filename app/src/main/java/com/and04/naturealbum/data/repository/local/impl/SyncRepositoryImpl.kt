package com.and04.naturealbum.data.repository.local.impl

import com.and04.naturealbum.data.datasource.local.LocalAlbumDataSource
import com.and04.naturealbum.data.datasource.local.LabelDataSource
import com.and04.naturealbum.data.datasource.local.PhotoDetailDataSource
import com.and04.naturealbum.data.dto.SyncAlbumsDto
import com.and04.naturealbum.data.dto.SyncPhotoDetailsDto
import com.and04.naturealbum.data.localdata.room.HazardAnalyzeStatus
import com.and04.naturealbum.data.repository.local.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val labelDataSource: LabelDataSource,
    private val localAlbumDataSource: LocalAlbumDataSource,
    private val photoDetailDataSource: PhotoDetailDataSource
) : SyncRepository {

    override suspend fun getIdByName(name: String): Int? {
        return labelDataSource.getIdByName(name)
    }

    override suspend fun getSyncCheckAlbums(): List<SyncAlbumsDto> {
        return localAlbumDataSource.getSyncCheckAlbums()
    }

    override suspend fun getSyncCheckPhotos(): List<SyncPhotoDetailsDto> {
        return localAlbumDataSource.getSyncCheckPhotos()
    }

    override suspend fun getHazardCheckResultByFileName(fileName: String): HazardAnalyzeStatus {
        return photoDetailDataSource.getHazardCheckResultByFileName(fileName)
    }

    override suspend fun updateHazardCheckResultByFIleName(
        hazardAnalyzeStatus: HazardAnalyzeStatus,
        fileName: String,
    ) {
        return photoDetailDataSource.updateHazardCheckResultByFIleName(
            hazardAnalyzeStatus,
            fileName
        )
    }
}
