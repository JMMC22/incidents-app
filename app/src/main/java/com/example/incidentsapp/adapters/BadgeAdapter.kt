package com.example.incidentsapp.adapters

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Badge

/*
    Clase IncidentAdapter: se encarga de enlazar los datos con el recyclerview.
 */
class BadgeAdapter : RecyclerView.Adapter<BadgeAdapter.ViewHolder>(), View.OnClickListener{

    lateinit var context : Context
    var positionBad: Int = 0

    var badges : MutableList<Badge> = ArrayList()

    lateinit var front: AnimatorSet
    lateinit var back: AnimatorSet
    var isfront = true

    lateinit var listener: View.OnClickListener


    fun BadgeAdapter(badges : MutableList<Badge>, context: Context){
        this.context = context
        this.badges = badges
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title_textView)
        var description: TextView = view.findViewById(R.id.description_textView)
        var actual: TextView = view.findViewById(R.id.actual_textView)
        var end: TextView = view.findViewById(R.id.last_textView)
        var icon: ImageView = view.findViewById(R.id.badge_imageView)

        fun bind(item: Badge, context: Context, position: Int){
            title.text = item.title
            description.text = item.description
            actual.text = item.actualCount + "/"
            end.text = item.maxCount
            if(item.done!!) {
                icon.setColorFilter(ContextCompat.getColor(context, R.color.secondary))
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_badge,parent,false)
        view.setOnClickListener(this)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = badges.get(position)
        holder.bind(item,context, position)
    }

    override fun getItemCount(): Int {
        return badges.count()
    }

    public fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if(listener!=null){listener.onClick(v)}
    }
}



