package com.example.incidentsapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.incidentsapp.fragments.steps.add.FirstAddIncidentFragment
import com.example.incidentsapp.fragments.steps.add.SecondAddIncidentFragment
import com.example.incidentsapp.fragments.steps.add.ThirdAddIncidentFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){

    //Cantidad fragments
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> FirstAddIncidentFragment()
            1 -> SecondAddIncidentFragment()
            2 -> ThirdAddIncidentFragment()
            else -> FirstAddIncidentFragment()
        }
    }


}