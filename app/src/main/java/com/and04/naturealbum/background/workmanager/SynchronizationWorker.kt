package com.and04.naturealbum.background.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.and04.naturealbum.data.localdata.datastore.DataStoreManager
import com.and04.naturealbum.data.dto.FirebaseLabel
import com.and04.naturealbum.data.dto.FirebaseLabelResponse
import com.and04.naturealbum.data.dto.FirebasePhotoInfo
import com.and04.naturealbum.data.dto.FirebasePhotoInfoResponse
import com.and04.naturealbum.data.dto.SyncAlbumsDto
import com.and04.naturealbum.data.dto.SyncPhotoDetailsDto
import com.and04.naturealbum.data.localdata.room.Album
import com.and04.naturealbum.data.localdata.room.HazardAnalyzeStatus
import com.and04.naturealbum.data.localdata.room.Label
import com.and04.naturealbum.data.localdata.room.PhotoDetail
import com.and04.naturealbum.data.repository.RetrofitRepository
import com.and04.naturealbum.data.repository.firebase.AlbumRepository
import com.and04.naturealbum.data.repository.local.LabelRepository
import com.and04.naturealbum.data.repository.local.LocalAlbumRepository
import com.and04.naturealbum.data.repository.local.PhotoDetailRepository
import com.and04.naturealbum.data.repository.local.SyncRepository
import com.and04.naturealbum.ui.utils.UserManager
import com.and04.naturealbum.utils.image.ImageConvert
import com.and04.naturealbum.utils.time.toDateTimeString
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@HiltWorker
class SynchronizationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val photoDetailRepository: PhotoDetailRepository,
    private val albumRepository: AlbumRepository,
    private val syncDataStore: DataStoreManager,
    private val retrofitRepository: RetrofitRepository,
    private val syncRepository: SyncRepository,
    private val localAlbumRepository: LocalAlbumRepository,
    private val labelRepository: LabelRepository,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private var IS_RUNNING = false
        private const val WORKER_NAME = "WORKER_SYNCHRONIZATION"
        private const val HOUR = 0
        private const val MINUTE = 0

        fun isWorking() = IS_RUNNING

        fun runSync(context: Context) {
            val duration = getDurationTime()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SynchronizationWorker>()
                .setInitialDelay(duration.seconds, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORKER_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun runImmediately(context: Context) {
            if (!IS_RUNNING) {
                val workRequest = OneTimeWorkRequestBuilder<SynchronizationWorker>()
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        WORKER_NAME,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
            }
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORKER_NAME)
        }

        private fun getDurationTime(): Duration {
            val triggerHour = HOUR
            val triggerMinute = MINUTE

            val newSyncTime = LocalTime.of(triggerHour, triggerMinute)
            val now: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val nowTime: LocalTime = now.toLocalTime()

            val plusDay = if (nowTime == newSyncTime || nowTime.isAfter(newSyncTime)) 1 else 0

            val nextTriggerTime = now.plusDays(plusDay.toLong())
                .withHour(newSyncTime.hour)
                .withMinute(newSyncTime.minute)

            return Duration.between(LocalDateTime.now(), nextTriggerTime)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val currentUser = Firebase.auth.currentUser ?: return@coroutineScope Result.failure()
            val uid = currentUser.uid
            IS_RUNNING = true

            val unSynchronizedPhotoDetailsToLocal = mutableListOf<FirebasePhotoInfoResponse>()
            // key LabelName, value (label_id to labelName)
            val fileNameToLabelUid = HashMap<String, Pair<Int, String>>()

            val label = async {
                val labels = albumRepository.getLabelsToList(uid).getOrThrow()
                val allLocalLabels = syncRepository.getSyncCheckAlbums().sortedBy { it.labelName }

                allLocalLabels.forEach { localLabel ->
                    var duplicationLabel = false
                    var unSyncLocalData = true

                    labels.forEach { firebaseLabel ->
                        if (!duplicationLabel && isUnSyncLabel(localLabel, firebaseLabel)) {
                            duplicationLabel = true
                            launch {
                                insertLabelToServer(uid, localLabel)
                            }
                        }

                        if (firebaseLabel.labelName == localLabel.labelName) {
                            unSyncLocalData = false
                        }
                    }

                    if (unSyncLocalData) {
                        launch {
                            insertLabelToServer(uid, localLabel)
                        }
                    }
                }

                labels.forEach { firebaseLabel ->
                    if (
                        !allLocalLabels.binarySearch(target = firebaseLabel.labelName)
                    ) {
                        val labelId = syncRepository.getIdByName(firebaseLabel.labelName)
                        if (labelId == null) {
                            launch {
                                fileNameToLabelUid[firebaseLabel.labelName] =
                                    insertLabelToLocal(firebaseLabel) to firebaseLabel.fileName
                            }
                        }
                    }
                }
            }

            val photoDetail = async {
                val allServerPhotos = albumRepository.getPhotosToList(uid).getOrThrow()
                val allLocalPhotos = syncRepository.getSyncCheckPhotos().sortedBy { it.fileName }

                allLocalPhotos.forEach { photo ->
                    if (
                        !allServerPhotos.binarySearch(target = photo.fileName)
                    ) {
                        launch {
                            insertPhotoDetailToServer(uid, photo)
                        }
                    }
                }

                unSynchronizedPhotoDetailsToLocal.addAll(
                    allServerPhotos.filter { photo ->
                        !allLocalPhotos.binarySearch(target = photo.fileName)
                    }
                )
            }

            label.await()
            photoDetail.await()

            async {
                unSynchronizedPhotoDetailsToLocal.forEach { photo ->
                    launch {
                        insertPhotoDetailAndAlbumToLocal(photo, fileNameToLabelUid)
                    }
                }
            }.await()

            syncDataStore.setSyncTime(
                LocalDateTime.now(ZoneId.of("UTC")).toDateTimeString()
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            IS_RUNNING = false
            runSync(applicationContext)
        }
    }

    private suspend fun insertLabelToServer(uid: String, label: SyncAlbumsDto) {
        val storageUri = albumRepository
            .saveImageFile(
                uid = uid,
                label = label.labelName,
                fileName = label.fileName,
                uri = label.photoDetailUri.toUri(),
            )

        albumRepository
            .insertLabel(
                uid = uid,
                labelName = label.labelName,
                labelData = FirebaseLabel(
                    backgroundColor = label.labelBackgroundColor,
                    thumbnailUri = storageUri.toString(),
                    fileName = label.fileName
                )
            )
    }

    private suspend fun insertLabelToLocal(label: FirebaseLabelResponse): Int =
        withContext(Dispatchers.IO + SupervisorJob()) {
            val localLabelData = Label(
                backgroundColor = label.backgroundColor,
                name = label.labelName
            )

            labelRepository.insertLabel(localLabelData).toInt()
        }

    private suspend fun insertPhotoDetailAndAlbumToLocal(
        photo: FirebasePhotoInfoResponse,
        fileNameToLabelUid: HashMap<String, Pair<Int, String>>,
    ) = withContext(Dispatchers.IO + SupervisorJob()) {

        val valueList = fileNameToLabelUid.values.toList()
        val findAlbumData = valueList.find { value -> value.second == photo.fileName }
        val uri = makeFileToUri(photo.uri, photo.fileName)


        val labelId = findAlbumData?.first
            ?: fileNameToLabelUid[photo.label]?.first
            ?: syncRepository.getIdByName(photo.label)!!

        val isDeletedImage = syncDataStore.getDeletedFileNames().contains(photo.fileName)
        if (isDeletedImage) {
            deleteServerPhoto(photo, labelId)
        } else {
            val photoDetailId = insertPhotoDetailToLocal(photo, labelId, uri)
            if (findAlbumData != null) {
                insertAlbum(labelId, photoDetailId)
            }
        }
    }

    private suspend fun performHazardAnalysis(photo: SyncPhotoDetailsDto): HazardAnalyzeStatus {
        val hazardAnalyzeStatus = syncRepository.getHazardCheckResultByFileName(photo.fileName)
        if (hazardAnalyzeStatus == HazardAnalyzeStatus.FAIL) return HazardAnalyzeStatus.FAIL

        val imgEncoding = ImageConvert.getBase64FromUri(applicationContext, photo.photoDetailUri)
        val hazardMapperResult = retrofitRepository.analyzeHazardWithGreenEye(imgEncoding)

        val updatedStatus = if (hazardMapperResult == HazardAnalyzeStatus.FAIL) {
            HazardAnalyzeStatus.FAIL
        } else {
            HazardAnalyzeStatus.PASS
        }
        syncRepository.updateHazardCheckResultByFIleName(updatedStatus, photo.fileName)
        return updatedStatus
    }

    private suspend fun insertPhotoDetailToServer(uid: String, photo: SyncPhotoDetailsDto) {
        val hazardAnalyzeStatus = performHazardAnalysis(photo)
        if (hazardAnalyzeStatus == HazardAnalyzeStatus.FAIL) return

        val storageUri = albumRepository
            .saveImageFile(
                uid = uid,
                label = photo.labelName,
                fileName = photo.fileName,
                uri = photo.photoDetailUri.toUri(),
            )

        albumRepository
            .insertPhotoInfo(
                uid = uid,
                fileName = photo.fileName,
                photoData = FirebasePhotoInfo(
                    uri = storageUri.toString(),
                    label = photo.labelName,
                    latitude = photo.latitude,
                    longitude = photo.longitude,
                    description = photo.description,
                    datetime = photo.datetime
                )
            )
    }

    private suspend fun insertPhotoDetailToLocal(
        photo: FirebasePhotoInfoResponse,
        labelId: Int,
        uri: String,
    ): Int {
        val latitude = photo.latitude ?: 0.0
        val longitude = photo.longitude ?: 0.0
        return photoDetailRepository.insertPhoto(
            PhotoDetail(
                labelId = labelId,
                photoUri = uri,
                fileName = photo.fileName,
                latitude = latitude, //FIXME 위치 NULL 해결 되면 삭제
                longitude = longitude,
                description = photo.description,
                datetime = LocalDateTime.parse(
                    photo.datetime,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                ).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime(),
                hazardCheckResult = HazardAnalyzeStatus.PASS,
                address = retrofitRepository.convertCoordsToAddress(
                    latitude = latitude,
                    longitude = longitude
                )
            )
        ).toInt()
    }

    private suspend fun insertAlbum(labelId: Int, photoDetailId: Int) {
        localAlbumRepository.insertPhotoInAlbum(
            Album(
                labelId = labelId,
                photoDetailId = photoDetailId
            )
        )
    }

    private suspend fun deleteServerPhoto(photo: FirebasePhotoInfoResponse, labelId: Int) {
        val hazardAnalyzeStatus = syncRepository.getHazardCheckResultByFileName(photo.fileName)
        if (hazardAnalyzeStatus == HazardAnalyzeStatus.FAIL) return

        val uid = UserManager.getUser()?.uid
        if (!uid.isNullOrEmpty()) {
            val label = labelRepository.getLabelById(labelId)
            val result = albumRepository.deleteImageFile(
                uid = uid,
                label = label,
                fileName = photo.fileName,
            )
            if (result) syncDataStore.removeDeletedFileName(photo.fileName)
        }
    }

    private fun makeFileToUri(photoUri: String, fileName: String): String {
        val context = applicationContext
        val storage = context.filesDir
        val imageFile = File(storage, fileName)
        imageFile.createNewFile()

        FileOutputStream(imageFile).use { fos ->
            BitmapFactory.decodeStream(URL(photoUri).openStream()).apply {
                if (Build.VERSION.SDK_INT >= 30) {
                    compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, fos)
                } else {
                    compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }

                recycle()
            }
            fos.flush()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        ).toString()
    }

    private fun isUnSyncLabel(label: SyncAlbumsDto, firebaseLabel: FirebaseLabelResponse): Boolean {
        return (firebaseLabel.labelName == label.labelName) &&
                ((firebaseLabel.fileName != label.fileName)
                        || (firebaseLabel.backgroundColor != label.labelBackgroundColor))
    }
}
