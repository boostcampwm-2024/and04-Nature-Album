package com.and04.naturealbum.sync

import com.and04.naturealbum.background.workmanager.binarySearch
import com.and04.naturealbum.data.dto.FirebaseLabelResponse
import com.and04.naturealbum.data.dto.SyncAlbumsDto
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class BinarySearchTest {

    @Test
    fun 선형탐색과_이진탐색_속도_측정() {
        val countArr = arrayOf(1000, 10000, 50000)

        countArr.forEach {
            val local = getSyncAlbumDtoDummyData(it)
            val firebase = getFirebaseLabelResponseDummyData(it)

            val linearSearchTime = checkLinearSearchTime(firebase, local)
            val binarySearchTime = checkBinarySearchTime(firebase, local)

            println("Count: $it")
            println("LinearSearch: $linearSearchTime")
            println("BinarySearch: $binarySearchTime")

            assertTrue(linearSearchTime > binarySearchTime)
        }
    }

    private fun checkLinearSearchTime(
        firebase: List<FirebaseLabelResponse>,
        local: List<SyncAlbumsDto>
    ): Long = measureTimeMillis {
        firebase.forEach { f ->
            for (data in local) {
                if (data.labelName == f.labelName) break
            }
        }
    }

    private fun checkBinarySearchTime(
        firebase: List<FirebaseLabelResponse>,
        local: List<SyncAlbumsDto>
    ): Long = measureTimeMillis {
        val sortedLocal = local.sortedBy { it.labelName }

        firebase.forEach { f ->
            sortedLocal.binarySearch(target = f.labelName)
        }
    }

    private fun getSyncAlbumDtoDummyData(count: Int): List<SyncAlbumsDto> {
        val dummyList = mutableListOf<SyncAlbumsDto>()

        repeat(count) {
            dummyList.add(
                SyncAlbumsDto(
                    labelId = 0,
                    labelName = getRandomString(),
                    labelBackgroundColor = "",
                    photoDetailUri = "",
                    fileName = ""
                )
            )
        }

        return dummyList
    }

    private fun getFirebaseLabelResponseDummyData(count: Int): List<FirebaseLabelResponse> {
        val dummyList = mutableListOf<FirebaseLabelResponse>()

        repeat(count) {
            dummyList.add(
                FirebaseLabelResponse(
                    labelName = getRandomString(),
                    backgroundColor = "",
                    thumbnailUri = "",
                    fileName = ""
                )
            )
        }
        return dummyList
    }

    private fun getRandomString(): String {
        val charset = ('a'..'z') + ('0'..'9')
        return (0..(3..10).random())
            .map { charset.random() }
            .joinToString("")
    }
}
