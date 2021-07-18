package com.example.incidentsapp.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Tag
import kotlin.collections.ArrayList

/*
    Clase TagAdapter: se encarga de enlazar los datos con el recyclerview.
 */

class TagAdapter : RecyclerView.Adapter<TagAdapter.ViewHolder>(), View.OnClickListener{

    lateinit var context : Context

    var tags : MutableList<Tag> = ArrayList()
    var selectedItemPosition : Int = -1

    lateinit var listener : View.OnClickListener

    fun TagAdapter(tags : MutableList<Tag>, context: Context){
        this.context = context
        this.tags = tags
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tagName : TextView = view.findViewById(R.id.name_tag)
        val tagIcon : ImageView = view.findViewById(R.id.icono_incident)
        val cardView : CardView = view.findViewById(R.id.cardViewTag)

        fun bind(item :Tag, context : Context){
            tagName.text = item.name
            val icon = item.icon
            val oval = OvalShape()
            val shape = ShapeDrawable(oval)
            shape.paint.color = Color.parseColor(item.colorBubble)
            tagIcon.setBackground(shape)
            tagIcon.setImageResource(context.resources.getIdentifier(icon,"drawable",context.packageName))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_tag,parent,false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tags.get(position)
        holder.bind(item,context)
        if(selectedItemPosition == position){
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.primary))
        }else{
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.white))
        }
    }

    override fun getItemCount(): Int {
        return tags.count()
    }

    public fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if(listener!=null){listener.onClick(v)}
    }
}