package com.yamamz.earthquakemonitor.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yamamz.earthquakemonitor.mainMVP.MainActivity

import com.yamamz.earthquakemonitor.R
import com.yamamz.earthquakemonitor.model.EarthQuake


/**
 * Created by AMRI on 8/25/2017.
 */

open class QuakeAdapter(private val context: Context, private val earthquakes: ArrayList<EarthQuake>) : RecyclerView.Adapter<QuakeAdapter.myViewHolder>() {

    private val mBackground: Int

    init {
        val mTypedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true)
        mBackground = mTypedValue.resourceId
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.earthquake_list, parent, false)
        itemView.setBackgroundResource(mBackground)
        return myViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val (magnitude, location, time, dept) = earthquakes[position]

        holder.tv_magnitude.text =magnitude.toString()

        val mag=magnitude

        when(mag){
            in -1.0 .. 1.09 -> {
                holder.tv_magnitude.setTextColor(Color.parseColor("#ffffff"))
                holder.tv_magnitude.setBackgroundResource(R.drawable.circle)}

            in 1.1 .. 2.49 -> {
                holder.tv_magnitude.setTextColor(Color.parseColor("#ffffff"))
                holder.tv_magnitude.setBackgroundResource(R.drawable.circle_weak)}

            in 2.5 .. 4.49 ->{
                holder.tv_magnitude.setBackgroundResource(R.drawable.circle_moderate)
                holder.tv_magnitude.setTextColor(Color.parseColor("#000000"))
            }

            else -> {
                holder.tv_magnitude.setTextColor(Color.parseColor("#ffffff"))
                holder.tv_magnitude.setBackgroundResource(R.drawable.very_strong_circle)}


        }


        holder.tv_location.text = location
        holder.tv_time.text = time
        holder.tv_dept.text =  "Depth $dept km"

        holder.mView.setOnClickListener {
            (context as MainActivity).goToDetailsMap(position)
        }

    }

    override fun getItemCount(): Int {
        return earthquakes.size

    }

    inner class myViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        val tv_magnitude: TextView = mView.findViewById(R.id.tv_magnitude)
        val tv_location: TextView = mView.findViewById(R.id.tv_location)
        val tv_dept: TextView = mView.findViewById(R.id.tv_dept)
        val tv_time: TextView = mView.findViewById(R.id.tv_times)


    }


    // Clean all elements of the recycler
    fun clear() {
        earthquakes.clear()
        notifyDataSetChanged()
    }




}
