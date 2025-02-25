package com.and04.naturealbum.data.datasource.local

import com.and04.naturealbum.data.localdata.room.PhotoDetail
import com.and04.naturealbum.data.localdata.room.PhotoDetailDao
import javax.inject.Inject

class PhotoDetailDataSource @Inject constructor(
    private val photoDetailDao: PhotoDetailDao,
) {
    suspend fun insertPhotoDetail(photoDetail: PhotoDetail): Long {
        return photoDetailDao.insertPhotoDetail(photoDetail)
    }

    suspend fun getAllPhotoDetail(): List<PhotoDetail> {
        return photoDetailDao.getAllPhotoDetail()
    }

    suspend fun getPhotoDetailById(id: Int): PhotoDetail {
        return photoDetailDao.getPhotoDetailById(id)
    }

    suspend fun getAllPhotoDetailsUriByLabelId(labelId: Int): List<PhotoDetail> {
        return photoDetailDao.getAllPhotoDetailsUriByLabelId(labelId)
    }

    suspend fun getAddress(id: Int): String {
        return photoDetailDao.getAddress(id = id)
    }

    suspend fun updateAddressById(address: String, id: Int) {
        photoDetailDao.updateAddressById(address = address, id = id)
    }

    suspend fun deleteImage(photoDetail: PhotoDetail) {
        photoDetailDao.deleteImage(photoDetail)
    }
}
