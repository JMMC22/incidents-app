package com.example.incidentsapp

import com.google.android.gms.common.api.Response

interface FirebaseCallback {
    fun onCallback(data: Any)
}