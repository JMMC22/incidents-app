package com.example.incidentsapp.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Tag
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.isNotEmpty

class MapaFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var lastKnownLocation: Location? = null

    private var database = Firebase.database.reference
    var firebaseData : FirebaseData = FirebaseData()
    var user = Firebase.auth.currentUser

    private var markerMap : HashMap<Marker,Incident> = HashMap<Marker,Incident>()

    var tagsFilter: ArrayList<String> = ArrayList()
    var statusFilter: ArrayList<String> = ArrayList()
    var createdByUserFilter: Boolean = false
    var nearIncidents: MutableList<Incident> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagsFilter.clear()
        statusFilter.clear()

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        configureViewButtons()

    }

    override fun onResume() {
        super.onResume()
    }

    fun configureViewButtons(){

        addButton.setOnClickListener(View.OnClickListener { v: View? ->


            if (nearIncidents.isEmpty()) {
                    showAddNewincident()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("¡Parece que hay incidencias cerca!")
                    .setMessage("¿Has comprobado que la incidencia no está ya registrada?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss()  })
                    .setPositiveButton("Sí", DialogInterface.OnClickListener { dialog, which -> showAddNewincident() })
                    .show()
            }
        })

        actual_position.setOnClickListener(View.OnClickListener { v: View? ->
            getDeviceLocation()
        })

        filter.setOnClickListener(View.OnClickListener { v: View? ->
            val filterFragment = FilterFragment()
            var bundle : Bundle = Bundle()
            bundle.putStringArrayList("tagsFilter",tagsFilter)
            bundle.putBoolean("createdByUser", createdByUserFilter)
            bundle.putStringArrayList("statusFilter", statusFilter)
            filterFragment.arguments = bundle
            filterFragment.setTargetFragment(this, FILTER_FRAGMENT)
            filterFragment.show(requireActivity().supportFragmentManager,"filter")

        })
    }

    fun showAddNewincident() {
        val bundle = Bundle()
        bundle.putDouble("latitude",lastKnownLocation!!.latitude)
        bundle.putDouble("longitude",lastKnownLocation!!.longitude)

        val addFragmet = AddIncidentFragment()
        addFragmet.arguments = bundle

        addFragmet.show(requireActivity().supportFragmentManager,"addIncident")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            FILTER_FRAGMENT ->
                if (resultCode == Activity.RESULT_OK){
                    var bundle : Bundle = data?.extras!!
                    tagsFilter = bundle.getStringArrayList("tagsFilter")!!
                    statusFilter = bundle.getStringArrayList("statusFilter")!!
                    createdByUserFilter = bundle.getBoolean("createdByUser")!!
                    addMarkersToMap(map!!)
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        getLocationPermission() //Pedimos permisos

        // Si se pulsa sobre Mark, se abre un dialogo con los datos de la incidencia
        map!!.setOnMarkerClickListener { marker: Marker? ->
            Log.d(TAG, markerMap[marker].toString())

            val builder = AlertDialog.Builder(requireContext())
            val view : View = layoutInflater.inflate(R.layout.dialog_incident,null)

            builder.setView(view)

            val alertDialog : AlertDialog = builder.create()
            alertDialog.window?.setGravity(Gravity.TOP)
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Ver",{dialog, which ->
                val detailsIncidentFragmet = DetailsIncidentFragment()

                val bundle = Bundle()
                bundle.putString("image",markerMap[marker]?.image)
                bundle.putString("description",markerMap[marker]?.description)
                bundle.putString("tag",markerMap[marker]?.tag)
                bundle.putDouble("latitud", markerMap[marker]?.latitud!!)
                bundle.putDouble("longitud", markerMap[marker]?.longitud!!)
                bundle.putString("id", markerMap[marker]?.id)

                detailsIncidentFragmet.arguments = bundle

                detailsIncidentFragmet.show(requireActivity().supportFragmentManager,"showIncident")
            })
            alertDialog.show()

            alertDialog.findViewById<TextView>(R.id.description_incident).setText(markerMap[marker]?.description)
            Picasso.get().load(markerMap[marker]?.image).into(alertDialog.findViewById<ImageView>(R.id.icono_incident))

            true
        }


    }

    //Función obtener permisos de localización
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION)
        }
    }

    //Función al conceder permisos o denegarlos
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            LOCATION_PERMISSION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    //Añadir botones de localización dependiendo de si ha concedido los permisos necesarios
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                getDeviceLocation()
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = false
                actual_position.isEnabled = true
                addButton.isEnabled = true

            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                actual_position.isEnabled = false
                addButton.isEnabled = false

                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //Obtener localización del dispositivo
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation

                locationResult.addOnSuccessListener { location: Location? ->
                    lastKnownLocation = location
                    if (lastKnownLocation != null) {
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), MapaFragment.DEFAULT_ZOOM.toFloat()))
                        addMarkersToMap(map!!)

                    } else {
                        Snackbar.make(requireView(), "Current location is null.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    // se añaden las MArks al mapa
    private fun addMarkersToMap(map: GoogleMap) {

        database.child("incidents")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d(MapaFragment.TAG,"Error al recibir información")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    map.clear()
                    val children = snapshot.children
                    nearIncidents.clear()

                    // Por cada incidencia de la base de datos, se crea una Mark
                    children.forEach{
                        val incident = it.getValue<Incident>()
                        val results = FloatArray(1)
                        Location.distanceBetween(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude,
                            incident!!.latitud!!.toDouble(), incident!!.longitud!!.toDouble(), results)

                        if (results[0] <= 20.0) {
                            nearIncidents.add(incident)
                        }

                        if (createdByUserFilter){
                            if (incident.userId.equals(user!!.uid) && (tagsFilter.isEmpty() || tagsFilter.contains(incident?.tag)) && (statusFilter.isEmpty() || statusFilter.contains(incident?.status))) {
                                createMark(map,incident!!)
                            }
                        } else {
                            if ((tagsFilter.isEmpty() || tagsFilter.contains(incident?.tag)) && (statusFilter.isEmpty() || statusFilter.contains(incident?.status))) {
                                createMark(map,incident!!)
                            }
                        }

                }
            }

        })


    }

    // Crear Mark que aparece en el mapa
    fun createMark(map:GoogleMap,incident : Incident){
        var tag : Tag? = null

        // Se obtiene TAG de la incidencia
        database.child("tags").child(incident.tag!!).addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG,"Error al recibir información")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                tag = snapshot.getValue<Tag>()!!

                val incidentMarker : Marker =  map.addMarker(MarkerOptions()
                    .position(LatLng(incident.latitud!!,incident.longitud!!))
                    //.icon(BitmapDescriptorFactory.fromResource(requireContext().resources.getIdentifier(tag!!.icon,"drawable",requireContext().packageName))))
                    .icon(BitmapDescriptorFactory.fromBitmap(createCircleBitmapOfTag(tag!!))))

                markerMap.put(incidentMarker,incident)
            }
        })

    }

    // Crear circulo donde aparece icono del tag correspondiente
    fun createCircleBitmapOfTag(tag: Tag): Bitmap{

        var circlePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        circlePaint.setColor(Color.parseColor(tag?.colorBubble))
        circlePaint.style = Paint.Style.FILL
        circlePaint.isAntiAlias = true

        var circleBitmap : Bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
        var circleCanvas : Canvas = Canvas(circleBitmap)
        var radius : Int = Math.min(circleCanvas.width, circleCanvas.height/2)
        var padding : Int = 5

        var icon : Bitmap = (BitmapFactory.decodeResource(requireContext().resources, requireContext().resources.getIdentifier(tag!!.icon,"drawable",requireContext().packageName)))

        circleCanvas.drawCircle(
                (circleCanvas.width/2).toFloat(),
                (circleCanvas.height/2).toFloat(),
                (radius - padding).toFloat(),
                circlePaint)

        circleCanvas.drawBitmap(icon,((circleCanvas.width - icon.width)/2).toFloat(),((circleCanvas.height - icon.height)/2).toFloat(),circlePaint)


        return  circleBitmap
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 17 //Zoom predetermiando
        private const val FILTER_FRAGMENT = 1
        private const val LOCATION_PERMISSION = 42

    }
}

