package com.example.incidentsapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Incident
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

/*
    Clase IncidentAdapter: se encarga de enlazar los datos con el recyclerview.
 */
class IncidentAdapter : RecyclerView.Adapter<IncidentAdapter.ViewHolder>(), View.OnClickListener{

    lateinit var context : Context

    var incidents : MutableList<Incident> = ArrayList()

    lateinit var listener : View.OnClickListener

    fun IncidentAdapter(incidents : MutableList<Incident>, context: Context){
        this.context = context
        this.incidents = incidents
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var incidentImage: ImageView = view.findViewById(R.id.icono_incident)
        var incidentDescription: TextView = view.findViewById(R.id.description_incident)

        fun bind(item :Incident, context : Context){
            incidentDescription.text = item.description
            Picasso.get().load(item?.image).into(incidentImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_incident,parent,false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = incidents.get(position)
        holder.bind(item,context)
    }

    override fun getItemCount(): Int {
        return incidents.count()
    }

    public fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if(listener!=null){listener.onClick(v)}
    }
}