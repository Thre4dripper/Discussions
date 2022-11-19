package com.example.discussions

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject


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
            context: Context,
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

            //deleting previous image from cloudinary if new image is selected
            //if fallbackImageUri is empty then it means that user is uploading a new image
            if (fallbackImageUri.toString().isNotEmpty()) {
                deleteImage(context, fallbackImageUri.toString())
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

        fun deleteImage(context: Context, imageUrl: String) {
            val split = imageUrl.split("/")

            val username = split[split.size - 2]
            val fileName = split[split.size - 1].substring(0, split[split.size - 1].indexOf("."))

            val publicId = "$username/$fileName"

            Log.d("Cloudinary", "deleteImage: $publicId")

            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.DELETE_IMAGE}"

            val body = "{\n" +
                    "    \"public_id\": \"$publicId\"\n" +
                    "}"

            val request = JsonObjectRequest(Request.Method.POST, url, JSONObject(body), { }, { })

            queue.add(request)
        }
    }
}