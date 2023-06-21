package com.example.triplife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triplife.adapter.ContentListAdapter
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_1.view.*
import kotlinx.android.synthetic.main.fragment_2.*
import kotlinx.android.synthetic.main.fragment_2.view.*
import kotlinx.android.synthetic.main.fragment_3.view.*


class Fragment2 : Fragment() {


    var firestore : FirebaseFirestore? = null
    var uid: String? = null

    private val viewModel by viewModels<ContentListViewModel>()

    private val adapter = ContentListAdapter(
        onCommentClick = {
            showCommentView(it)
        },
        onDeleteContent = {
            deleteContent(it)
        }
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_2, container, false)
        //DB에 접근
        //추가2
        firestore = FirebaseFirestore.getInstance()

        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.fragment2_recyclerview.adapter = adapter
        view.fragment2_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupViewModel()
    }
    private fun setupViewModel() {
        viewModel.loadScrapList(uid)
        viewModel.contentList.observe(viewLifecycleOwner) {
            WLog.d("$it")
            adapter.submitList(it)
        }
    }
    private fun showCommentView(it: String) {
        val intent = Intent(requireContext(), CommentActivity::class.java)
        intent.putExtra(CommentActivity.CONTENT_ID, it)
        startActivity(intent)
    }

    private fun deleteContent(it: ContentDTO) {
        WLog.d("it.id ${it.id}")
        FirebaseFirestore.getInstance()
            .collection("images")
            .document(it.id.orEmpty())
            .delete()
            .addOnSuccessListener {
                WLog.d("success")
                viewModel.loadScrapList(uid)
            }
            .addOnFailureListener {
                WLog.e("$it")
            }
    }
}