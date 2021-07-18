package com.example.incidentsapp.fragments.admin

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.R
import com.example.incidentsapp.adapters.IncidentAdapter
import com.example.incidentsapp.fragments.DetailsIncidentFragment
import com.example.incidentsapp.fragments.MapaFragment
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.utils.GeoJSON
import com.example.incidentsapp.utils.LoadingDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_incidents_admin.*


class IncidentsAdminFragment : Fragment() {

    private var writePermissionGranted = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var tab: TabLayout

    var adapter : IncidentAdapter = IncidentAdapter()
    var incidents: MutableList<Incident> = mutableListOf()
    var firebaseData: FirebaseData = FirebaseData()

    lateinit var loading: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAllIncidents() // Obtenemos todas las incidencias de la app
        loading = LoadingDialog(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incidents_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerIncidents)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tab = view.findViewById(R.id.tabIncidents)

        adapter.IncidentAdapter(incidents,requireContext())

        // Si se pulsa sobre una incidencia, se pasa la información a DetailsFragment
        adapter.setOnClickListener(View.OnClickListener { v: View? ->

            var position : Int? = v?.let { recyclerView.getChildAdapterPosition(it) }

            val detailsIncidentFragmet = DetailsIncidentFragment()

            val bundle = Bundle()
            bundle.putString("image",incidents[position!!]?.image)
            bundle.putString("description",incidents[position!!]?.description)
            bundle.putString("tag",incidents[position!!]?.tag)
            bundle.putDouble("latitud", incidents[position!!]?.latitud!!)
            bundle.putDouble("longitud", incidents[position!!]?.longitud!!)
            bundle.putString("id", incidents[position!!]?.id)

            detailsIncidentFragmet.arguments = bundle

            detailsIncidentFragmet.show(requireActivity().supportFragmentManager,"showIncident")

        })
        recyclerView.adapter = adapter

        // Si se pulsa sobre botón, se abre un modal
        fabExport.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Desea exportar las incidencias?")
                .setMessage("El archivo se exportará en formato .geojson.")
                .setNegativeButton("Cancelar") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Exportar") { dialog, which ->
                    getWritePermission()
                }
                .show()
        }

        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.i("onTab","Reselected")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.i("onTab","UnSelected")
            }

            // Deependiendo de que tab pulses se cargar las incidencias correspondientes
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> getAllIncidents()
                    1 -> getIncidentsByStatus(Status.PENDING.name)
                    2 -> getIncidentsByStatus(Status.REJECTED.name)
                    3 -> getIncidentsByStatus(Status.INPROCESS.name)
                    4 -> getIncidentsByStatus(Status.FIXED.name)
                }
            }

        })

    }

    // Obtener incidencias por status
    fun getIncidentsByStatus(status: String){
        FirebaseData().getIncidentsByStatus(status, object :FirebaseCallback {
            override fun onCallback(data: Any) {
                incidents.clear()
                incidents.addAll(data as MutableList<Incident>)
                adapter.notifyDataSetChanged()
            }

        })
    }

    // Obtener todas las incidencias
    fun getAllIncidents(){
        firebaseData.getAllIncidents(object: FirebaseCallback {
            override fun onCallback(data: Any) {
                incidents.clear()
                incidents.addAll(data as MutableList<Incident>)
                adapter.notifyDataSetChanged()
            }
        })
    }

    //Función obtener permisos de localización
    private fun getWritePermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext().applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            writePermissionGranted = true
            exportGeoJSON()

        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                IncidentsAdminFragment.WRITE_PERMISSION
            )
        }
    }

    //Función al conceder permisos o denegarlos
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        writePermissionGranted = false
        when (requestCode) {
            IncidentsAdminFragment.WRITE_PERMISSION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writePermissionGranted = true
                    exportGeoJSON()
                }
            }
        }
    }

    // Exporta las incidencias e geojson
    fun exportGeoJSON() {
        loading.startLoadingDialog()
        GeoJSON(requireActivity()).convertIncidentsToFeatures(incidents, object : FirebaseCallback {
            override fun onCallback(data: Any) {
                loading.dismissLoadingDialog()
                if (data == true) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("¡Éxito!")
                        .setMessage("Se han descargado las incidencias")
                        .show()
                }

            }

        })
    }

    companion object {
        private const val WRITE_PERMISSION = 40
    }
}