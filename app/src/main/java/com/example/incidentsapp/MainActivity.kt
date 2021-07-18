package com.example.incidentsapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.incidentsapp.fragments.LoginFragment
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.utils.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.nio.file.Files.createFile

class MainActivity : AppCompatActivity() {
     var auth : FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_IncidentsApp)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseData().checkIfBadgesCreated()

        if (checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)

            })
        } else {
            //You won't be able to send notifications to this device
            Log.w("TAG", "Device doesn't have google play services")
        }

        if(auth.currentUser != null){
            startActivity(Intent(this,MapsActivity::class.java))
            finish()
        }else{
            supportFragmentManager.beginTransaction().add(R.id.fragment_main_activity,LoginFragment()).commit()
        }

    }

    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Log.e("TAG", "Error")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Log.i("TAG", "Google play services updated")
            true
        }
    }
}