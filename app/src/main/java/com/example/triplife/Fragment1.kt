package com.example.triplife

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.triplife.model.DataModel
import com.example.triplife.model.User
import com.example.triplife.adapter.UserAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_1.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class Fragment1 : Fragment() {

    private lateinit var insertBtn: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    val dataModelList = mutableListOf<DataModel>()
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var userList: ArrayList<User> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_1, container, false)

        insertBtn = view.findViewById(R.id.insert_btn)
        insertBtn.setOnClickListener {
            val intent = Intent(requireContext(), InsertActivity2::class.java)
            activityResult.launch(intent)
        }

        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter()
        recyclerView.adapter = adapter

        // Set up item touch helper for swipe gestures
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder:RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ):Boolean{
                return false
            }

            override fun onSwiped(viewHolder:RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                when(direction){
                    ItemTouchHelper.LEFT->{
                        val uid: Int?=adapter.getUserList()[position].id
                        val uTitle:String?=adapter.getUserList()[position].title

                        val user= User(uid, uTitle)

                        //아이템 삭제
                        adapter.deleteUser(position)

                        //아이템 삭제화면 재정리
                        adapter.notifyItemRemoved(position)

                        //DB생성
                        val db = AppDatabase.getDatabase(requireContext())

                        //삭제 쿼리
                        db?.userDao()?.deleteUser(user)
                    }
                }
            }
            //스와이프 기능 커스터마이즈
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                    //스와이프 컬러
                    RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.TRANSPARENT)
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("삭제")
                        .setSwipeLeftLabelColor(Color.BLACK)
                        .create()
                        .decorate()

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

        itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        //사용자 조회
        loadUserList()
        return view
    }

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 돌아온 값이 OK라면
                // 사용자 조회
                loadUserList()
            }
        }

    // 사용자 조회
    private fun loadUserList() {
        val db = AppDatabase.getDatabase(requireContext())
        val userList: List<User>? = db?.userDao()?.getAllUser()
        if (userList != null) {
            adapter.setUserList(userList as ArrayList<User>)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = Firebase.database
        val myRef = database.getReference("dday")


        val endButton: ImageButton = requireView().findViewById(R.id.button1)

        var endDate = ""


        val now = System.currentTimeMillis()
        val today2 = Date(now)
        val t_dateFormat = SimpleDateFormat("yyyyMMdd", Locale("ko", "kr"))
        val todayDate = t_dateFormat.format(today2).toString()
        Log.d("todaydate", todayDate)

        val calendar_end = Calendar.getInstance()

        myRef.child(Firebase.auth.currentUser!!.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataModelList.clear()

                for (dataModel in snapshot.children) {
                    Log.d("Data", dataModel.toString())
                    // dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        endButton.setOnClickListener {

            val today = GregorianCalendar()
            val year = today.get(Calendar.YEAR)
            val month = today.get(Calendar.MONTH)
            val day = today.get(Calendar.DATE)


            val dlg =
                DatePickerDialog(requireContext(), object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {

                        endDate = "${year}${month}${dayOfMonth}"
                        Log.d("day: ", endDate)


                        calendar_end.set(year, month, dayOfMonth)

                        calendar_end.timeInMillis


                        val tripdate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).format(calendar_end.timeInMillis).toString()
                        Log.d("day: ", tripdate)
                        val tripday = requireView().findViewById<TextView>(R.id.tripday)
                        tripday?.setText(tripdate)
                        val textArea = requireView().findViewById<TextView>(R.id.finalDate)
                        // textArea.setText((tripdate.toInt() - todayDate.toInt()).toString())


                        val finalDay = TimeUnit.MILLISECONDS.toDays(calendar_end.timeInMillis - now)
                        textArea?.setText(finalDay.toString())

                        val database = Firebase.database
                        val myRef =
                            database.getReference("dday").child(Firebase.auth.currentUser!!.uid)
                        val model = DataModel(tripdate, finalDay.toString())

                        myRef
                            .setValue(model)
                    }

                }, year, month, day)
            dlg.show()


        }

        suppliesBtn.setOnClickListener() {
            val intent = Intent(requireContext(), SuppliesActivity::class.java)
            startActivity(intent)
        }

        planBtn.setOnClickListener() {
            // 여행계획
            val intent = Intent(requireContext(), MemoActivity::class.java)
            startActivity(intent)
        }
        bookBtn.setOnClickListener() {
            // 가계부
            val intent = Intent(requireContext(), BookActivity::class.java)
            startActivity(intent)
        }
    }
}



