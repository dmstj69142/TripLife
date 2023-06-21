package com.example.triplife.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun ImageView.loadProfileImage(id: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()) {
    val profileImage = getProfileImage(id).getOrNull()
    if (profileImage != null) {
        loadImageUrl(profileImage, isCircle = true)
    }
}


suspend fun getProfileImage(userId: String): Result<String> {
    return suspendCoroutine { continuation ->
        val storage = FirebaseStorage.getInstance()
        storage.reference.child("profiles" + File.separator + userId + ".png")
            .downloadUrl
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("getProfileImage", "이미지 로드 성공 !")
                    continuation.resume(Result.success(it.result.toString()))
                } else {
                    Log.e("getProfileImage", "이미지 로드 에러 !")
                    continuation.resume(
                        Result.failure(
                            it.exception ?: Throwable("image load error")
                        )
                    )
                }
            }
    }
}