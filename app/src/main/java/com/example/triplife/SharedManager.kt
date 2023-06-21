package com.example.triplife

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences 를 관리할 수 있는 유틸 클래스이다.
 * 사용을 원하는 액티비티에서 SharedManager.init(getApplicationContext()); 로 초기화 한 이후에
 * 예시 - SharedManager.write(SharedManager.ID, "저장하고 싶은 값"); 의 형태로 DB에 쓰고
 * SharedManager.read(SharedManager.ID, "저장된 기존 값이 없을때 default value"); 로 읽어 올 수 있다.
 */

object SharedManager {
    private var mSharedPref: SharedPreferences? = null
    var USER_NAME                   = "USER_NAME"                   // 현재 로그인 한 계정의 닉네임
    var PROFILE_URL                 = "PROFILE_URL"                 // 현재 로그인 한 프로필 URL
    var MY_UID                      = "MY_UID"                      // 현재 로그인 한 Firebase Uid


    fun init(context: Context) {
        if (mSharedPref == null) mSharedPref =
            context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun read(key: String?, defValue: String?): String? {
        return mSharedPref!!.getString(key, defValue)
    }

    fun write(key: String?, value: String?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun read(key: String?, defValue: Boolean): Boolean {
        return mSharedPref!!.getBoolean(key, defValue)
    }

    fun write(key: String?, value: Boolean) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun read(key: String?, defValue: Int): Int {
        return mSharedPref!!.getInt(key, defValue)
    }

    fun write(key: String?, value: Int) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putInt(key, value).apply()
    }

    fun read(key: String?, defValue: Long): Long {
        return mSharedPref!!.getLong(key, defValue)
    }

    fun write(key: String?, value: Long) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putLong(key, value).apply()
    }

    fun clear() {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }
}