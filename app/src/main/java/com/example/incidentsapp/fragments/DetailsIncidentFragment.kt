package com.example.incidentsapp.fragments

import android.app.Dialog
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.models.Tag
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_details_incident.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.image_preview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DetailsIncidentFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var idIncident : String

    private lateinit var mapView: MapView
    private var map: GoogleMap? = null

    var incident: Incident? = null

    var database : FirebaseDatabase = Firebase.database
    var currentUserId = Firebase.auth.currentUser?.uid
    var auth : FirebaseAuth = Firebase.auth

    var ref: DatabaseReference = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_IncidentsApp_FullScreenDialog)
        idIncident = requireArguments().getString("id").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details_incident, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getIncidentById(idIncident, object :FirebaseCallback {
            override fun onCallback(data: Any) {
                if (data == true) {
                    Picasso.get().load(incident?.image).into(image_incident)
                    description_view.setText(incident?.description)
                    incident?.let { configureStatus(incident!!) }

                    if(incident?.userId != currentUserId){
                        delete_button.isVisible = false
                    }
                    configureStatus(incident!!)
                    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this@DetailsIncidentFragment)
                }

            }

        })
        configureToolbar()
        delete_button.setOnClickListener(View.OnClickListener { v: View? ->
            CoroutineScope(Dispatchers.Main).launch {
                dismiss()
                FirebaseData().deleteIncident(idIncident)
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            FirebaseData().isAdmin(auth.currentUser!!.uid, object: FirebaseCallback {
                override fun onCallback(data: Any) {
                    if(data as Boolean == true){
                        var statusButton = view.findViewById<Button>(R.id.status_button)
                        statusButton.visibility = View.VISIBLE
                        statusButton.setOnClickListener {
                            val statusFragment = StatusFragment()
                            statusFragment.arguments = Bundle().apply {
                                putString("id", idIncident)
                                putString("status", incident?.status)
                                putString("userId", incident?.userId)
                            }
                            statusFragment.show(requireActivity().supportFragmentManager,"changeStatusIncident")
                        }
                    }
                }
            })
        }

    }
    fun configureToolbar(){
        toolbar_details?.setNavigationOnClickListener { v: View? -> dismiss() }
    }

    fun getIncidentById(id: String, callback: FirebaseCallback) {

        ref.child("incidents")
            .child(id).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.value != null) {
                            var inci = snapshot.getValue(Incident::class.java)
                            incident = inci
                            callback.onCallback(true)
                    } else {
                        callback.onCallback(false)
                    }

                }
            })
    }

    fun configureStatus(incident: Incident){
        var status: String? = incident?.status
        textViewStatus.text = status
        when(status) {
            Status.PENDING.name -> {
                imageViewStatus.setImageResource(R.drawable.ic_schedule)
                delete_button.visibility = View.VISIBLE
            }
            Status.INPROCESS.name -> imageViewStatus.setImageResource(R.drawable.ic_ethics)
            Status.FIXED.name -> imageViewStatus.setImageResource(R.drawable.ic_checked)
            Status.REJECTED.name -> imageViewStatus.setImageResource(R.drawable.ic_cancel)
        }
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT

            dialog?.window?.setLayout(width, height)
            dialog?.window?.setWindowAnimations(R.style.Theme_IncidentsApp_Slide)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.uiSettings?.isZoomGesturesEnabled = false
        map?.uiSettings?.isScrollGesturesEnabled = false

        incident?.let { createMark(map!!, it) }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(incident?.latitud!!, incident?.longitud!!), DetailsIncidentFragment.DEFAULT_ZOOM.toFloat()))
    }

    // Crear Mark que aparece en el mapa
    fun createMark(map:GoogleMap,incident : Incident){
        var tag : Tag? = null

        // Se obtiene la TAG de la incidencia
        ref.child("tags").child(incident.tag!!).addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.d("onCanceled","Error al recibir información")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                tag = snapshot.getValue<Tag>()!!

                textView_tag_category.text = tag?.name

                map.addMarker(MarkerOptions()
                    .position(LatLng(incident.latitud!!,incident.longitud!!))
                    //.icon(BitmapDescriptorFactory.fromResource(requireContext().resources.getIdentifier(tag!!.icon,"drawable",requireContext().packageName))))
                    .icon(BitmapDescriptorFactory.fromBitmap(createCircleBitmapOfTag(tag!!))))

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
        private const val TAG = "DetailsFragment"
        private const val DEFAULT_ZOOM = 17 //Zoom predetermiando

    }
}