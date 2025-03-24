package com.and04.naturealbum.data.repository.local.impl

import com.and04.naturealbum.data.datasource.local.LocalAlbumDataSource
import com.and04.naturealbum.data.datasource.local.PhotoDetailDataSource
import com.and04.naturealbum.data.localdata.room.Album
import com.and04.naturealbum.data.localdata.room.PhotoDetail
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import javax.inject.Inject

class PhotoDetailRepositoryImpl @Inject constructor(
    private val localAlbumDataSource: LocalAlbumDataSource,
    private val photoDetailDataSource: PhotoDetailDataSource,
) : PhotoDetailRepository {

    override suspend fun insertPhoto(photoDetail: PhotoDetail): Long {
        return photoDetailDataSource.insertPhotoDetail(photoDetail)
    }

    override suspend fun getAllPhotoDetail(): List<PhotoDetail> {
        return photoDetailDataSource.getAllPhotoDetail()
    }

    override suspend fun getPhotoDetailById(id: Int): PhotoDetail {
        return photoDetailDataSource.getPhotoDetailById(id)
    }

    override suspend fun getPhotoDetailsUriByLabelId(labelId: Int): List<PhotoDetail> {
        return photoDetailDataSource.getAllPhotoDetailsUriByLabelId(labelId)
    }

    override suspend fun deleteImage(photoDetail: PhotoDetail) {
        val album = localAlbumDataSource.getAlbumByLabelId(photoDetail.labelId).first()
        val isRepresentedImage = album.photoDetailId == photoDetail.id
        val nextRepresentedImage =
            photoDetailDataSource.getAllPhotoDetailsUriByLabelId(photoDetail.labelId)
                .firstOrNull { it != photoDetail }

        if (isRepresentedImage && nextRepresentedImage != null) {
            localAlbumDataSource.updateAlbum(
                Album(
                    id = album.id,
                    labelId = photoDetail.labelId,
                    photoDetailId = nextRepresentedImage.id
                )
            )
        }
        return photoDetailDataSource.deleteImage(photoDetail)
    }

    override suspend fun getAddressByPhotoDetailId(photoDetailId: Int): String {
        return photoDetailDataSource.getAddress(id = photoDetailId)
    }

    override suspend fun updateAddressByPhotoDetailId(address: String, photoDetailId: Int) {
        photoDetailDataSource.updateAddressById(address = address, id = photoDetailId)
    }
}
