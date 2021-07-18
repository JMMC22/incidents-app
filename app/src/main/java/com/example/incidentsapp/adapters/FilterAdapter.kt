package com.example.incidentsapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Tag
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

/*
    Clase FilterAdapter: se encarga de enlazar los datos con el recyclerview.
 */
class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder>(){

    lateinit var context : Context

    var tags : MutableList<Tag> = ArrayList()
    var tagsSelected : ArrayList<String> = ArrayList()

    lateinit var listener : View.OnClickListener

    fun FilterAdapter(context: Context,tags : MutableList<Tag>, tagsSelected : ArrayList<String>){
        this.context = context
        this.tags = tags
        this.tagsSelected = tagsSelected
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var switchComp : SwitchMaterial = view.findViewById(R.id.switchView)

        fun bind(item : Tag, context : Context, tagsSelected: ArrayList<String>){
            switchComp.text = item.name
            if(tagsSelected.contains(item.id)){
                switchComp.isChecked = true
            } else {
                switchComp.isChecked = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_filter,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = tags.get(position)
        holder.bind(item,context,tagsSelected)
        holder.switchComp.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                item.id?.let { tagsSelected.add(it) }
            } else {
                item.id?.let { tagsSelected.remove(it) }
            }
        })
    }

    override fun getItemCount(): Int {
        return tags.count()
    }

}