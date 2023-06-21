package com.example.triplife

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.loadProfileImage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //storage 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //앨범 오픈
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //버튼에 이벤트 넣어줌
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }

        lifecycleScope.launch {
            profile_image.loadProfileImage()
        }

        profile_textview.text = auth?.currentUser?.email
    }

    //선택한 이미지를 받는 부분 구현
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
                //사진을 선택했을때 이미지의 경로가 이쪽으로 넘어옴
            } else {
                //취소 버튼을 눌렀을 때 작동하는 부분
                finish()
            }
        }
    }

    fun contentUpload() {
        //파일 이름 만들기

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //파일 업로드
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //이미지의 downloadUri 넣기
            contentDTO.imageUrl = uri.toString()

            //유저의 uid 넣기
            contentDTO.uid = auth?.currentUser?.uid

            //userid 넣기
            contentDTO.userId = auth?.currentUser?.email

            //content의 설명
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //시간
            contentDTO.timestamp = System.currentTimeMillis()
            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }
        Toast.makeText(baseContext, "게시물 업로드 성공", Toast.LENGTH_SHORT).show()
    }
}
