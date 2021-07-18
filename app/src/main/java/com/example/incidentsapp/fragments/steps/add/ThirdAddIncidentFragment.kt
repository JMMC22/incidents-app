package com.example.incidentsapp.fragments.steps.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.adapters.TagAdapter
import com.example.incidentsapp.models.Tag
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class ThirdAddIncidentFragment : Fragment() {

    lateinit var recycler : RecyclerView
    val adapter : TagAdapter = TagAdapter()

    private lateinit var database: DatabaseReference

    var selectedItemPosition : Int = -1

    val tags = mutableListOf<Tag>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
        populateTags()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_add_incident, container, false)

        val cardView : CardView = view.findViewById(R.id.card_tag)

        recycler = view.findViewById(R.id.recycler_tag)
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter.TagAdapter(tags,requireContext())

        adapter.setOnClickListener(View.OnClickListener { v: View? ->

            var position : Int? = v?.let { recycler.getChildAdapterPosition(it) }

            arguments = Bundle().apply {
                putString("tag", tags[position!!].id)
            }

            adapter.selectedItemPosition = position!!
            adapter.notifyDataSetChanged()

        })

        recycler.adapter = adapter


        return view
    }

    private fun populateTags() {

        val refTags = database.child("tags")

        val tagsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tags.clear()
                    snapshot.children.forEach {
                        val tag: Tag = it.getValue(Tag::class.java)!!
                        tags.add(tag)
                    }
                } else {
                    tags.clear()
                    tags.add(
                        Tag(
                            UUID.randomUUID().toString(),
                            "Alumbrado",
                            "icon_alumbrado",
                            "#F6FF57"
                        )
                    )
                    tags.add(Tag(UUID.randomUUID().toString(), "Calzada", "icon_road", "#E7DAF4"))
                    tags.add(
                        Tag(
                            UUID.randomUUID().toString(),
                            "Contenedores",
                            "icon_bin",
                            "#BCFF8D"
                        )
                    )
                    tags.add(Tag(UUID.randomUUID().toString(), "Limpieza", "icon_clean", "#FFD78F"))
                    tags.add(
                        Tag(
                            UUID.randomUUID().toString(),
                            "Mobiliario",
                            "icon_furniture",
                            "#FF6756"
                        )
                    )
                    tags.add(
                        Tag(
                            UUID.randomUUID().toString(),
                            "Señales y semáforos",
                            "icon_traffics_lights",
                            "#5781FF"
                        )
                    )
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