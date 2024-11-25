package com.and04.naturealbum.ui.savephoto

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.and04.naturealbum.data.repository.DataRepository
import com.and04.naturealbum.data.room.Album
import com.and04.naturealbum.data.room.Label
import com.and04.naturealbum.data.room.Label.Companion.NEW_LABEL
import com.and04.naturealbum.data.room.PhotoDetail
import com.and04.naturealbum.ui.model.UiState
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import com.google.firebase.Firebase

@HiltViewModel
class SavePhotoViewModel @Inject constructor(
    private val repository: DataRepository,
) : ViewModel() {
    private val _geminiApiUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val geminiApiUiState: StateFlow<UiState<String>> = _geminiApiUiState

    private val _photoSaveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val photoSaveState: StateFlow<UiState<Unit>> = _photoSaveState

    fun savePhoto(
        uri: String,
        fileName: String,
        label: Label,
        location: Location,
        description: String,
        isRepresented: Boolean,
    ) {
        _photoSaveState.value = UiState.Loading // 로딩 시작

        viewModelScope.launch {
            try {
                val labelId =
                    if (label.id == NEW_LABEL) repository.insertLabel(label).toInt()
                    else label.id

                val album = async { repository.getAlbumByLabelId(labelId) }
                val photoDetailId = async {
                    repository.insertPhoto(
                        PhotoDetail(
                            labelId = labelId,
                            photoUri = uri,
                            fileName = fileName,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            description = description,
                            datetime = LocalDateTime.now(ZoneId.of("UTC")),
                        )
                    )
                }
                album.await().run {
                    if (isEmpty()) {
                        repository.insertPhotoInAlbum(
                            Album(
                                labelId = labelId,
                                photoDetailId = photoDetailId.await().toInt()
                            )
                        )
                    } else if (isRepresented) {
                        repository.updateAlbum(
                            first().copy(
                                photoDetailId = photoDetailId.await().toInt()
                            )
                        )
                    } else {
                    }
                }
                _photoSaveState.emit(UiState.Success(Unit)) // 저장 완료
            } catch (e: Exception) {
                Log.e("SavePhotoViewModel", "Error saving photo: ${e.message}")
                _photoSaveState.emit(UiState.Idle)
            }
        }
    }

    fun getGeneratedContent(bitmap: Bitmap?) {
        bitmap?.let { nonNullBitmap ->
            viewModelScope.launch {
                _geminiApiUiState.emit(UiState.Loading)
                val model = Firebase.vertexAI.generativeModel("gemini-1.5-pro")
                val content = content {
                    image(nonNullBitmap)
                    text("이 이미지는 어떤 생물인지 알려줘. 종까지 말해주면 좋아. 단 문장이 아닌, 제일 유사성이 높은 한 단어로만 알려줘")
                }

                val result = model.generateContent(content)
                _geminiApiUiState.emit(UiState.Success(result.text ?: ""))
            }
        }
    }
}
