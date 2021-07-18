package com.example.incidentsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.incidentsapp.fragments.*
import com.example.incidentsapp.fragments.admin.IncidentsAdminFragment
import com.example.incidentsapp.models.Levels
import com.example.incidentsapp.models.Tag
import com.example.incidentsapp.models.User
import com.example.incidentsapp.notifications.NotificationData
import com.example.incidentsapp.notifications.PushNotification
import com.example.incidentsapp.notifications.RetrofitInstance
import com.example.incidentsapp.utils.LoadingDialog
import com.example.incidentsapp.utils.MyFirebaseMessagingService
import com.example.incidentsapp.utils.UserProfile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity() {

    var auth : FirebaseAuth = Firebase.auth
    var isAdmin : Boolean = false
    var user = Firebase.auth.currentUser
    lateinit var userData: User

    lateinit var loading: LoadingDialog

    lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_maps)
        MyFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        configureView()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_map_activity,
                MapaFragment()
            ).addToBackStack(null).commit()
        }
        checkIsAdmin()
        configureBottomNavigation()

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            MyFirebaseMessagingService.token = it
            token = it
            checkIfUserRegistered()
        }

    }

    fun checkIfUserRegistered() {
        FirebaseData().isRegistered(user!!.uid, object: FirebaseCallback {
            override fun onCallback(data: Any) {
                if(data as Boolean == false){
                    FirebaseData().createUser(user!!.uid, user!!.email!!, MyFirebaseMessagingService.token!!,  object :FirebaseCallback {
                        override fun onCallback(data: Any) {
                            getUser()
                        }

                    })
                } else {
                    getUser()
                }
            }
        })
    }

    fun getUser(){
        UserProfile().getUser(user!!.uid, object : FirebaseCallback {
            override fun onCallback(data: Any) {
                userData = data as User

                if (userData.level?.toInt() != Levels().checkCurrentLevelOfUser(userData.px!!.toInt())){
                    var level = Levels().checkCurrentLevelOfUser(userData.px!!.toInt())
                    var notificationData = NotificationData("¡Nuevo nivel!", "Enhorabuena, has subido de nivel!")
                    PushNotification(notificationData, MyFirebaseMessagingService.token!!, notificationData).sendNotification()
                    FirebaseData().updateLevel(level)
                    updateLevelBadges(userData, level)
                }
            }

        })
    }

    fun updateLevelBadges(userdata: User, level: Int) {
        FirebaseData().setLevelBadge(userdata, level.toString())
    }

    fun configureView(){
        loading = LoadingDialog(this)
        loading.startLoadingDialog()
    }

    fun configureBottomNavigation() {
        botton_nav.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.map -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_map_activity,MapaFragment()).addToBackStack(null).commit()
                    true
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_map_activity,ProfileFragment()).addToBackStack(null).commit()
                    true
                }
                R.id.ranking -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_map_activity,RankingFragment()).addToBackStack(null).commit()
                    true
                }
                R.id.incidents -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_map_activity,IncidentsAdminFragment()).addToBackStack(null).commit()
                    true
                }
                else->  false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    // Comprueba si usuario es administrador
    fun checkIsAdmin(){
            FirebaseData().isAdmin(auth.currentUser!!.uid, object: FirebaseCallback {
                override fun onCallback(data: Any) {
                    if(data as Boolean == true){
                        isAdmin = true
                        botton_nav.menu.findItem(R.id.incidents).isVisible = true
                    }
                    loading.dismissLoadingDialog()
                }
            })
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 17 //Zoom predetermiando

    }


}