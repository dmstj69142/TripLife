package com.example.triplife.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.AlbumActivity
import com.example.triplife.R
import com.example.triplife.model.User


class UserAdapter: RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
    private var userList: ArrayList<User> = ArrayList<User>()

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val titleText: TextView = itemView.findViewById(R.id.title_text)
    }

    //화면 설정
    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item,parent,false)
        return MyViewHolder(view)
    }
    //데이터 설정
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.titleText.text = userList[holder.adapterPosition].title

        //intent 이용해서
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, AlbumActivity::class.java)
            //추가: 목록 구분을 위해, tripDetailId 를 추가
            intent.putExtra("tripDetailId",userList[holder.adapterPosition].title.toString())
            holder.itemView.context.startActivity(intent)
        }

    }

    fun getUserList(): ArrayList<User> {
        return userList
    }

    //아이템 갯수
    override fun getItemCount(): Int{
        return userList.size
    }
    //아이템 등록
    fun setUserList(userList: ArrayList<User>){
        this.userList = userList
        notifyDataSetChanged()
    }
    //사용자 삭제
    fun deleteUser(position: Int){
        this.userList.removeAt(position)
    }
}