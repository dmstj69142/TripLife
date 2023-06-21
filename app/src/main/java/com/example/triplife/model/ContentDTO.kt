package com.example.triplife.model

import androidx.room.PrimaryKey


//post 게시물 관련 관리
data class ContentDTO(
    var id: String? = null,
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var userId: String? = null, var userName: String? = null,
    var timestamp: Long? = null,

    //스크랩한 사용자 목록
    var scrapList: MutableList<String> = arrayListOf()

) {

    //댓글을 남겼을 때 데이터 관리를 위함
    data class Comment(
        var uid: String? = null,
        var userId: String? = null, var userName: String? = null,
        var id: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}


