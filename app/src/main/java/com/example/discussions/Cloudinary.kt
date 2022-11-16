package com.example.discussions

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.discussions.api.ResponseCallback

class Cloudinary {
    companion object {
        /**
         * METHOD FOR SETTING UP CLOUDINARY SDK
         */
        fun setupCloudinary(context: Context) {
            val config = HashMap<String, String>()
            config["cloud_name"] = context.getString(R.string.cloud_name)
            config["api_key"] = context.getString(R.string.api_key)
            config["api_secret"] = context.getString(R.string.api_secret)
            config["secure"] = "true"

            MediaManager.init(context, config)
        }

        /**
         * METHOD FOR UPLOADING IMAGE TO CLOUDINARY AND RETURNING THE URL
         */
        fun uploadImage(
            callback: ResponseCallback,
            selectedImageUri: Uri,
            fallbackImageUri: Uri,
            folderName: String
        ) {
            //if image is not changed then @selectedImageUri will be empty
            if (selectedImageUri.toString().isEmpty()) {
                callback.onSuccess(fallbackImageUri.toString())
                return
            }

            MediaManager.get().upload(selectedImageUri)
                    //removing @ from folder name
                .option("folder", "${folderName.substring(1)}/")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        var imageUrl = resultData!!["url"].toString()
                        imageUrl = imageUrl.replace("http://", "https://")
                        callback.onSuccess(imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        callback.onError("Error uploading image")
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}

                }).dispatch()
        }
    }
}