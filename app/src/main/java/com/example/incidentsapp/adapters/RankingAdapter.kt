package com.example.incidentsapp.adapters

import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.User
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import kotlin.collections.ArrayList

/*
    Clase IncidentAdapter: se encarga de enlazar los datos con el recyclerview.
 */
class RankingAdapter : RecyclerView.Adapter<RankingAdapter.ViewHolder>(){

    lateinit var context : Context

    var users : MutableList<User> = ArrayList()
    var tab: String? = null

    fun RankingAdapter(users : MutableList<User>, context: Context, tab: String){
        this.context = context
        this.users = users
        this.tab = tab
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var username: TextView = view.findViewById(R.id.username_textView)
        var positionText: TextView = view.findViewById(R.id.position_textView)
        var px: TextView = view.findViewById(R.id.px_textView)
        var level: TextView = view.findViewById(R.id.levelrank_textView)
        var crown: ImageView = view.findViewById(R.id.crown_imageView)

        fun bind(item: User, context: Context, position: Int, tab: String){
            positionText.text = (position + 1).toString()
            username.text = item.email!!.split("@")[0]

            when(position) {
                0 -> {
                    crown.visibility = View.VISIBLE
                    crown.setColorFilter(ContextCompat.getColor(context, R.color.secondary))
                }
                1 -> {
                    crown.visibility = View.VISIBLE
                    crown.setColorFilter(Color.LTGRAY)
                }
                2 -> {
                    crown.visibility = View.VISIBLE
                    crown.setColorFilter(Color.parseColor("#cd7f32"))
                }
                else -> {
                    crown.visibility = View.INVISIBLE
                }
            }

            when(tab){
                "users" -> {
                    px.visibility = View.VISIBLE
                    px.text = item.px + " px"
                    level.text = item.level
                }
                "usersPerIncidents" -> {
                    px.visibility = View.INVISIBLE
                    level.text = item.incidentsN
                }
                "usersPerBadges" -> {
                        level.text = item.badgesN
                        px.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = users.get(position)
        holder.bind(item,context, position, tab!!)
    }

    override fun getItemCount(): Int {
        return users.count()
    }

}