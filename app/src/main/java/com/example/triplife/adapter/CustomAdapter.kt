package com.example.triplife.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.R
import com.example.triplife.model.HouseKeepingData

class CustomAdapter(private val context: Context, private val dataList: ArrayList<HouseKeepingData>)
    : RecyclerView.Adapter<ItemViewHolder>(){
    //RecyclerView에 binding 해줄 Adapter를 연결시킨다.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_item_layout,parent, false)
        return ItemViewHolder(view)

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataList[position], context)
        //recycleView에 있는 각각의 아이템과 아이템의 위치
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private val Rcpurpose = itemView.findViewById<TextView>(R.id.Rcpurpose)
    private val RcMoney = itemView.findViewById<TextView>(R.id.RcMoney)
    private val RcMemo = itemView.findViewById<TextView>(R.id.RcMemo)
    private val RcDate = itemView.findViewById<TextView>(R.id.RCdate)


    //data -> resource (binding)
    fun bind(dataVo: HouseKeepingData, context: Context){


        //TextView 데이터 세팅 작업
        Rcpurpose.text =dataVo.purpose
        RcMoney.text = dataVo.money.toString()
        RcMemo.text = dataVo.memo
        RcDate.text = dataVo.date

    }

}