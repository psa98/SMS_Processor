package com.pon.smsprocessor

import android.util.Log
import com.pon.smsprocessor.api.RetrofitClient.api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

object PollTaskRepository {

    private val maxTimePolling =
        40 * 60 * 1000L // максимальное время работы поллинга, потом задача самоудаляется
    private fun pollingPeriod() =  DefaultsRepository.freqTime * 1000L // частота  поллинга


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
                    delay(pollingPeriod())
                    try {
                        val result =
                            api.checkDriveState(driveId, TaxiRepository.token, TaxiRepository.uHash)
                        val booking = result.body()?.data?.booking?.values?.firstOrNull()
                        val newState: Int? =booking?.b_state?.toIntOrNull()
                        val newMessage:String = result.body()?.data?.message?.values?.firstOrNull()?:""

                        // пара содержит телефон юзера и прежнее сообщение, возможно пустое
                        val oldMessagePair: Pair<String, String> = TaxiRepository.messageMap[driveId]!!
                        if (newMessage!=oldMessagePair.second){
                            //шлем СМС c новым сообщением и обновляем пару
                            Logger.addToLog(
                                "Изменение статуса поездки id=${driveId} - новое сообщение ${newMessage}, " +
                                        "было ранее ${oldMessagePair.second}"
                            )
                            SMSSender.sendSMS(oldMessagePair.first, "*$newMessage")
                            TaxiRepository.messageMap[driveId]= Pair(oldMessagePair.first,newMessage)
                        }


                        if (newState != currentOrderState && newState != null) {
                            Logger.addToLog(
                                "Изменение статуса поездки ${driveId}\n booking_status было $currentOrderState" +
                                        " стало $newState"
                            )
                            currentOrderState = newState
                            if (newState in DefaultsRepository.stopCodesList) break
                        }

                    } catch (e: Exception) {
                        Logger.addToLog("Запрос данных о поездке id=${driveId} неуспешен, $e")

                    }
                }
                Logger.addToLog("Запрос данных о поездке id=${driveId} прекращается")
                currentTasksList.remove(this@PollingTask)
            }
        }
    }

}