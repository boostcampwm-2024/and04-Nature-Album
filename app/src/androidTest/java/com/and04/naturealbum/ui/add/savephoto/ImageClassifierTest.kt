package com.and04.naturealbum.ui.add.savephoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class ImageClassifierTest {
    private lateinit var classifier: ImageClassifier
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        classifier = ImageClassifier(context.assets)

        val labels = classifier.loadLabels()
        assertNotNull(labels)
        assertTrue(labels.isNotEmpty())

        Log.d("ImageClassifierTest", "라벨 JSON 로드 완료: ${labels.size}개 라벨")
    }

    @Test
    fun testSingleImageClassification() {
        val imagePath = "test/cat.jpg" // androidTest/assets/test/ 내 이미지 파일
        Log.d("ImageClassifierTest", "테스트 이미지 로드 시도: $imagePath")
        val bitmap = loadBitmapFromAssets(imagePath)

        assertNotNull("이미지를 로드하지 못했습니다: $imagePath", bitmap)

        val result = classifier.classify(bitmap!!)
        assertNotNull("예측 결과가 null입니다.", result)
        assertTrue("예측 결과가 비어 있습니다.", result.isNotEmpty())

        Log.d("ImageClassifierTest", "Single Image Test: $imagePath → Predicted Label: $result")
    }

    @Test
    fun testFolderImageClassification() {
        val folderPath = "test/" // androidTest/assets/test/ 폴더 내 모든 이미지 테스트
        val imageList = context.assets.list(folderPath) ?: emptyArray()

        assertTrue("테스트할 이미지가 없습니다.", imageList.isNotEmpty())

        for (imageName in imageList) {
            val imagePath = "$folderPath$imageName"
            Log.d("ImageClassifierTest", "이미지 테스트 시작: $imagePath")

            val bitmap = loadBitmapFromAssets(imagePath)
            assertNotNull("이미지를 로드하지 못했습니다: $imagePath", bitmap)

            val result = classifier.classify(bitmap!!)
            assertNotNull("예측 결과가 null입니다.", result)
            assertTrue("예측 결과가 비어 있습니다.", result.isNotEmpty())

            Log.d("ImageClassifierTest", "Folder Image Test: $imageName → Predicted Label: $result")
        }
    }

    private fun loadBitmapFromAssets(path: String): Bitmap? {
        return try {
            val inputStream: InputStream = context.assets.open(path)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("ImageClassifierTest", "이미지 로드 실패: $path", e)
            null
        }
    }
}
