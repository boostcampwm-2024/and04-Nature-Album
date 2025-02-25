package com.and04.naturealbum.data.datasource.local

import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.data.localdata.room.LabelDao
import javax.inject.Inject

class LabelDataSource @Inject constructor(
    private val labelDao: LabelDao
) {
    suspend fun getAllLabel(): List<Label> {
        return labelDao.getAllLabel()
    }

    suspend fun getLabelById(id: Int): Label {
        return labelDao.getLabelById(id)
    }

    suspend fun insertLabel(label: Label): Long {
        return labelDao.insertLabel(label)
    }

    suspend fun getIdByName(name: String): Int? {
        return labelDao.getIdByName(name)
    }
}
