package com.pon.smsprocessor

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.MutableLiveData

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pon.smsprocessor.api.OrderReport
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Logger {
    private val gson = Gson()

    private val preferences = App.appContext.getSharedPreferences("main",MODE_PRIVATE)


    private var logItemListType: Type = object : TypeToken<List<LogItem>>() {}.type
    private val emptyLogItem = LogItem(false,"","Пустой лог сообщений")
    private val emptyItemLog = gson.toJson(listOf(emptyLogItem))
    private val savedLogList:List<LogItem> = gson.fromJson(preferences.getString("LogItemList", emptyItemLog), logItemListType)
    private val logItemList = mutableListOf<LogItem>().apply {  addAll(savedLogList)}
    val logItemData:MutableLiveData<List<LogItem>> = MutableLiveData(logItemList)


    fun addToLog (item:String, important: Boolean= false){
        synchronized(logItemList){
        logItemList.remove(emptyLogItem)
        if (important) SoundEffectsPlayer.playSound()
        val newItem  = LogItem(important,LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)),item)
        //messageList.add(0,sms)
        logItemList.add(0,newItem)
        if (logItemList.size>1000) logItemList.removeLast()
        Log.e(TAG, "addToLog: start=")
        val jsonList = gson.toJson(logItemList)
        Log.e(TAG, "addToLog: = end")
        preferences.edit().putString("LogItemList",jsonList).apply()
        logItemData.postValue(logItemList)
        }
    }

    data class LogItem(
        val important:Boolean,
        val time: String,
        val text:String
    )

}