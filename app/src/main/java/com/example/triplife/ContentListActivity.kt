package com.example.triplife

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.triplife.adapter.ContentListAdapter
import com.example.triplife.databinding.ActivityContentListBinding
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentListBinding

    private val viewModel by viewModels<ContentListViewModel>()

    private val myId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val adapter = ContentListAdapter(
        onCommentClick = {
            showCommentView(it)
        },
        onDeleteContent = {
            deleteContent(it)
        }
    )

    private fun deleteContent(it: ContentDTO) {
        WLog.d("it.id ${it.id}")
        FirebaseFirestore.getInstance()
            .collection("images")
            .document(it.id.orEmpty())
            .delete()
            .addOnSuccessListener {
                WLog.d("success")
                viewModel.loadContentList(myId)
            }
            .addOnFailureListener {
                WLog.e("$it")
            }
    }

    private fun showCommentView(it: String) {
        val intent = Intent(this@ContentListActivity, CommentActivity::class.java)
        intent.putExtra(CommentActivity.CONTENT_ID, it)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loadContentList(myId)
        setupView()
        setupViewModel()
    }

    private fun setupView() {
        binding.list.adapter = adapter

        title = "게시글"
    }

    private fun setupViewModel() {
        viewModel.contentList.observe(this@ContentListActivity) {
            adapter.submitList(it)
        }
    }
}