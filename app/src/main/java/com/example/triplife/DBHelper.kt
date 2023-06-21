package com.example.triplife

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.triplife.model.HouseKeepingData

class DBHelper(context: Context, filename:String): SQLiteOpenHelper(context,filename,null,1) {

    companion object {
        var dbhelper: DBHelper? = null
        fun getInstance(context: Context, filename: String): DBHelper {
            if (dbhelper == null) {
                dbhelper = DBHelper(context, filename)
            }
            return dbhelper!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        var sql: String = "CREATE TABLE IF NOT EXISTS HKBook( " +
                "SEQ INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CATEGORY STRING, " +
                "PURPOSE STRING," +
                "DATE STRING, " +
                "MONEY INTEGER," +
                "MEMO STRING ) "

        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }


    fun insert(vo: HouseKeepingData) {

        println("데이터 입력 완료")

        var sql = " INSERT INTO HKBook(CATEGORY,PURPOSE,DATE,MONEY,MEMO) " +
                " VALUES('${vo.category}','${vo.purpose}','${vo.date}',${vo.money},'${vo.memo}')"

        var db = this.writableDatabase
        db.execSQL(sql)


    }

    fun delete(date: String) {
        var sql = " DELETE FROM HKBook WHERE DATE = " +
                " '${date}' "

        var db = this.writableDatabase
        db.execSQL(sql)
    }

    @SuppressLint("Range")
    fun search(date:String):String {
        var sql = " SELECT SEQ, CATEGORY, PURPOSE, MONEY, MEMO  FROM HKBook WHERE DATE =" +
                " '${date}' "

        var db = this.writableDatabase
        var result = db.rawQuery(sql, null)

        var str: String? = ""

        while (result.moveToNext()) {
            str +=  " \n " +"----------------------"+" \n " +
                    "여행지: " + result.getString(result.getColumnIndex("PURPOSE")) + " \n " +
                    "금액: " + result.getInt(result.getColumnIndex("MONEY")) + " \n " +
                    "메모: " + result.getString(result.getColumnIndex("MEMO"))
        }

        if(str==""){
            str="\n" + "검색된 데이터가 없습니다."
        }

        return str!!

        print(str)

    }


    @SuppressLint("Range")
    fun findTime(fromDate:String, toDate:String): ArrayList<HouseKeepingData> {

        // 2022-02-15
        var sql = " SELECT SEQ, CATEGORY, PURPOSE, DATE, MONEY, MEMO FROM HKBook WHERE DATE BETWEEN " +
                " '${fromDate}' AND '${toDate}'"

        var db = this.writableDatabase
        var result = db.rawQuery(sql, null)

        var list = ArrayList<HouseKeepingData>()

        while (result.moveToNext()) {
            var seq = result.getInt(result.getColumnIndex("SEQ"))
            var category = result.getString(result.getColumnIndex("CATEGORY"))
            var purpose = result.getString(result.getColumnIndex("PURPOSE"))
            var date = result.getString(result.getColumnIndex("DATE"))
            var money = result.getInt(result.getColumnIndex("MONEY"))
            var memo = result.getString(result.getColumnIndex("MEMO"))

            list.add(HouseKeepingData(seq,category,purpose,date,money,memo))
        }

        for (i in list.indices){
            println(list[i].date + " " + list[i].money)
        }

        return list
    }





}