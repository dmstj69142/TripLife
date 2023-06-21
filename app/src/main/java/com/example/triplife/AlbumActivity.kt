package com.example.triplife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.triplife.MyApplication.Companion.auth
import com.example.triplife.SharedManager.clear
import com.example.triplife.model.PhotoDTO
import com.example.triplife.util.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.core.RepoManager.clear
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_afterlogin.*
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.album_item.view.*


class AlbumActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    //추가: 목록 구분을 위해, tripDetailId 추가
    var tripDetailId: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        //추가: 목록 구분을 위해, tripDetailId 추가
        tripDetailId = intent.getStringExtra("tripDetailId")

        album_recyclerview.adapter = AlbumRecyclerViewAdapter()
        album_recyclerview.layoutManager = LinearLayoutManager(this)

        // 클릭 리스너
        camera_btn.setOnClickListener {

            //추가: 목록 구분을 위해, tripDetailId 를 추가
            val intent = Intent(this, AddPhoto2Activity::class.java)
            intent.putExtra("tripDetailId",tripDetailId)
            startActivity(intent)

        }
    }

    inner class AlbumRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var photoDTOs: ArrayList<PhotoDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()
       // var listenerRegistration: ListenerRegistration? = null

        init {

            //변경 : tripDetailId를 부여하여, 목록별 구분
            //변경 : uid를 부여하여, 사용자 구분
             FirebaseFirestore.getInstance().collection("images2").
             whereEqualTo("tripDetailId",tripDetailId).
             whereEqualTo("uid",uid).
             get().addOnSuccessListener{ querySnapshot  ->
                        photoDTOs.clear()
                        contentUidList.clear()
                        for (snapshot in querySnapshot.documents) {
                            val item = snapshot.toObject(PhotoDTO::class.java)
                            photoDTOs.add(item!!)
                            contentUidList.add(snapshot.id)
                        }
                    notifyDataSetChanged()
                }.addOnFailureListener{exception ->
                     Log.d("ERROR",exception.message.toString())
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.album_item, p0, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return photoDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            // UserId

            // Image
            Glide.with(holder.itemView.context).load(photoDTOs!![position].imageUrl)
                .into(viewholder.album_imageview)



            }
        }
    }




