package com.example.triplife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import com.example.triplife.model.HouseKeepingData

class InsertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert)


        val InsertAddBtn = findViewById<Button>(R.id.InsertAddBtn)
        var cateResult: String = ""


        val purposeEditTxt = findViewById<EditText>(R.id.purposeEditTxt)
        val insertDataPicker = findViewById<DatePicker>(R.id.insertDataPicker)
        var DateString = ""
        val DateSetBtn = findViewById<Button>(R.id.button2)

        val moneyEditTxt = findViewById<EditText>(R.id.moneyEditTxt)
        val contentEidtTxt = findViewById<EditText>(R.id.contentEidtTxt)




        DateSetBtn.setOnClickListener {

            var idpMonth:String = (insertDataPicker.month + 1).toString()
            var idpDate:String  = (insertDataPicker.dayOfMonth).toString()

            if(insertDataPicker.month + 1<10){
                idpMonth= "0"+"$idpMonth"
                //달이 10보다 작을 경우 달 앞에 0을 붙인다 ex) 3 ->03 or 9 ->09
            }
            if(insertDataPicker.dayOfMonth<10){
                idpDate = "0"+"$idpDate"
            }	//일이 10보다 작을 경우 일 앞에 0을 붙인다. ex) 7 ->07  or 3 ->03

            DateString =
                insertDataPicker.year.toString() + "-" + (idpMonth) + "-" + idpDate
            println("DateString:$DateString")
            //SQLite에서 Date 열의 값을 String으로 지정했고 형식은 YYYY-MM-DD로 맞춰야한다.
            //따라서 "-"가 년/월 월/일 사이에 추가된다.
            val toast = Toast.makeText(this.applicationContext, DateString, Toast.LENGTH_SHORT)
            toast.show()
        }


        InsertAddBtn.setOnClickListener {

            val houseKDT = HouseKeepingData(
                0,
                cateResult,
                purposeEditTxt.text.toString().trim(),
                DateString,
                moneyEditTxt.text.toString().toInt(),
                contentEidtTxt.text.toString().trim()
            )
            //이때까지 .text 안 써서 고생함 (⓿_⓿)

            val dbHelper = DBHelper.getInstance(this, "HKBook.db",)
            dbHelper.insert(houseKDT)

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }

    }
}