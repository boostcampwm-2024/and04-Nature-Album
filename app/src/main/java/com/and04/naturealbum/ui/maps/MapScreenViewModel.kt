package com.and04.naturealbum.ui.maps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.and04.naturealbum.data.dto.FirebaseFriend
import com.and04.naturealbum.data.repository.firebase.AlbumRepository
import com.and04.naturealbum.data.repository.firebase.FriendRepository
import com.and04.naturealbum.data.repository.local.LabelRepository
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val photoDetailRepository: PhotoDetailRepository,
    private val albumRepository: AlbumRepository,
    private val friendRepository: FriendRepository,
    private val labelRepository: LabelRepository,
) : ViewModel() {
    private var myPhotos = listOf<PhotoItem>()
    private val _photosByUid = MutableStateFlow<Map<String, List<PhotoItem>>>(emptyMap())
    val photosByUid: StateFlow<Map<String, List<PhotoItem>>> = _photosByUid

    private val _friends = MutableStateFlow<List<FirebaseFriend>>(emptyList())
    val friends: StateFlow<List<FirebaseFriend>> = _friends

    init {
        viewModelScope.launch {
            val fetchPhotos = async { photoDetailRepository.getAllPhotoDetail() }
            val fetchLabels = labelRepository.getLabels()
            myPhotos = fetchPhotos.await().toPhotoItems(fetchLabels)
            _photosByUid.emit(mapOf("" to myPhotos))
        }
    }

    fun fetchFriendsPhotos(friends: List<String>) {
        viewModelScope.launch {
            try {
                val photos = async { albumRepository.getPhotos(friends) }
                val labels = albumRepository.getLabelsToMap(friends)
                _photosByUid.emit(
                    mapOf("" to myPhotos) +
                            photos.await()
                                .getOrThrow()
                                .mapValues { (uid, photos) ->
                                    photos.toFriendPhotoItems(labels.getOrThrow().getValue(uid))
                                }
                )

            } catch (e: Exception) {
                Log.e("MapScreenViewModel", e.toString())
            }
        }
    }

    fun fetchFriends(uid: String) {
        viewModelScope.launch {
            friendRepository.getFriendsAsFlow(uid).collect { friends ->
                _friends.value = friends
            }
        }
    }
}
