package com.example.triplife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.loadProfileImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_search_image.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_comment_imageview
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_explain_textview
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_imageview_content
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_profile_image
import kotlinx.android.synthetic.main.item_detail.view.detailviewitem_profile_textview
import kotlinx.android.synthetic.main.post.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchImageActivity : AppCompatActivity() {

    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var searchImageAdapter: SearchImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_image)

        images.apply {
            searchImageAdapter = SearchImageAdapter()
            adapter = searchImageAdapter
            layoutManager = LinearLayoutManager(this@SearchImageActivity)
        }

        back.setOnClickListener {
            finish()
        }

        keyword.addTextChangedListener {
            close.isVisible = it.toString().trim().isNotEmpty()

            searchImageAdapter.setContentWithChangedKeyword(it.toString())
        }

        close.setOnClickListener {
            keyword.setText("")
        }
    }

    override fun onStart() {
        super.onStart()

        searchImageAdapter.notifyDataSetChanged()
    }

    inner class SearchImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()
        val contentDTOsCopy = arrayListOf<ContentDTO>()
        val contentUidListCopy = arrayListOf<String>()

        init {
            firestore.collection("images").orderBy("timestamp").addSnapshotListener {querySnapshot,firebaseFireStoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    val contentDTO = snapshot.toObject(ContentDTO::class.java)
                    contentDTO?.let {
                        contentDTOs.add(it)
                        contentDTOsCopy.add(it)
                        contentUidList.add(snapshot.id)
                        contentUidListCopy.add(snapshot.id)
                    }
                }
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return  CustomViewHolder(view)
        }

        fun setContentWithChangedKeyword(keyword: String) {
            contentDTOsCopy.clear()
            contentUidListCopy.clear()

            for(index in 0 until contentDTOs.size) {
                val contentDTO = contentDTOs[index]
                val explain = contentDTO.explain

                explain?.let {
                    if(it.contains(keyword.trim())) {
                        contentDTOsCopy.add(contentDTO)
                        contentUidListCopy.add(contentUidList[index])
                    }
                } ?: run {
                    contentDTOsCopy.add(contentDTO)
                    contentUidListCopy.add(contentUidList[index])
                }
            }

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = contentDTOsCopy.size

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val viewHolder = (p0 as CustomViewHolder).itemView

            viewHolder.detailviewitem_profile_textview.text =
                contentDTOsCopy[p1].userId

            viewHolder.detailviewitem_explain_textview.text =
                contentDTOsCopy[p1].explain

            Glide.with(p0.itemView.context)
                .load(contentDTOsCopy[p1].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)
//
//             Glide.with(p0.itemView.context)
//                 .load(contentDTOsCopy[p1].imageUrl).circleCrop()
//                .into(viewHolder.detailviewitem_profile_image)
            CoroutineScope(Dispatchers.Main).launch {
                contentDTOsCopy[p1].uid?.let {
                    viewHolder.detailviewitem_profile_image.loadProfileImage(
                        it
                    )
                }
            }

            viewHolder.detailviewitem_comment_imageview.setOnClickListener{ v->
                Intent(v.context, CommentActivity::class.java).apply {
                    putExtra("contentUid", contentUidListCopy[p1])
                }.also {
                    startActivity(it)
                }
            }
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}