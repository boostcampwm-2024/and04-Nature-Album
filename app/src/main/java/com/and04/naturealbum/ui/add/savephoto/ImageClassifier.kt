package com.and04.naturealbum.ui.add.savephoto

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifier(private val assetManager: AssetManager) {
    private var interpreter: Interpreter
    private val labels: Map<Int, String>

    init {
        interpreter = Interpreter(loadModelFile())
        labels = loadLabels()
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(MODEL_PATH)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun loadLabels(): Map<Int, String> {
        val labelsMap = mutableMapOf<Int, String>()
        val inputStream = assetManager.open(LABEL_PATH)
        val jsonText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        val jsonObject = JSONObject(jsonText)

        jsonObject.keys().forEach { key ->
            labelsMap[key.toInt()] = jsonObject.getString(key)
        }
        return labelsMap
    }

    fun classify(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val outputBuffer = Array(BATCH_SIZE) { FloatArray(OUTPUT_CLASSES) }
        interpreter.run(inputBuffer, outputBuffer)

        val maxIdx = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1
        return labels[maxIdx] ?: UNKNOWN_LABEL
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        bitmap.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE)
        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    companion object {
        private const val MODEL_PATH = "mobilenet_v3_small.tflite"
        private const val LABEL_PATH = "imageNetLabels.json"
        private const val UNKNOWN_LABEL = "알 수 없음"
        private const val IMAGE_SIZE = 224
        private const val OUTPUT_CLASSES = 1000
        private const val BATCH_SIZE = 1
    }
}
