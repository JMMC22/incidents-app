package com.example.incidentsapp

import android.util.Log
import com.example.incidentsapp.models.*
import com.example.incidentsapp.notifications.NotificationData
import com.example.incidentsapp.notifications.PushNotification
import com.example.incidentsapp.utils.MyFirebaseMessagingService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class FirebaseData {

    var database : FirebaseDatabase = Firebase.database
    var currentUserId = Firebase.auth.currentUser?.uid

    var ref: DatabaseReference = database.reference

    var incidentsOfCurrentUser: MutableList<Incident> = mutableListOf()
    var tagsSystem: MutableList<Tag> = mutableListOf()
    var isRegistered : Boolean = false
    var isAdmin : Boolean = false
    var hasCreated: Boolean = false
    var allIncidents: MutableList<Incident> = mutableListOf()
    var incidentsByStatus: MutableList<Incident> = mutableListOf()
    var usersBest: MutableList<User> = mutableListOf()
    var badges: MutableList<Badge> = mutableListOf()
    var userBadges: MutableList<Badge> = mutableListOf()
    var bestperIncidents: MutableList<User> = mutableListOf()
    var bestperBadges: MutableList<User> = mutableListOf()



    /*
        Incidencias
     */

    // Obtiene las incidencias del usuario actual
    fun getIncidentsOfCurrentUser(firebaseCallback: FirebaseCallback) {
        ref.child("incidents").orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    incidentsOfCurrentUser.clear()
                    for (incident in snapshot.children){
                        incidentsOfCurrentUser.add(incident.getValue<Incident>()!!)
                    }
                    firebaseCallback.onCallback(incidentsOfCurrentUser)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    // Obtiene todas las incidencias
    fun getAllIncidents(firebaseCallback: FirebaseCallback) {
        ref.child("incidents")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allIncidents.clear()
                    for (incident in snapshot.children){
                        allIncidents.add(incident.getValue<Incident>()!!)
                    }
                    firebaseCallback.onCallback(allIncidents)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    // Obtener incidencias por estado
    fun getIncidentsByStatus(status: String, firebaseCallback: FirebaseCallback) {
        ref.child("incidents").orderByChild("status").equalTo(status)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    incidentsByStatus.clear()
                    for (incident in snapshot.children){
                        incidentsByStatus.add(incident.getValue<Incident>()!!)
                    }
                    firebaseCallback.onCallback(incidentsByStatus)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    // Elimina incidencia
    suspend fun deleteIncident(id: String){
        ref.child("incidents/" + id).removeValue()
    }

    // Cambia estado de incidencia
    fun setStatus(id: String, status: String, firebaseCallback: FirebaseCallback) {
        ref.child("incidents/" + id)
            .child("status").setValue(status)
            .addOnCompleteListener {
                firebaseCallback.onCallback(true)
            }

    }

    /*
       Tags
    */

    // Obtiene todas las tags de la app
    fun getAllTags(firebaseCallback: FirebaseCallback) {
        ref.child("tags")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tagsSystem.clear()
                    for (incident in snapshot.children){
                        tagsSystem.add(snapshot.getValue<Tag>()!!)
                    }
                    firebaseCallback.onCallback(tagsSystem)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    /*
        badges
     */
    fun checkIfBadgesCreated() {
        ref.child("badges")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        createBadges()
                    }
                }

            })
    }

    // Crea un usuario en la base de datos con el rol de USER
    fun createBadges(){
        var badges: List<Badge> = Badge().generateBadges()
        badges.forEach {
            ref.child("badges")
                .child(it.id.toString())
                .setValue(it)
        }

    }

    fun getAllBadges(callback: FirebaseCallback) {
        ref.child("badges").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.getValue<Badge>()?.let { it1 -> badges.add(it1) }
                }
                callback.onCallback(badges)
            }
        })
    }

    /*
       Usuarios
    */

    // Crea un usuario en la base de datos con el rol de USER
    fun createUser(uid: String,email: String,deviceId: String, callback: FirebaseCallback){
        getAllBadges(object : FirebaseCallback {
            override fun onCallback(data: Any) {
                var user = User(uid, "user", "0", "0", email,deviceId, "0", "0")
                ref.child("users")
                    .child(uid)
                    .setValue(user).addOnCompleteListener {
                        badges.forEach {
                            ref.child("users")
                                .child(uid)
                                .child("badges")
                                .child(it.id!!)
                                .setValue(it)
                        }
                        hasCreated = true
                        callback.onCallback(hasCreated)
                    }
            }
        })

    }

    // Obtiene si el usuario estaba registrado
    fun isRegistered(uid: String, callback: FirebaseCallback) {

        ref.child("users").child(uid).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isRegistered = true
                } else {
                    isRegistered = false
                }
                callback.onCallback(isRegistered)
            }
        })
    }

    // Obtiene si el usuario tiene el rol administrador
    fun isAdmin(uid: String, callback: FirebaseCallback) {

        ref.child("users").child(uid).child("role")
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value!!.equals("admin")){
                        isAdmin = true
                    } else {
                        isAdmin = false
                    }
                    callback.onCallback(isAdmin)
                }
                })
    }

    fun setPxToUser(userId: String, pxToAdd: Int) {
        ref.child("users")
            .child(userId).get().addOnSuccessListener {
                var px = it.child("px").value as String
                addPointsExperience(userId, px, pxToAdd)
            }
    }


    fun addPointsExperience(userId: String, px: String, pxToAdd: Int){
        var addedPx = (px)?.toInt()?.plus(pxToAdd)
        ref.child("users")
            .child(userId)
            .child("px")
            .setValue(addedPx.toString())
    }

    fun addOneIncidentToCount(user: User){
        var incidentsN = (user.incidentsN)?.toInt()?.plus(1)
        ref.child("users")
            .child(currentUserId!!)
            .child("incidentsN")
            .setValue(incidentsN.toString())

        this.setIncidentsBadge(user, incidentsN.toString())

    }

    fun addOneBadgeToCount(user: User){
        var badgesN = (user.badgesN)?.toInt()?.plus(1)
        ref.child("users")
            .child(currentUserId!!)
            .child("badgesN")
            .setValue(badgesN.toString())
        var notificationData = NotificationData("¡Nueva insignia!", "¡Enhorabuena, ve a tu perfil para comprobar tu nueva insignia!")
        PushNotification(notificationData, MyFirebaseMessagingService.token!!, notificationData).sendNotification()
    }

    fun updateLevel(level: Int) {
        ref.child("users")
            .child(currentUserId!!)
            .child("level")
            .setValue(level.toString())
    }

    fun getUsersBestPx(callback: FirebaseCallback) {
        ref.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersBest.clear()
                    bestperBadges.clear()
                    bestperIncidents.clear()

                    getBestUsersPerPx(snapshot)
                    getBestUsersPerNIncidents(snapshot)
                    getBestUsersPerNBadges(snapshot)

                    callback.onCallback(mutableListOf(usersBest, bestperIncidents, bestperBadges))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    fun getBestUsersPerPx(dataSnapshot: DataSnapshot) {
        var users =
            dataSnapshot.children.sortedByDescending { (it.child("px").value as String?)?.toInt() }
                .take(10)
        for (user in users) {
            Log.d("TAG", user.value.toString())
            User()?.apply {
                id = user.child("id").value as String?
                px = user.child("px").value as String?
                level = user.child("level").value as String?
                role = user.child("role").value as String?
                email = user.child("email").value as String?
                user.child("badges").children.forEach {
                    badges?.add(it.getValue<Badge>()!!)
                }
                incidentsN = user.child("incidentsN").value as String?
                badgesN = user.child("badgesN").value as String?

            }.also {
                usersBest.add(it!!)
            }
        }
    }

    fun getBestUsersPerNIncidents(dataSnapshot: DataSnapshot) {
        var users =
            dataSnapshot.children.sortedByDescending { (it.child("incidentsN").value as String?)?.toInt() }
                .take(10)
        for (user in users) {
            User()?.apply {
                id = user.child("id").value as String?
                px = user.child("px").value as String?
                level = user.child("level").value as String?
                role = user.child("role").value as String?
                email = user.child("email").value as String?
                user.child("badges").children.forEach {
                    badges?.add(it.getValue<Badge>()!!)
                }
                incidentsN = user.child("incidentsN").value as String?
                badgesN = user.child("badgesN").value as String?

            }.also {
                bestperIncidents.add(it!!)
            }
        }
    }

    fun getBestUsersPerNBadges(dataSnapshot: DataSnapshot) {
        var users =
            dataSnapshot.children.sortedByDescending { (it.child("badgesN").value as String?)?.toInt() }
                .take(10)
        for (user in users) {
            User()?.apply {
                id = user.child("id").value as String?
                px = user.child("px").value as String?
                level = user.child("level").value as String?
                role = user.child("role").value as String?
                email = user.child("email").value as String?
                user.child("badges").children.forEach {
                    badges?.add(it.getValue<Badge>()!!)
                }
                incidentsN = user.child("incidentsN").value as String?
                badgesN = user.child("badgesN").value as String?

            }.also {
                bestperBadges.add(it!!)
            }
        }
    }



    fun setLevelBadge(userdata: User, level: String){
        var badgeRef = ref.child("users").child(currentUserId!!).child("badges")
        userdata.badges?.filter { it.type.equals(BadgeType.LEVEL.toString()) && it.done!!.equals(false) }?.forEach {
            if ((it.maxCount!!).toInt() > (level).toInt() ) {
                badgeRef.child(it.id!!).child("actualCount").setValue(level)
            } else if ((level).toInt() == (it.maxCount!!).toInt()) {
                badgeRef.child(it.id!!).child("actualCount").setValue(it.maxCount)
                badgeRef.child(it.id!!).child("done").setValue(true)
                addOneBadgeToCount(userdata)
            }
        }
    }

    fun setIncidentsBadge(userdata: User, incidentN: String){
        var badgeRef = ref.child("users").child(currentUserId!!).child("badges")
        userdata.badges?.filter { it.type.equals(BadgeType.INCIDENT.toString()) && it.done!!.equals(false) }?.forEach {
            if ((it.maxCount!!).toInt() > (incidentN).toInt() ) {
                badgeRef.child(it.id!!).child("actualCount").setValue(incidentN)
            } else if ((incidentN).toInt() == (it.maxCount!!).toInt()) {
                badgeRef.child(it.id!!).child("actualCount").setValue(it.maxCount)
                badgeRef.child(it.id!!).child("done").setValue(true)
                addOneBadgeToCount(userdata)
            }
        }
    }

    // Obtiene las incidencias del usuario actual
    fun getBadgesOfCurrentUser(firebaseCallback: FirebaseCallback) {
        ref.child("users").child(currentUserId!!).child("badges")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userBadges.clear()
                    snapshot.children.forEach {
                        userBadges.add(it.getValue<Badge>()!!)
                    }
                    userBadges.sortBy { it.maxCount!!.toInt() }
                    firebaseCallback.onCallback(userBadges)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }

    fun getDeviceIdFromUser(userId: String, firebaseCallback: FirebaseCallback) {
        ref.child("users")
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var deviceId = snapshot.child("deviceId").value as String?
                    firebaseCallback.onCallback(deviceId!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("onCancelled", error.toException())
                }

            })
    }


}