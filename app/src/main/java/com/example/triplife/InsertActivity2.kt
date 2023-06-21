package com.example.triplife

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.triplife.model.User

class InsertActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert2)

        val titleEdit: EditText = findViewById(R.id.title_edit)
        val saveBtn : Button = findViewById(R.id.save_btn)

        saveBtn.setOnClickListener {
            val sTitle = titleEdit.text.toString()

            //사용자 등록
            insertUser(sTitle)
        }
    }

    private fun insertUser(title: String){
        val user = User(null, title)
        var db: AppDatabase? = AppDatabase.getDatabase(applicationContext)
        db?.userDao()?.insertUser(user)

        //상태값 돌려주기
        setResult(Activity.RESULT_OK)

        //액티비티 닫기
        finish()

    }
}