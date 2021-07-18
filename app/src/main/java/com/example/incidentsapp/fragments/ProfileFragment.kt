package com.example.incidentsapp.fragments

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.*
import com.example.incidentsapp.adapters.BadgeAdapter
import com.example.incidentsapp.adapters.IncidentAdapter
import com.example.incidentsapp.adapters.TagAdapter
import com.example.incidentsapp.models.Badge
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Levels
import com.example.incidentsapp.models.User
import com.example.incidentsapp.utils.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_dialog.*
import kotlinx.android.synthetic.main.row_badge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val user = Firebase.auth.currentUser
    private lateinit var recyclerView: RecyclerView

    var adapter : IncidentAdapter = IncidentAdapter()
    var adapterBadges : BadgeAdapter = BadgeAdapter()

    var incidents: MutableList<Incident> = mutableListOf()
    var badges: MutableList<Badge> = mutableListOf()

    var firebaseData: FirebaseData = FirebaseData()
    lateinit var userData: User

    lateinit var front: AnimatorSet
    lateinit var back: AnimatorSet
    var isfront = true
    var position: Int = -1
    var lasPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseData.getIncidentsOfCurrentUser(object: FirebaseCallback{
            override fun onCallback(data: Any) {
                incidents.clear()
                incidents.addAll(data as MutableList<Incident>)
                adapter.notifyDataSetChanged()
            }
        })

        firebaseData.getBadgesOfCurrentUser(object: FirebaseCallback{
            override fun onCallback(data: Any) {
                badges.clear()
                badges.addAll(data as MutableList<Badge>)
                adapterBadges.notifyDataSetChanged()
            }
        })

        userData = (activity as MapsActivity).userData

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val avatar : ImageView = view.findViewById(R.id.avatar)

        if(user?.photoUrl != null) {
            Picasso.get().load(user?.photoUrl).into(avatar)
        } else {
            avatar.setImageResource(R.drawable.avatar)
        }

        recyclerView = view.findViewById(R.id.recyclerProfile)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.IncidentAdapter(incidents,requireContext())
        adapterBadges.BadgeAdapter(badges,requireContext())

        adapter.setOnClickListener(View.OnClickListener { v: View? ->

            // Incident adapter
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


        exit_button.setOnClickListener(View.OnClickListener { v: View? ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Está seguro de cerrar sesión?")
                .setNegativeButton("Cancelar"){dialog, which ->  }
                .setPositiveButton("Aceptar"){dialog, which ->
                    Firebase.auth.signOut()
                    requireActivity().finish()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                }
                .show()
        })

        incidentsButton.setOnClickListener {v: View? ->
            recyclerView.adapter = adapter
        }

        insigniasButton.setOnClickListener {v: View? ->
            recyclerView.adapter = adapterBadges
        }

        pxProgressBar.progress = Levels().getPercentOfLevel((userData.px)?.toInt() ?: 0)
        level_textView.text = userData.level

        adapterBadges.setOnClickListener(View.OnClickListener {
            position = it.let { recyclerView.getChildAdapterPosition(it) }

            if (position == lasPosition || lasPosition == -1) {
                animateCard(it)
            } else {
                var lastPositionView = recyclerView.get(lasPosition)
                if(!isfront){
                    front.setTarget(lastPositionView!!.findViewById<CardView>(R.id.back_card))
                    back.setTarget(lastPositionView!!.findViewById<CardView>(R.id.front_card))
                    back.start()
                    front.start()
                    isfront = true
                }
                animateCard(it)
            }
            Log.d(TAG, position.toString())
        })

    }

    fun animateCard(v: View) {
        val scale: Float = requireContext().applicationContext.resources.displayMetrics.density
        v!!.findViewById<CardView>(R.id.front_card).cameraDistance = 8000 * scale
        v!!.findViewById<CardView>(R.id.back_card).cameraDistance = 8000 * scale

        front = AnimatorInflater.loadAnimator(
            requireContext().applicationContext,
            R.animator.front_card_animator
        ) as AnimatorSet
        back = AnimatorInflater.loadAnimator(
            requireContext().applicationContext,
            R.animator.back_card_animator
        ) as AnimatorSet

        if (isfront) {
            front.setTarget(v!!.findViewById<CardView>(R.id.front_card))
            back.setTarget(v!!.findViewById<CardView>(R.id.back_card))
            front.start()
            back.start()
            isfront = false
        } else {
            front.setTarget(v!!.findViewById<CardView>(R.id.back_card))
            back.setTarget(v!!.findViewById<CardView>(R.id.front_card))
            back.start()
            front.start()
            isfront = true
        }

        lasPosition = position
    }


    companion object {
        private const val TAG = "ProfileFragment"
    }
}