package com.example.incidentsapp.utils

import android.util.Log
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.models.Badge
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Levels
import com.example.incidentsapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.math.log

class UserProfile() {

    var database : FirebaseDatabase = Firebase.database
    var user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var currentUser: User = User()
    var previousData: User = User()

    var ref: DatabaseReference = database.reference

    fun getUser(userId: String, callback: FirebaseCallback) {
        ref.child("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser?.apply {
                        id = snapshot.child("id").value as String?
                        px= snapshot.child("px").value as String?
                        level = snapshot.child("level").value as String?
                        role = snapshot.child("role").value as String?
                        email = snapshot.child("email").value as String?
                        deviceId = snapshot.child("deviceId").value as String?
                        snapshot.child("badges").children.forEach{
                            badges?.add(it.getValue<Badge>()!!)
                        }
                        incidentsN = snapshot.child("incidentsN").value as String?
                        badgesN = snapshot.child("badgesN").value as String?

                    }.also {
                        if(!previousData.equals(currentUser)) {
                            previousData = it!!
                            callback.onCallback(it!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    /*fun setBadge(badgeId: String, actualCount: String, callback: FirebaseCallback) {
        ref.child(user!!.uid)
            .child("badges")
            .child(badgeId)
    } */

}