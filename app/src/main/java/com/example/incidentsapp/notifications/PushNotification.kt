package com.example.incidentsapp.notifications

import android.util.Log
import com.example.incidentsapp.MapsActivity
import com.example.incidentsapp.utils.MyFirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PushNotification(val data: NotificationData,
                            val to: String,
                            val notification: NotificationData) {

    fun sendNotification() {
        PushNotification(NotificationData(data.title, data.body), to, NotificationData(data.title,data.body))
            .also {
            sendNotification(it)
            }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                // Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("Push Notification: ", response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e("Push Notification: ", e.toString())
        }
    }
}