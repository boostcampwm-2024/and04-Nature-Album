package com.and04.naturealbum.data.repository.local.impl

import com.and04.naturealbum.data.datasource.local.LabelDataSource
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.data.repository.local.LabelRepository
import javax.inject.Inject

class LabelRepositoryImpl @Inject constructor(
    private val labelDataSource: LabelDataSource
) : LabelRepository {
    override suspend fun getLabels(): List<Label> {
        return labelDataSource.getAllLabel()
    }

    override suspend fun getLabelById(id: Int): Label {
        return labelDataSource.getLabelById(id)
    }

    override suspend fun insertLabel(label: Label): Long {
        return labelDataSource.insertLabel(label)
    }
}
