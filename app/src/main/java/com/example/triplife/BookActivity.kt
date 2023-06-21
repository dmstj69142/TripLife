package com.example.triplife

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_book.*

class BookActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        var cateResult: String = ""
        var DateString = ""

        val insertBtn = findViewById<FloatingActionButton>(R.id.insertBtn)
        val timeSearchBtn = findViewById<ImageButton>(R.id.timeSearchBtn)

        val SearchDataPicker = findViewById<DatePicker>(R.id.SearchDataPicker)
        val resultOutputTxt = findViewById<TextView>(R.id.resultOutputTxt)
        var resultDate = ""

        val SearchSetDateBtn = findViewById<ImageButton>(R.id.SearchSetDateBtn)

        bookDelete.visibility = View.GONE


        // textview 스크롤
        resultOutputTxt.movementMethod = ScrollingMovementMethod.getInstance()

        SearchSetDateBtn.setOnClickListener{
            var dpMonth:String = (SearchDataPicker.month + 1).toString()
            var dpDate:String  = (SearchDataPicker.dayOfMonth).toString()

            if(SearchDataPicker.month + 1<10){
                dpMonth= "0"+"$dpMonth"
            }
            if(SearchDataPicker.dayOfMonth<10){
                dpDate = "0"+"$dpDate"
            }


            resultDate  =
                SearchDataPicker.year.toString() + "-" +
                        dpMonth + "-" +
                        dpDate
            println("resultDate:$resultDate ")


            val dbHelper = DBHelper.getInstance(this,"HKBook.db",)
            val result = dbHelper.search(resultDate)

            resultOutputTxt.movementMethod = ScrollingMovementMethod.getInstance()
            resultOutputTxt.text = result

            bookDelete.visibility = View.VISIBLE
        }

        bookDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("가계부")
                .setMessage("해당 날짜의 내용을 삭제하시겠습니까?")
                .setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, id ->
                        var dpMonth:String = (SearchDataPicker.month + 1).toString()
                        var dpDate:String  = (SearchDataPicker.dayOfMonth).toString()

                        if(SearchDataPicker.month + 1<10){
                            dpMonth= "0"+"$dpMonth"
                        }
                        if(SearchDataPicker.dayOfMonth<10){
                            dpDate = "0"+"$dpDate"
                        }


                        resultDate  =
                            SearchDataPicker.year.toString() + "-" +
                                    dpMonth + "-" +
                                    dpDate
                        println("resultDate:$resultDate ")

                        val dbHelper = DBHelper.getInstance(this, "HKBook.db",)
                        dbHelper.delete(resultDate)

                        Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()

                        resultOutputTxt.text = ""
                    })
                .setNegativeButton("아니요",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            builder.show()

        }


        insertBtn.setOnClickListener {
            val intent = Intent(this, InsertActivity::class.java)
            startActivity(intent)
        }

        timeSearchBtn.setOnClickListener {
            val intent = Intent(this, FindTimeActivity::class.java)
            startActivity(intent)
        }

    }

}