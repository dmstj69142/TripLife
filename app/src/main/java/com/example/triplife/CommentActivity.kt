package com.example.triplife


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.databinding.ItemCommentBinding
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.example.triplife.util.loadProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*


class CommentActivity : AppCompatActivity() {
    private var contentUid: String? = null

    //댓글 삭제
    private val onDeleteComment: (ContentDTO.Comment) -> Unit = {
        val commentId = it.id
        if (!commentId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("images").document(contentUid!!)
                .collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener {
                    commentAdapter.loadComments()
                }
                .addOnFailureListener {
                    WLog.e("$it")
                }
        }
    }

    private val commentAdapter by lazy {
        CommentRecyclerviewAdapter(onDeleteComment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra(CONTENT_ID)
        comment_recyclerview.adapter = commentAdapter
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        comment_btn_send?.setOnClickListener {
            val comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance()
                .collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)

            comment_edit_message.setText("")
        }
    }
//CommentRecyclerviewAdapter 선언
    inner class CommentRecyclerviewAdapter(private val onDeleteComment: (ContentDTO.Comment) -> Unit) :
        RecyclerView.Adapter<CommentRecyclerviewAdapter.CustomViewHolder>() {

        private var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            loadComments()
        }

        fun loadComments() {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    val elements = querySnapshot.documents.mapNotNull { snapshot ->
                        snapshot.toObject(ContentDTO.Comment::class.java)?.apply {
                            this.id = snapshot.id
                        }
                    }
                    comments.addAll(elements)
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
            return CustomViewHolder(p0, onDeleteComment)
        }

        inner class CustomViewHolder(
            parent: ViewGroup,
            private val onDeleteComment: (ContentDTO.Comment) -> Unit
        ) : RecyclerView.ViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).root
        ) {
            private val binding = ItemCommentBinding.bind(itemView)

            fun bind(item: ContentDTO.Comment) {
                WLog.d("ContentDTO $item")
                binding.commentviewitemTextviewComment.text = item.comment
                binding.commentviewitemTextviewProfile.text = item.userId

                CoroutineScope(Dispatchers.Main).launch {
                    binding.commentviewitemImageviewsProfile.loadProfileImage(item.uid.orEmpty())
                    cancel()
                }

                binding.delete.isVisible =
                    item.uid.orEmpty() == FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                binding.delete.setOnClickListener {
                    onDeleteComment(item)
                }
            }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.bind(comments[position])
        }
    }

    companion object {
        const val CONTENT_ID = "content_id"
    }
}