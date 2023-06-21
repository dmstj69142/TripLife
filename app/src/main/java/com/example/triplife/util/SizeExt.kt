package com.example.triplife.util

import android.content.res.Resources

//destinyDpi-> 화면 밀도

val Int.dp: Float
    get() = this * (densityDpi / 160f)

val Int.sp: Float
    get() = this * scaledDensity

val Float.dp: Float
    get() = this * (densityDpi / 160f)

val Float.sp: Float
    get() = this * scaledDensity

val Int.px: Int
    get() = (this / (densityDpi / 160f)).toInt()

val Float.px: Int
    get() = (this / (densityDpi / 160f)).toInt()

private val densityDpi: Int
    get() = Resources.getSystem().displayMetrics.densityDpi

private val scaledDensity: Float
    get() = Resources.getSystem().displayMetrics.scaledDensity