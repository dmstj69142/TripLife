package com.example.triplife


import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.triplife.model.ContentDTO
import com.example.triplife.util.WLog
import com.example.triplife.util.dp
import com.example.triplife.util.getProfileImage
import com.example.triplife.util.loadProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo2.*
import kotlinx.android.synthetic.main.fragment_4.*
import kotlinx.android.synthetic.main.fragment_4.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Fragment4 : Fragment() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null

    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    companion object{
        var PICK_PROFILE_FROM_ALBUM =10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_4, container, false)
        uid = FirebaseAuth.getInstance().currentUser?.uid
        //DB에 접근
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fragmentView?.account_recyclerview?.adapter = Fragment4RecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(requireActivity(), 3)

        //내정보에 있는 프로필 클릭하면 프로필 수정할 수 있음
        fragmentView?.account_iv_profile?.setOnClickListener{
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type="image/*"
            photoPickerLauncher.launch(photoPickerIntent)
        }
        CoroutineScope(Dispatchers.Main).launch {
            fragmentView?.account_iv_profile?.loadProfileImage(uid.toString())
        }
        return fragmentView
    }

    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val photoUri: Uri? = data.data
                if (photoUri != null) {
                    uploadProfileImageToFirestore(photoUri)
                }
            }
        } else {
            // 취소 눌렀을 때
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    inner class Fragment4RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            //나의 사진만 찾기
            WLog.d("uid $uid")
            firestore?.collection("images")
                ?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    //get data
                    for (snapshot in querySnapshot?.documents!!) {
                        val toObject = snapshot.toObject(ContentDTO::class.java)
                        WLog.d("toObject ${toObject?.imageUrl}")
                        contentDTOs.add(toObject!!)
                    }

                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val imageView = ImageView(p0.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            imageView.setPadding(2.dp.toInt())
            return CustomViewHolder(imageView)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val imageView = (p0 as CustomViewHolder).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageView)



            imageView.setOnClickListener {
                val intent = Intent(it.context, ContentListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu1, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu1 ->
                logout()
            R.id.menu2 ->
                findPassword()
        }
        return super.onOptionsItemSelected(item)
    }

    fun findPassword() {
        MyApplication.auth.sendPasswordResetEmail(MyApplication.email.toString())
            ?.addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Toast.makeText(requireContext(), "비밀번호 변경, 전송된 메일을 확인해 주세요",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "메일 발송 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logout() {
        MyApplication.auth.signOut()
        MyApplication.email = null
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }

    private fun uploadProfileImageToFirestore(photoUri: Uri) {
        // 이미지를 업로드할 Firestore 컬렉션 및 문서 경로 설정
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val storageRef = FirebaseStorage.getInstance().reference
        val profileRef = storageRef.child("profiles/$userId.png")

        // 이미지 업로드
        val uploadTask = profileRef.putFile(photoUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            profileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UPLOAD_SUCCESS", "프로필 이미지 업로드 성공!")
                // 업로드 성공 처리를 수행하는 로직 추가


                CoroutineScope(Dispatchers.Main).launch {
                    fragmentView?.account_iv_profile?.loadProfileImage(uid.toString())
                }
            } else {
                Log.e("UPLOAD_ERROR", "프로필 이미지 업로드 실패: ${task.exception}")
                // 업로드 실패 처리를 수행하는 로직 추가
            }
        }
    }


}
