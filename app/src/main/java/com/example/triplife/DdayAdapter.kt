package com.example.triplife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.model.DataModel

abstract class DdayAdapter(private val mDataList: List<DataModel>) :
    RecyclerView.Adapter<DdayAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val finalDate: TextView = itemView.findViewById(R.id.finalDate)
        val tripday: TextView = itemView.findViewById(R.id.tripday)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_1, parent, false)
        return MyViewHolder(itemView)
    }
}

