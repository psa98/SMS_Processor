package com.pon.smsprocessor

import android.content.Context.MODE_PRIVATE
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.pon.smsprocessor.DefaultsRepository.formattedDate
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

object TaxiRepository {

    private val preferences = App.appContext.getSharedPreferences("main", MODE_PRIVATE)
    var token: String = preferences.getString("token", "").toString()
        set(value) {
            field = value
            preferences.edit().putString( "token",field).apply()
        }
    var uHash: String = preferences.getString("uHash", "").toString()
        set(value) {
            field = value
            preferences.edit().putString("uHash",field).apply()
        }




    var userIdMap: HashMap<String, String> = HashMap()

    private fun retriesEnded(phone:String){
        if (phone.isEmpty()) return
        LogRepository.addToLog("Повторные попытки исчерпаны, неудача")
        SMSSender.sendSMS(phone,DefaultsRepository.failMessage)
    }

    // должно быть вызвано течение 10 секунд после авторизации, обновит поля tокен или u_hash
    // Возвращает false при неуспехе
    private suspend fun renewTokensForHash(hash: String, retry: Int =0): Boolean {
        if (retry==DefaultsRepository.retryCount){
            retriesEnded("")
            return false
        }
        if (hash.isEmpty()) return false
        val result = try {
            api.getTokenHash(hash)
        } catch (e: Exception) {
            LogRepository.addToLog("${e.message.toString()}\n")
            delay(DefaultsRepository.retryTime)
            renewTokensForHash(hash,retry+1)
            return false
        }
        val token = result.body()?.data?.token
        val uHash = result.body()?.data?.u_hash
        if (token.isNullOrEmpty() || uHash.isNullOrEmpty()) {
            LogRepository.addToLog("Токен или u_hash не получены! \n")
            return false
        }
        TaxiRepository.uHash = uHash
        TaxiRepository.token = token
        Log.d(TAG, "renewTokensForHash: =$token \n $uHash")
        return true
    }


    private suspend fun registerUserWithPhone(phone: String, name: String,retry: Int=0) {
        if (retry==DefaultsRepository.retryCount){
            retriesEnded(phone)
            return
        }
        val result = try {
            api.registerNewUser(token, uHash, name, phone)
        } catch (e: Exception) {
            LogRepository.addToLog("${e.message}\n")
            delay(DefaultsRepository.retryTime)
            registerUserWithPhone(phone,name,retry+1)
            return
        }
        when {
            result.isSuccessful -> {
                when (result.body()?.code) {
                    "200" -> {
                        val userId = result.body()?.data?.u_id
                        if (userId != null) userIdMap[phone] = userId.toString()
                        val message ="Пользователь успешно зарегистрирован, userId = $userId "
                        LogRepository.addToLog(message)
                        Log.d(TAG, message)
                    }

                    else -> {
                        val errorMessage =
                            "Пользователь - ошибка регистрации, code = ${result.body()?.code}" +
                                    "   status =${result.body()?.status} ${result.message()}"
                        LogRepository.addToLog(errorMessage)
                        Log.d(TAG, errorMessage)
                        delay(DefaultsRepository.retryTime)
                        registerUserWithPhone(phone,name,retry+1)
                    }
                }
            }

        }

    }

    // метод заносит ид пользователя в карту при успехе.
    // Если на сервере юзера нет то он регистрируется
    //
    suspend fun checkAndRegisterUser(phone: String, name: String,retry: Int=0) {
        if (retry==DefaultsRepository.retryCount){
            retriesEnded(phone)
            return
        }
        if (userIdMap.contains(phone)) {
            LogRepository.addToLog("Юзер с этим телефоном уже зарегистрирован, его ид известен ")
            return
        }
        val result = try {
            api.checkUser(token, uHash, phone)
        } catch (e: Exception) {
            LogRepository.addToLog("${e.message.toString()}\n")
            delay(DefaultsRepository.retryTime)
            checkAndRegisterUser(phone,name,retry+1)
            return
        }
        when {
            result.isSuccessful -> {
                when (result.body()?.code) {
                    "404" -> {
                        LogRepository.addToLog("Пользователь не найден, нужна регистрация")
                        registerUserWithPhone(phone, name)
                    }

                    "200" -> {
                        val userId = result.body()?.auth_user?.u_id
                        if (userId != null) userIdMap[phone] = userId else {
                            LogRepository.addToLog(" User id = null, ошибка сервера")
                        }
                        val message =
                            "Пользователь найден, userId = $userId регистрация не требуется"
                        LogRepository.addToLog(message)
                        Log.d(TAG, message)
                    }

                    else -> {
                        val errorMessage =
                            "Пользователь - ошибка регистрации, code = ${result.body()?.code}" +
                                    "   status =${result.body()?.status} ${result.message()}"
                        LogRepository.addToLog(errorMessage)
                        Log.d(TAG, errorMessage)
                        delay(DefaultsRepository.retryTime)
                        checkAndRegisterUser(phone,name,retry+1)
                    }
                }
            }
        }
    }


