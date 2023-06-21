package com.example.triplife

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.util.*

class ProfileActivity : AppCompatActivity() {
    lateinit var ivProfile: ImageView
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        SharedManager.init(applicationContext)
        initImageViewProfile()
    }

    @SuppressLint("NewApi")
    private fun initImageViewProfile() {
        databaseRef = FirebaseDatabase.getInstance().reference

        ivProfile = findViewById(R.id.ivProfile)

        val etPersonName: EditText = findViewById(R.id.editTextTextPersonName)

        val btnNickName: Button = findViewById(R.id.btn_nick_name)
        btnNickName.setOnClickListener {
            // 사용자 이름 변경 저장
            if (etPersonName.text.toString().isBlank()) {
                Toast.makeText(this@ProfileActivity, "입력필드가 비어있습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val myNewNickName = etPersonName.text.toString()

            if (SharedManager.read(SharedManager.USER_NAME, "")?.isNotEmpty() == true &&
                SharedManager.read(SharedManager.PROFILE_URL, "")?.isNotEmpty() == true &&
                SharedManager.read(SharedManager.MY_UID, "")?.isNotEmpty() == true) {

                // 기존에 닉네임을 수정한 적이 있으며, 커뮤니티 탭에도 접근 가능한 조건일 땐 채팅방 데이터가 있을 수 있으므로 채팅 데이터 쪽 닉네임도 다 변경해야함

                // Firebase database 에 기본 계정정보 db set
                databaseRef.child("UserInfo").child(myUid).child("uid").setValue(myUid)
                databaseRef.child("UserInfo").child(myUid).child("userName").setValue(etPersonName.text.toString())

                // 채팅 대화기록에 표시되는 닉네임 변경

              /* databaseRef.child("ChatRoomInfo").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount != 0L) {

                            // save shared pref
                            SharedManager.write(SharedManager.MY_UID, FirebaseAuth.getInstance().currentUser?.uid.toString())
                            SharedManager.write(SharedManager.USER_NAME, etPersonName.text.toString())

                            Toast.makeText(this@ProfileActivity, "사용자 이름이 저장 되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })

               */

            } else {
                // save shared pref
                SharedManager.write(SharedManager.MY_UID, FirebaseAuth.getInstance().currentUser?.uid.toString())
                SharedManager.write(SharedManager.USER_NAME, etPersonName.text.toString())

                Toast.makeText(this@ProfileActivity, "사용자 이름이 저장 되었습니다", Toast.LENGTH_SHORT).show()
            }
        }




        // get profile from firebase storage
        val storage = FirebaseStorage.getInstance()
        storage.reference.child("profiles" + File.separator + FirebaseAuth.getInstance().currentUser?.uid.toString() + ".png").downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(javaClass.simpleName, "이미지 로드 성공 !")
                Glide.with(this)
                    .load(it.result)
                    .into(ivProfile)

            } else {
                Log.e(javaClass.simpleName, "이미지 로드 에러 !")
            }
        }

        ivProfile.setOnClickListener {
            when {
                // 갤러리 접근 권한이 있는 경우
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                -> {
                    navigateGallery()
                }

                // 갤러리 접근 권한이 없는 경우
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                -> {
                    showPermissionContextPopup()
                }

                // 권한 요청 하기(requestPermissions) -> 갤러리 접근(onRequestPermissionResult)
                else -> requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }

        }
    }

    // 권한 요청 승인 이후 실행되는 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    navigateGallery()
                else
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                //
            }
        }
    }

    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        // 가져올 컨텐츠들 중에서 Image 만을 가져온다.
        intent.type = "image/*"
        // 갤러리에서 이미지를 선택한 후, 프로필 이미지뷰를 수정하기 위해 갤러리에서 수행한 값을 받아오는 startActivityForeResult를 사용한다.
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 예외처리
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            // 2000: 이미지 컨텐츠를 가져오는 액티비티를 수행한 후 실행되는 Activity 일 때만 수행하기 위해서
            2000 -> {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    ivProfile.setImageURI(selectedImageUri)

                    // save to firebase storage
                    setImageToStorage(selectedImageUri)
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setImageToStorage(cameraUri: Uri) {
        val fileName = "${FirebaseAuth.getInstance().currentUser?.uid}.png" // user auth token
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://test-628ed.appspot.com").child("profiles" + File.separator + fileName)
        storageRef.putFile(cameraUri).addOnSuccessListener {
            if (it.task.isSuccessful) {
                // stop loading progress
                Toast.makeText(this, "프로필 이미지가 수정되었습니다", Toast.LENGTH_SHORT).show()

                // get profile from firebase storage
                storageRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(javaClass.simpleName, "이미지 로드 성공 !")
                        Glide.with(this)
                            .load(it.result)
                            .into(ivProfile)

                        // save to firebase database
                        databaseRef.child("UserInfo").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("uid").setValue(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        databaseRef.child("UserInfo").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("profileUrl").setValue(it.result.toString())

                        // save shared pref
                        SharedManager.write(SharedManager.MY_UID, FirebaseAuth.getInstance().currentUser?.uid.toString())
                        SharedManager.write(SharedManager.PROFILE_URL, it.result.toString())
                    } else {
                        Log.e(javaClass.simpleName, "이미지 로드 에러 !")
                    }
                }
            }

        }.addOnFailureListener {
            // stop loading progress
            Toast.makeText(this, "프로필 이미지 업로드에 실패하였습니다\n잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NewApi")
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    private fun saveStore(){
        //add............................
        val user = mapOf(
            "profile" to ivProfile,
            "name" to editTextTextPersonName.text.toString(),
        )


        MyApplication.db.collection("user")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("kkang", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("kkang", "Error adding document", e)
            }

    }
}