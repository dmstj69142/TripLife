package com.example.triplife

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.triplife.adapter.ViewPagerAdapter
import com.example.triplife.databinding.ActivityAfterloginBinding
import com.example.triplife.model.UserInfo
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_afterlogin.*

class AfterloginActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityAfterloginBinding
    private lateinit var databaseRef: DatabaseReference

    var initTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterloginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //사진경로를 가져올 수 있는 권한을 줌
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        //메인화면으로 세팅(set default screen)
        bottomNavigationView.selectedItemId = R.id.home

        SharedManager.init(applicationContext)
        databaseRef = FirebaseDatabase.getInstance().reference

        // 페이저에 어댑터 연결
        binding.mainViewPager.adapter = ViewPagerAdapter(this)

        // 슬라이드하여 페이지가 변경되면 바텀네비게이션의 탭도 그 페이지로 활성화
        binding.mainViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.bottomNavigationView.menu.getItem(position).isChecked = true
                }
            }
        )


        databaseRef.child("UserInfo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                for (ds in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)
                    if (ds.key == myUid) {
                        SharedManager.write(SharedManager.MY_UID, myUid)
                        if (userInfo != null)
                            SharedManager.write(SharedManager.USER_NAME, userInfo.userName)
                        if (userInfo != null)
                            SharedManager.write(SharedManager.PROFILE_URL, userInfo.profileUrl)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })

        // 리스너 연결
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                // ViewPager의 현재 item에 첫 번째 화면을 대입
                binding.mainViewPager.currentItem = 0
                return true
            }
            R.id.post -> {
                // ViewPager의 현재 item에 네 번째 화면을 대입
                binding.mainViewPager.currentItem = 1
                return true
            }
            R.id.camera -> {
                // ViewPager의 현재 item에 세 번째 화면을 대입
                binding.mainViewPager.currentItem = 2
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.friend -> {
                // ViewPager의 현재 item에 두 번째 화면을 대입
                binding.mainViewPager.currentItem = 3
                return true
            }
            R.id.account -> {
                // ViewPager의 현재 item에 다섯 번째 화면을 대입
                binding.mainViewPager.currentItem = 4
                return true
            }

            else ->
                return false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode === KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==Fragment4.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}