    private suspend fun orderTaxi(
        userId: String,
        startAddress: String,
        endAddress: String,
        startDateTime: String,
        waiting: Int,
        passCount: Int,
        type: Int,
        phone:String,
        retry: Int=0
    ) {

        val taxiData = OrderData(
            startAddress,
            endAddress,
            startDateTime,
            waiting,
            passCount,
            listOf(type),
            "1",
        )
        if (retry==DefaultsRepository.retryCount){
            retriesEnded(phone)
            return
        }
        val order = Order(token, uHash, userId, taxiData)
        val result = try {
            Log.i(TAG, "orderTaxi: =${Gson().toJson(order)}")
            val dataUrl = taxiData.toGson()
            LogRepository.addToLog("Заказываю такси, параметры $taxiData \n")
            Log.i(TAG, "orderTaxi: =$dataUrl")
            api.orderTaxi(token, uHash, userId, dataUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            LogRepository.addToLog(e.message.toString())
            delay(DefaultsRepository.retryTime)
            orderTaxi(userId,startAddress,endAddress,startDateTime,waiting,passCount,type,phone,retry+1)
            return
        }
        if (result.body()?.code == "200"){
            LogRepository.addToLog("Такси заказано успешно, ${result.body()}")
        val resultMessage = " status = ${result.body()?.status} ${result.body()?.message}"
        Log.i(TAG, "orderTaxi: result=$resultMessage")
        // приходит в double в Аnу значение номера и надо отработать все возможные null
        val driveId:Int = (Gson().fromJson( result.body()?.data.toString(),HashMap<String,Any>()
            .javaClass)["b_id"]
            .toString()
            .toDoubleOrNull()?: 0)
            .toInt()
        if (driveId!=0&&type==5) cancelOrderInTime(driveId,userId)
        Log.i(TAG, "orderTaxi: id=$driveId")}
        else {
            LogRepository.addToLog("Такси не заказано, ошибка, ${result.body()}, попытка ${retry+1}")
            delay(DefaultsRepository.retryTime)
            orderTaxi(userId,startAddress,endAddress,startDateTime,waiting,passCount,type,phone,retry+1)
        }

    }

    private fun cancelOrderInTime(driveId: Int,id:String, retry:Int=0) {
        if (retry==DefaultsRepository.retryCount){
            retriesEnded("")
            return
        }
        val looper = Looper.getMainLooper()
        android.os.Handler(looper).postDelayed({
            CoroutineScope(IO).launch {
                val result = try {
                    api.cancelTaxi(driveId, token, uHash,id)
                } catch (e:Exception){
                    LogRepository.addToLog(e.message.toString())
                    delay(DefaultsRepository.retryTime)
                    cancelOrderInTime(driveId,id, retry+1)
                    return@launch
                }
                LogRepository.addToLog("Отмена заказа по времени - ответ сервера ${result.body()}")
            }
        },DefaultsRepository.cancelTime*60*1000L)

    }


    // метод так же обновляет токен и хэш
    suspend fun authAdmin(retry: Int = 0) {
        if (retry==DefaultsRepository.retryCount) {
            retriesEnded("")
            return
        }
        val result = try {
            api.authAsAdmin()
        } catch (e: Exception) {
            LogRepository.addToLog("Ошибка авторизации админа: ${e.message.toString()}\n")
            delay(DefaultsRepository.retryTime)
            authAdmin(retry+1)
            return
        }
        when {
            result.isSuccessful -> {
                if (result.body()?.code == "200") {
                    val hash = result.body()?.auth_hash ?: ""
                    val renewTokenResult = renewTokensForHash(hash, retry+1)
                    if (!renewTokenResult) {
                        LogRepository.addToLog("Авторизация не выполнена, не получены токен и хэш")
                        return
                    } else {
                        LogRepository.addToLog("Авторизация - токен и хэш обновлены")
                    }
                } else {
                    val errorMessage =
                        "Ошибка авторизации code=${result.body()?.code} Message: ${result.body()?.message}\n"
                    LogRepository.addToLog(errorMessage)
                    Log.e(TAG, errorMessage)
                }
            }
            else -> {
                LogRepository.addToLog(result.message())
            }
        }
    }

    // тестируем parsed time на правильность, если формат 00:00 то возвращаем его
    // в виде 00:00:00, иначе текущее время
    private fun formatTime(time: String): String {
        val realTime = try {
            LocalTime.parse(time.trim())
            "${time.trim().take(5)}:00"
        } catch (e: Exception) {
            LocalTime.now().toString().take(8)
        }
        return realTime
    }

    fun makeOrder(phone:String,smsTextString:String) {
        CoroutineScope(IO).launch {
            var idString: String? = userIdMap[phone]
            if (idString == null) checkAndRegisterUser(phone, "SMSUser ${LocalDateTime.now()}")
            idString = userIdMap[phone]
            if (idString == null) {
                LogRepository.addToLog("Ошибка регистрации пользователя\n")
                return@launch
            }
            val smsText: List<String> =smsTextString.split(DefaultsRepository.divider)
            val startAddress = smsText.getOrElse(0) { "" }.trim()
            if (startAddress.isEmpty()) {
                LogRepository.addToLog("Не указан обязательный параметр - адрес," +
                        " заказа такси не будет\n")
                return@launch
            }
            val endAddress = smsText.getOrElse(1) { "" }.trim()
            val passCount: Int = smsText.getOrElse(2) { "1" }.trim().toIntOrNull() ?: 1
            val orderTime  = smsText.getOrElse(3) {
                LocalTime
                    .now()
                    .plusSeconds(DefaultsRepository.time.toLong())
                    .format(DateTimeFormatter.ISO_LOCAL_TIME)
                    .take(5)
            }.trim().take(5)

            val orderType = smsText.getOrElse(4) { DefaultsRepository.orderType.toString() }
                .trim()
                .toIntOrNull() ?: DefaultsRepository.orderType
            // если тип заказа 5 то время игнорируется, подставляется текущее
            val orderDateTime = if (orderType==1)formattedDate( formatTime(orderTime))
            else formattedDate(LocalTime.now() .plusSeconds(DefaultsRepository.time.toLong())
                .format(DateTimeFormatter.ISO_LOCAL_TIME).take(8))

            //todo - нет в ТЗ параметра для ожидания, по умолчанию стоит "сутки"
            orderTaxi(
                idString, startAddress, endAddress, orderDateTime,
                24 * 60 * 60, passCount, orderType,phone
            )
        }
    }


}