package com.example.triplife.util

import android.util.Log
import com.example.triplife.BuildConfig

object WLog {

    private data class LogInfo(val lineNumber: Int, val className: String, val methodName: String)

    @JvmStatic
    fun isDebuggable(): Boolean {
        return BuildConfig.DEBUG
    }

    private fun createLog(logInfo: LogInfo, log: String?): String {
        return "[${logInfo.methodName}():${logInfo.lineNumber}]$log"
    }

    private fun getLogInfo(sElements: Array<StackTraceElement>): LogInfo {
        return LogInfo(
            sElements[1].lineNumber,
            when {
                sElements[1].fileName.endsWith(".java") -> sElements[1].fileName.replace(
                    ".java".toRegex(),
                    ""
                )
                sElements[1].fileName.endsWith(".kt") -> sElements[1].fileName.replace(
                    ".kt".toRegex(),
                    ""
                )
                else -> sElements[1].fileName
            },
            sElements[1].methodName
        )
    }

    @JvmStatic
    fun e(message: String?) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.e(logInfo.className, createLog(logInfo, message))
    }

    @JvmStatic
    fun e(throwable: Throwable) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.e(logInfo.className, createLog(logInfo, "$throwable"))
    }

    @JvmStatic
    fun i(message: String?) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.i(logInfo.className, createLog(logInfo, message))
    }

    @JvmStatic
    fun d(message: String?) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.d(logInfo.className, createLog(logInfo, message))
    }

    @JvmStatic
    fun v(message: String?) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.v(logInfo.className, createLog(logInfo, message))
    }

    @JvmStatic
    fun w(message: String?) {
        if (!isDebuggable()) return
        val logInfo = getLogInfo(Throwable().stackTrace)
        Log.w(logInfo.className, createLog(logInfo, message))
    }
}