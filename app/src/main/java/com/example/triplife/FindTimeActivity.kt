package com.example.triplife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triplife.adapter.CustomAdapter

class FindTimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_time)

        val findDataPicker = findViewById<DatePicker>(R.id.findDataPicker)
        val findDatePicker2 = findViewById<DatePicker>(R.id.findDatePicker2)

        val findFromTxt = findViewById<TextView>(R.id.findFromTxt)
        val findToTxt = findViewById<TextView>(R.id.findToTxt)


        val FindTimeBtn = findViewById<Button>(R.id.FindTimeBtn)



        FindTimeBtn.setOnClickListener {

            val fromDate:String
            var frMonth:String = (findDataPicker.month + 1).toString()
            var frDate:String  = (findDataPicker.dayOfMonth).toString()

            if(findDataPicker.month + 1<10){
                frMonth= "0$frMonth"
            }
            if(findDataPicker.dayOfMonth<10){
                frDate = "0$frDate"
            }

            fromDate =
                findDataPicker.year.toString() + "-" + frMonth + "-" + frDate
            println("fromDate:${fromDate}")

            val toDate:String
            var lastMonth:String = (findDatePicker2.month + 1).toString()
            var lastDate:String  = (findDatePicker2.dayOfMonth).toString()

            if(findDatePicker2.month + 1<10){
                lastMonth= "0$lastMonth"
            }
            if(findDatePicker2.dayOfMonth<10){
                lastDate = "0$lastDate"
            }

            toDate =
                findDatePicker2.year.toString() + "-" + lastMonth + "-" + lastDate
            println("toDate:$toDate")

            findFromTxt.text = fromDate
            findToTxt.text = toDate
            val dbHelper = DBHelper.getInstance(this,"HKBook.db",)
            val list = dbHelper.findTime(fromDate,toDate)


            val FindRecyclerView = findViewById<RecyclerView>(R.id.FindRecyclerView)
            val mAdapter = CustomAdapter(this,list)
            FindRecyclerView.adapter = mAdapter


            val layout = LinearLayoutManager(this)
            FindRecyclerView.layoutManager=layout //recycler 뷰에 리니어 레이아웃 적용

            FindRecyclerView.setHasFixedSize(true)


        }

    }
}