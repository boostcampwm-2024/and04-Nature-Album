package com.and04.naturealbum.ui.add.savephoto

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object LabelLoader {
    private var labels: Map<Int, String>? = null

    fun loadLabels(context: Context) {
        if (labels == null) {
            labels = readJsonLabels(context)
        }
    }

    fun getLabels(): Map<Int, String> {
        return labels ?: emptyMap()
    }

    private fun readJsonLabels(context: Context): Map<Int, String> {
        val labelsMap = mutableMapOf<Int, String>()
        val inputStream = context.assets.open(LABEL_PATH)
        val jsonText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        val jsonObject = JSONObject(jsonText)

        jsonObject.keys().forEach { key ->
            labelsMap[key.toInt()] = jsonObject.getString(key)
        }
        return labelsMap
    }

    private const val LABEL_PATH = "imageNetLabels.json"
}
