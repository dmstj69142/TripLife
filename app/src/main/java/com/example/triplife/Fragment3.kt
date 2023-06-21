package com.example.triplife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triplife.MyApplication.Companion.auth
import com.example.triplife.MyApplication.Companion.db
import com.example.triplife.Utils.FirebaseUtils
import com.example.triplife.adapter.ContentListAdapter
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_3.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Fragment3 : Fragment() {

    //추가1
    var firestore : FirebaseFirestore? = null
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
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_3, container, false)
        //DB에 접근
        //추가2
        firestore = FirebaseFirestore.getInstance()


        view.fragment3_recyclerview.adapter = adapter
        view.fragment3_recyclerview.layoutManager = LinearLayoutManager(activity)

        //검색
        view.keyword.setOnClickListener {
            val intent = Intent(requireContext(), SearchImageActivity::class.java)
            requireActivity().startActivity(intent)
        }
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
        viewModel.loadContentList()
        //추가 프로필 이미지 변경사항을 알려주기 위해 사용
        adapter.notifyDataSetChanged()
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
                viewModel.loadContentList()
            }
            .addOnFailureListener {
                WLog.e("$it")
            }
    }

}


