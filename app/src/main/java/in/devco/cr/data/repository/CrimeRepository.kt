package `in`.devco.cr.data.repository

import `in`.devco.cr.data.remote.API
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CrimeRepository @Inject constructor(private val service: API) {
    fun reportCrime(
        latitude: String,
        longitude: String,
        description: String,
        imageFile: MultipartBody.Part?,
        videoFile: MultipartBody.Part?
    ) = service.reportCrime(latitude, longitude, description, imageFile, videoFile)
}