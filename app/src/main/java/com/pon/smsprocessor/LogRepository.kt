package com.pon.smsprocessor

import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.MutableLiveData

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object LogRepository {
    private val gson = Gson()
    private var listType: Type = object : TypeToken<List<String>>() {}.type
    private val emptyLog = gson.toJson(listOf("Пустой лог сообщений"))
    private val preferences = App.appContext.getSharedPreferences("main",MODE_PRIVATE)
    private val listFromPref:List<String> = gson.fromJson(preferences.getString("LogList", emptyLog), listType)
    private val messageList = mutableListOf<String>().apply {  addAll(listFromPref)}
    val logString
        get() = messageList.joinToString("\n")
    val logData:MutableLiveData<String> = MutableLiveData (logString)


    fun addToLog (sms:String){
        messageList.remove("Пустой лог сообщений")
        messageList.add(0,sms)
        if (messageList.size>20) messageList.removeLast()
        val jsonList = gson.toJson(messageList)
        preferences.edit().putString("LogList",jsonList).apply()
        logData.postValue(messageList.joinToString("\n"))
    }


}