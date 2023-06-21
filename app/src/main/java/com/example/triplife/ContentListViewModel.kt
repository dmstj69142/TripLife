package com.example.triplife

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ContentListViewModel : ViewModel() {

    private var isLoaded = false

    private val _contentList = MutableLiveData<List<ContentDTO>>()
    val contentList: LiveData<List<ContentDTO>>
        get() = _contentList

    fun loadContentList(id: String = "") {
        isLoaded = false
        FirebaseFirestore.getInstance()
            .collection("images")
            .run {
                if (id.isNotEmpty()) {
                    whereEqualTo("uid", id)
                } else this
            }
            .orderBy("timestamp", Query.Direction.DESCENDING)// 타임스탬프를 내림차순으로 정렬
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (isLoaded) return@addSnapshotListener
                isLoaded = true
                WLog.d("querySnapshot $querySnapshot firebaseFirestoreException $firebaseFirestoreException")

                if (querySnapshot == null) {
                    if (firebaseFirestoreException != null) {
                        WLog.e("$firebaseFirestoreException")
                    }
                    _contentList.value = emptyList()
                } else {
                    _contentList.value = querySnapshot.documents.mapNotNull { snapshot ->
                        snapshot.toObject(ContentDTO::class.java)?.apply {
                            this.id = snapshot.id
                        }
                    }
                }
            }
    }

    //추가:
    //특정 id를 이용하여, 스크랩한 게시물을 가져오기 위해 사용
    fun loadScrapList(id: String? = ""){
        if(id == null){
            return
        }
            isLoaded = false
            FirebaseFirestore.getInstance()
                .collection("images")
                .whereArrayContains("scrapList",id)
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (isLoaded) return@addSnapshotListener
                    isLoaded = true
                    WLog.d("querySnapshot $querySnapshot firebaseFirestoreException $firebaseFirestoreException")

                    if (querySnapshot == null) {
                        if (firebaseFirestoreException != null) {
                            WLog.e("$firebaseFirestoreException")
                        }
                        _contentList.value = emptyList()
                    } else {
                        _contentList.value = querySnapshot.documents.mapNotNull { snapshot ->
                            snapshot.toObject(ContentDTO::class.java)?.apply {
                                this.id = snapshot.id
                            }
                        }
                    }
                }

    }
}
