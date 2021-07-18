package com.example.incidentsapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.adapters.FilterAdapter
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.models.Tag
import com.example.incidentsapp.utils.LoadingDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_filter.*
import java.util.*
import kotlin.collections.ArrayList

class FilterFragment : DialogFragment() {


    lateinit var recyclerView: RecyclerView
    var adapter : FilterAdapter = FilterAdapter()
    lateinit var toolbar: Toolbar
    lateinit var filterButton: Button

    private lateinit var database: DatabaseReference

    var tags = mutableListOf<Tag>()
    var tagsFilter: ArrayList<String> = ArrayList()
    var statusFilter: ArrayList<String> = ArrayList()
    var createdByUser: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference

        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_IncidentsApp_FullScreenDialog)

        tagsFilter = requireArguments().getStringArrayList("tagsFilter") as ArrayList<String>
        createdByUser = requireArguments().getBoolean("createdByUser") as Boolean
        statusFilter = requireArguments().getStringArrayList("statusFilter") as ArrayList<String>

        populateTags()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v: View = inflater.inflate(R.layout.fragment_filter, container, false)

        recyclerView = v.findViewById(R.id.recyclerViewFilter)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        adapter.FilterAdapter(requireContext(),tags,tagsFilter)

        recyclerView.adapter = adapter

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view?.findViewById(R.id.toolbar_status)
        toolbar?.setNavigationOnClickListener { v: View? -> dismiss() }
        toolbar?.setTitle("Filtrar mapa")

        filterButton = view.findViewById(R.id.filterButton)
        filterButton.setOnClickListener(View.OnClickListener { v: View? ->
            tagsFilter = adapter.tagsSelected
            var intent : Intent = Intent()
            intent.putExtra("tagsFilter",tagsFilter)
            intent.putExtra("createdByUser", createdByUser)
            intent.putExtra("statusFilter", statusFilter as ArrayList<String>)
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK,intent)
            dismiss()
        })

        switchUser.isChecked = createdByUser
        switchPendientes.isChecked = statusFilter.contains(Status.PENDING.name)
        switchEnProceso.isChecked = statusFilter.contains(Status.INPROCESS.name)
        switchRechazadas.isChecked = statusFilter.contains(Status.REJECTED.name)
        switchFinalizadas.isChecked = statusFilter.contains(Status.FIXED.name)


        switchUser.setOnCheckedChangeListener { buttonView, isChecked ->
            createdByUser = isChecked
        }

        switchPendientes.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                statusFilter.add(Status.PENDING.name)
            } else {
                statusFilter.remove(Status.PENDING.name)
            }
        }

        switchEnProceso.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                statusFilter.add(Status.INPROCESS.name)
            } else {
                statusFilter.remove(Status.INPROCESS.name)
            }
        }

        switchRechazadas.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                statusFilter.add(Status.REJECTED.name)
            } else {
                statusFilter.remove(Status.REJECTED.name)
            }
        }

        switchFinalizadas.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                statusFilter.add(Status.FIXED.name)
            } else {
                statusFilter.remove(Status.FIXED.name)
            }
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

    // Añade tags a la base de datos y si ya existen, las obtiene
    private fun populateTags(){

        val refTags = database.child("tags")

        val tagsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    tags.clear()
                    snapshot.children.forEach {
                        val tag: Tag = it.getValue(Tag::class.java)!!
                        tags.add(tag)
                    }
                } else {
                    tags.clear()
                    tags.add(Tag(UUID.randomUUID().toString(), "Alumbrado", "icon_alumbrado", "#F6FF57"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Calzada", "icon_road", "#E7DAF4"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Contenedores", "icon_bin", "#BCFF8D"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Limpieza", "icon_clean", "#FFD78F"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Mobiliario", "icon_furniture", "#FF6756"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Señales y semáforos", "icon_traffics_lights", "#5781FF"))
                    tags.add(Tag(UUID.randomUUID().toString(), "Otros", "icon_others", "#CBB0F7"))

                    for (tag in tags) {
                        refTags.child(tag.id!!).setValue(tag)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAGFIRE", "loadPost:onCancelled", error.toException())
            }
        }

        refTags.orderByChild("name").addListenerForSingleValueEvent(tagsListener)

    }




}