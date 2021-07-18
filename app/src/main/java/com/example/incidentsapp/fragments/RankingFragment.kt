package com.example.incidentsapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.R
import com.example.incidentsapp.adapters.IncidentAdapter
import com.example.incidentsapp.adapters.RankingAdapter
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.models.User
import com.google.android.material.tabs.TabLayout

class RankingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tab: TabLayout

    var adapter : RankingAdapter = RankingAdapter()
    var users: MutableList<User> = mutableListOf()
    var usersPerIncidents: MutableList<User> = mutableListOf()
    var usersPerBadges: MutableList<User> = mutableListOf()

    var firebaseData: FirebaseData = FirebaseData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBestUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tab = view.findViewById(R.id.tabRanking)


        recyclerView = view.findViewById(R.id.recyclerRanking)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.RankingAdapter(users,requireContext(), "users")
        recyclerView.adapter = adapter



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
                    0 ->  { adapter.users = users
                            adapter.tab = "users" }
                    1 -> { adapter.users = usersPerIncidents
                            adapter.tab = "usersPerIncidents" }
                    2 -> { adapter.users = usersPerBadges
                            adapter.tab = "usersPerBadges" }
                }
                adapter.notifyDataSetChanged()
            }

        })

    }

    fun getBestUsers() {
        firebaseData.getUsersBestPx(object: FirebaseCallback {
            override fun onCallback(data: Any) {
                var list = (data as MutableList<*>)

                users.clear()
                usersPerBadges.clear()
                usersPerIncidents.clear()

                users.addAll(list.get(0) as MutableList<User>)
                usersPerIncidents.addAll(list.get(1) as MutableList<User>)
                usersPerBadges.addAll(list.get(2) as MutableList<User>)

                adapter.notifyDataSetChanged()
            }
        })
    }

    companion object {

    }
}