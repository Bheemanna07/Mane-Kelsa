package com.kvsn.builds.cap1

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*

class MyAdaptor(private val ct: Context, private val al: ArrayList<Person>) : RecyclerView.Adapter<MyAdaptor.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val li = LayoutInflater.from(ct)
        val v = li.inflate(R.layout.my_layout, parent, false)
        return MyHolder(v)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val sharedPreferences = ct.getSharedPreferences("Getkey", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val p1 = al[position]
        holder.nameTv.text = p1.Name
        Log.d("1st check", "0")

        val key = p1.id

        holder.ratingTv.text = p1.Rating
        Log.d("2nd check", "1")

        if (p1.url != null) {
            Picasso.get().load(p1.url).into(holder.workImage)
        } else {
            holder.workImage.setImageResource(R.drawable.profile)
        }
        Log.d("3rd check", "2")

        holder.cardView.setOnClickListener {
            editor.putString("key", key)
            editor.apply()
            ct.startActivity(Intent(ct, Show_Available_worker_Profile::class.java))
        }
    }

    override fun getItemCount(): Int {
        return al.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var workImage: ImageView = itemView.findViewById(R.id.work_image)
        var nameTv: TextView = itemView.findViewById(R.id.worker_name)
        var ratingTv: TextView = itemView.findViewById(R.id.work_rating)
        var cardView: CardView = itemView.findViewById(R.id.cl)
    }
}
