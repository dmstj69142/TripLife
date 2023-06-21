package com.example.triplife

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.triplife.MyApplication.Companion.auth
import com.example.triplife.MyApplication.Companion.storage
import com.example.triplife.model.PhotoDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo2.*
import kotlinx.android.synthetic.main.activity_album.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhoto2Activity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var tripDetailId: String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo2)

        //스토리지 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //추가: tripDetailId를 추가하여 목록 구분
        tripDetailId = intent.getStringExtra("tripDetailId")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1

            )
        } else {
            //앨범 오픈
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        }

        //버튼 이벤트
        addphoto2_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    //선택한 이미지 받는 부분
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            //사진을 선택할때 이미지 경로가 여기루
            photoUri = data?.data
            addphoto2_image.setImageURI(photoUri)
        } else {
            //취소 눌렀을 때 작동하는 부분입니당
            finish()
        }
    }

    fun contentUpload() {
        //파일명 만들기
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE2_" + timestamp + "_.png"

        if (photoUri != null) {
            var storageRef = storage?.reference?.child("images2")?.child(imageFileName)
            storageRef?.putFile(photoUri!!)
                ?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                    return@continueWithTask storageRef.downloadUrl
                }?.addOnSuccessListener { uri ->
                    val photoDTO = PhotoDTO()

                    //이미지의 downloadUri 넣기
                    photoDTO.imageUrl = uri.toString()

                    //uid 넣기
                    photoDTO.uid = auth?.currentUser?.uid

                    photoDTO.userId = auth?.currentUser?.email

                    photoDTO.timestamp = System.currentTimeMillis()

                    //추가: tripDetailId
                    photoDTO.tripDetailId = tripDetailId

                    //파이어스토어에 사진 데이터 저장
                    firestore?.collection("images2")?.add(photoDTO)
                        ?.addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "사진이 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(this, "사진 업로드에 실패했습니다.다시 시도해주세요.", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                }else{
                Toast.makeText(this, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        setResult(Activity.RESULT_OK)
        finish()
        }
    }




