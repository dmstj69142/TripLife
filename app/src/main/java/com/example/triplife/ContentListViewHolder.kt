package com.example.triplife

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.Utils.FirebaseUtils
import com.example.triplife.databinding.ItemDetailBinding
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.loadImageUrl
import com.example.triplife.util.loadProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentListViewHolder(
    parent: ViewGroup,
    private val onCommentClick: (String) -> Unit,
    private val onDeleteContent: (ContentDTO) -> Unit
) : RecyclerView.ViewHolder(
    ItemDetailBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    ).root
) {

    private val binding = ItemDetailBinding.bind(itemView)


    fun bind(item: ContentDTO) {
        binding.detailviewitemCommentImageview.setOnClickListener {
            onCommentClick(item.id.orEmpty())
        }

        //추가
        //여기가 스크랩이미지뷰를 클릭했을 때 파이어베이스에 들어가도록 하는 부분
        binding.detailviewitemScrapImageview.setOnClickListener {
            scrapOrUnscrap(item)
        }
        binding.detailviewitemUnscrapImageview.setOnClickListener{
            scrapOrUnscrap(item)
        }

        //추가
        //스크랩한 콘텐츠 or 스크랩하지 않은 콘텐츠 구분
        if(item.scrapList.contains(FirebaseUtils.getUid())){
            binding.detailviewitemScrapImageview.visibility = View.GONE
            binding.detailviewitemUnscrapImageview.visibility = View.VISIBLE
        } else {
            binding.detailviewitemScrapImageview.visibility = View.VISIBLE
            binding.detailviewitemUnscrapImageview.visibility = View.GONE
        }


        //프로필 이미지 호출
        CoroutineScope(Dispatchers.Main).launch {
            binding.detailviewitemProfileImage.loadProfileImage(item.uid.orEmpty())
        }


        binding.detailviewitemProfileTextview.text = item.userId
        binding.detailviewitemImageviewContent.loadImageUrl(item.imageUrl.orEmpty())
        binding.detailviewitemExplainTextview.text = item.explain


        binding.delete.isVisible =
            item.uid.orEmpty() == FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        binding.delete.setOnClickListener {
            onDeleteContent(item)
        }
    }

    //스크랩 버튼 클릭시 이옹될 함수
    fun scrapOrUnscrap(item: ContentDTO){
        FirebaseUtils.db
            .collection("images")
            .whereEqualTo("imageUrl",item.imageUrl)
            .get().addOnSuccessListener {
                for(document in it.documents){


                    val scrapList = document.get("scrapList") as MutableList<String>

                    if(item.scrapList.contains(FirebaseUtils.getUid())) {
                        //이미 스크랩 한 경우
                        item.scrapList.remove(FirebaseUtils.getUid())
                        scrapList.remove(FirebaseUtils.getUid())

                        binding.detailviewitemScrapImageview.visibility = View.VISIBLE
                        binding.detailviewitemUnscrapImageview.visibility = View.GONE
                    } else {
                        item.scrapList.add(FirebaseUtils.getUid())
                        scrapList.add(FirebaseUtils.getUid())

                        binding.detailviewitemScrapImageview.visibility = View.GONE
                        binding.detailviewitemUnscrapImageview.visibility = View.VISIBLE
                    }


                    FirebaseUtils.db.collection("images").document(document.id).
                    update("scrapList",scrapList).addOnSuccessListener {
                        Toast.makeText(itemView.context, "성공", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(itemView.context, "실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}