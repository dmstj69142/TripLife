package com.example.triplife

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.triplife.adapter.ListViewAdapterSupplies
import com.example.triplife.databinding.ActivitySuppliesBinding
import com.example.triplife.model.DataModelsupplies
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_supplies.*

class SuppliesActivity : AppCompatActivity() {
    val dataModelList = mutableListOf<DataModelsupplies>()
    lateinit var binding: ActivitySuppliesBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplies)

        binding = ActivitySuppliesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val myRef = database.getReference("mySupplies")


        val listView = findViewById<ListView>(R.id.listViewSupplies)

        val adapter_list = ListViewAdapterSupplies(dataModelList)

        listView.adapter = adapter_list

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent,
                                                                         view,
                                                                         position,
                                                                         id ->
            val selectItem = parent.getItemAtPosition(position) as DataModelsupplies

            selectItem.suppliesMemo

            dataModelList.removeAt(position)
            adapter_list.notifyDataSetChanged()

            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }

        myRef.child(Firebase.auth.currentUser!!.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataModelList.clear()

                for (dataModel in snapshot.children) {
                    Log.d("Data", dataModel.toString())
                    dataModelList.add(dataModel.getValue(DataModelsupplies::class.java)!!)
                }
                adapter_list.notifyDataSetChanged()
                Log.d("DataModel", dataModelList.toString())

                // textView.text = dataModelList.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val mDeleteBtn = findViewById<ImageButton>(R.id.deleteBtn)
        mDeleteBtn?.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("여행 준비물")
                .setMessage("전체 삭제하시겠습니까?")
                .setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, id ->
                        val myRef = database.getReference("mySupplies").child(Firebase.auth.currentUser!!.uid)

                        // 삭제 버튼 누르면 리스트 전체가 삭제됨
                        myRef
                            .removeValue()
                    })
                .setNegativeButton("아니요",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            builder.show()


        }

        saveBtn2.setOnClickListener() {
            val title = binding.suppliesMemo.text.toString()

            val database = Firebase.database
            val myRef = database.getReference("mySupplies").child(Firebase.auth.currentUser!!.uid)

            val model2 = DataModelsupplies(title)

            myRef
                .push()
                .setValue(model2)

            // textView.text = title <- listView 대신 TextView

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()

            suppliesMemo.text.clear()

        }
    }
}