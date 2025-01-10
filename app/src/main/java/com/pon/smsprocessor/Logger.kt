package com.pon.smsprocessor

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pon.smsprocessor.api.OrderReport
import com.pon.smsprocessor.room.AppDatabase
import com.pon.smsprocessor.room.LogDataBaseItem
import com.pon.smsprocessor.room.LogsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofLocalizedDate
import java.time.format.DateTimeFormatter.ofLocalizedDateTime
import java.time.format.DateTimeFormatter.ofLocalizedTime
import java.time.format.FormatStyle
import java.time.format.FormatStyle.MEDIUM
import java.time.format.FormatStyle.SHORT


object Logger {

    private val coroutineScope = CoroutineScope(IO)
    private var logCount = 0
    private val limitForShow: Int = 2000
    private val limitForWarning: Int = 50000
    private val filesDir = File(App.appContext.filesDir, "logs").apply {
        mkdirs()
    }
    private var file = File(filesDir, "logfile.txt")
        .apply { writeText("") }

    private val emptyLogItem = LogItem(false, "", "Пустой лог сообщений")

    private val emptyItemLog = mutableListOf<LogItem>().apply {
        add(emptyLogItem)
    }
    private var currentList = emptyItemLog

    val logItemData: MutableLiveData<List<LogItem>> = MutableLiveData(emptyItemLog)
    private val mutex = Mutex()


    init {
        CoroutineScope(IO).launch {
            val lastItems = async {
                App.roomDao.getLatest(limitForShow)
            }
            val newList = lastItems.await().map { LogItem(it.important, it.time, it.text) }
            currentList = newList.toMutableList()
            logItemData.postValue(currentList)
            logCount = async {
                App.roomDao.getCount()
            }.await()
        }
    }

    fun addToLog(item: String, important: Boolean = false) {

        synchronized(currentList) {
            if (important) SoundEffectsPlayer.playSound()
            val newItem = LogItem(
                important,
                time(),
                item
            )
            if (currentList.last()== emptyLogItem) currentList.removeLast()
            logCount++
            currentList.add(0, newItem)
            if (logCount > limitForWarning && currentList.size % 50 == 0) {
                currentList.add(
                    0, LogItem(
                        false,
                        time(),
                        "Рекомендуем сохранить и очистить логи, текущий размер $logCount зап." +
                                "Очистку данных приложения можно выполнить в  меню его свойств, " +
                                "после этого потребуется заново выдать права на доступ к СМС при перезапуске  "
                    )
                )
            }
            logItemData.postValue(currentList)
            coroutineScope.launch {
                mutex.withLock {
                    App.roomDao.insertRecord(
                        LogDataBaseItem(
                            newItem.time,
                            newItem.important,
                            newItem.text, 0
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    fun sendLog(context: Context) {
        Toast.makeText(context, "Идет подготовка", Toast.LENGTH_SHORT).show()
        CoroutineScope(IO).launch {
            Log.e(TAG, "sendLog: start=")
            val lastItems = async {
                App.roomDao.getAll()
            }
            val allLogs = lastItems.await()
                .map { LogItem(it.important, it.time, it.text) }
                .joinToString(separator = "\n",
                    transform = { item -> item.time + (if (item.important) " ** " else "   ") + item.text })
            file.writeText(allLogs)
            Log.e(TAG, "sendLog: end. log file ready=")
            val uri = FileProvider.getUriForFile(context, "com.pon.smsprocessor", file)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "SMSProcessor  логи")
            shareIntent.type = "text/html"
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(shareIntent, "Отправить логи:"))
        }
        val uri = FileProvider.getUriForFile(context, "com.pon.smsprocessor", file)
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "SMSProcessor  логи")
        shareIntent.type = "text/html"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(shareIntent, "Отправить логи:"))
    }

    private fun time() = (LocalDate.now().format(ofLocalizedDate(SHORT))
            + ", " + LocalTime.now().format(ofLocalizedTime(MEDIUM)))


    data class LogItem(
        val important: Boolean,
        val time: String,
        val text: String
    )

}