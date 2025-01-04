package com.pon.smsprocessor

import android.content.Context.MODE_PRIVATE
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.pon.smsprocessor.DefaultsRepository.formattedDate
import com.pon.smsprocessor.DefaultsRepository.okMessage
import com.pon.smsprocessor.api.Order
import com.pon.smsprocessor.api.OrderData
import com.pon.smsprocessor.api.RetrofitClient.api
import com.pon.smsprocessor.api.SMSSender
import com.pon.smsprocessor.api.toGson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

object PollTaskRepository {

    private val maxTimePolling =
        5 * 60 * 1000L // максимальное время работы поллинга, потом задача самоудаляется
    private val pollingPeriod =
        20 * 1000L // частота  поллинга


    val status = mapOf(
        Pair(1, "В обработке"),
        Pair(2, "Одобрен"),
        Pair(3, "Отменен"),
        Pair(4, "Выполнен"),
        Pair(5, "Ожидание активации"),
        Pair(6, "Предлагается водителям")
    )
    private val currentTasksList: MutableList<PollingTask> = mutableListOf()

    fun enqueueTask(driveId: Int) {
        currentTasksList.add(PollingTask(driveId))

    }

    internal class PollingTask(
        private val driveId: Int,
        startMoment: Long = Date().time
    ) {
        private val endTime = startMoment + maxTimePolling

        init {
            CoroutineScope(IO).launch {
                var currentOrderState = 1
                while (Date().time < endTime) {
                    delay(pollingPeriod)
                    try {
                        val result =
                            api.checkDriveState(driveId, TaxiRepository.token, TaxiRepository.uHash)

                        val newState: Int? =
                            result.body()?.data?.booking?.values?.firstOrNull()?.b_state?.toIntOrNull()
                        if (newState != currentOrderState && newState != null) {
                            Logger.addToLog(
                                "Изменение статуса поездки id=${driveId}\n было ${status[currentOrderState]}" +
                                        " стало ${status[newState]}"
                            )
                            currentOrderState = newState
                            if (newState==3) break
                        }

                    } catch (e: Exception) {
                        Logger.addToLog("Запрос данных о поездке id=${driveId} неуспешен, $e")

                    }
                }
                currentTasksList.remove(this@PollingTask)
            }
        }
    }

}