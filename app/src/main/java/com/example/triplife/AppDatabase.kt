package com.example.triplife

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.triplife.model.User
import com.example.triplife.model.UserDao

@Database(entities = [User::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object{
        private var INSTANCE : AppDatabase?=null

        fun getDatabase(context: Context): AppDatabase?{
            if(INSTANCE == null){

                INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,"app_database")
                .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }
    }
}