package com.pon.smsprocessor

import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.google.gson.Gson
import com.pon.smsprocessor.api.Order
import com.pon.smsprocessor.api.OrderData
import com.pon.smsprocessor.api.RetrofitClient.api
import com.pon.smsprocessor.api.toGson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UserRepository {

    private val preferences = App.appContext.getSharedPreferences("main", MODE_PRIVATE)
    var token: String = preferences.getString("token", "").toString()
        set(value) {
            field = value
            preferences.edit().putString(field, "token").apply()
        }
    var uHash: String = preferences.getString("uHash", "").toString()
        set(value) {
            field = value
            preferences.edit().putString(field, "uHash").apply()
        }

    var userIdMap: HashMap<String, String> = HashMap()

    var lastUserId = ""


    suspend fun renewTokensForHash(hash: String) {
        if (hash.isEmpty()) return
        val result = try {
            api.getTokenHash(hash)
        } catch (e: Exception) {
            LogRepository.addToLog("${e.message.toString()}\n")
            return
        }
        val token = result.body()?.data?.token
        val uHash = result.body()?.data?.u_hash
        if (token.isNullOrEmpty() || uHash.isNullOrEmpty()) {
            LogRepository.addToLog("Токен или u_hash не получены! \n")
            return
        }
        UserRepository.uHash = uHash
        UserRepository.token = token
        Log.e(TAG, "renewTokensForHash: =$token \n $uHash")
    }


    fun registerUserWithPhone(phone: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                api.registerNewUser(UserRepository.token, UserRepository.uHash, name, phone)
            } catch (e: Exception) {
                LogRepository.addToLog("${e.message.toString()}\n")
                Log.e(TAG, "registerUserWithPhone: =$e")
                return@launch
            }
            when {
                result.isSuccessful -> {
                    when (result.body()?.code) {
                        "200" -> {
                            val userId = result.body()?.data?.u_id
                            // если нулл лучше вывести в лог
                            if (userId != null) userIdMap[phone] = userId.toString()
                            val message =
                                "Пользователь успешно зарегистрирован, userId = $userId регистрация не требуется"
                            lastUserId = userId.toString()
                            LogRepository.addToLog(message)
                            Log.d(TAG, message)
                        }

                        else -> {
                            val errorMessage =
                                "Пользователь - ошибка регистрации, code = ${result.body()?.code}" +
                                        "   status =${result.body()?.status} ${result.message()}"
                            LogRepository.addToLog(errorMessage)
                            Log.d(TAG, errorMessage)
                        }
                    }
                }

            }

        }


    }

    suspend fun checkAndRegisterUser(phone: String, name: String) {

        if (userIdMap.contains(phone)) {
            LogRepository.addToLog("Юзер с этим телефоном уже зарегистрирован, его ид известен ")
            lastUserId = userIdMap[phone].toString()
            return
        }
        val result = try {
            api.checkUser(UserRepository.token, UserRepository.uHash, phone)
        } catch (e: Exception) {
            LogRepository.addToLog("${e.message.toString()}\n")
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
                        if (userId != null) userIdMap[phone] = userId
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
                    }
                }
            }

        }


    }

    suspend fun getUserId(phone: String): String? {

        if (userIdMap.contains(phone)) {
            LogRepository.addToLog("Юзер с этим телефоном уже зарегистрирован, его ид известен ")
            lastUserId = userIdMap[phone].toString()
            return lastUserId
        }
        val job = try {
            CoroutineScope(IO).async {
                api.checkUser(
                    UserRepository.token,
                    UserRepository.uHash,
                    phone
                )
            }

        } catch (e: Exception) {
            LogRepository.addToLog("${e.message.toString()}\n")
            return null
        }
        val result = job.await()
        when {
            result.isSuccessful -> {
                when (result.body()?.code) {
                    "404" -> {
                        LogRepository.addToLog("Пользователь не найден, нужна регистрация")
                        return null
                    }

                    "200" -> {
                        val userId = result.body()?.auth_user?.u_id
                        if (userId != null) userIdMap[phone] = userId
                        val message =
                            "Пользователь найден, userId = $userId регистрация не требуется"
                        LogRepository.addToLog(message)
                        Log.d(TAG, message)
                        return userId
                    }

                    else -> {
                        val errorMessage =
                            "Пользователь - ошибка регистрации, code = ${result.body()?.code}" +
                                    "   status =${result.body()?.status} ${result.message()}"
                        LogRepository.addToLog(errorMessage)
                        Log.d(TAG, errorMessage)
                        return null
                    }
                }
            }

        }
        return null

    }


    suspend fun orderTaxi(
        id: String,
        startAddress: String,
        endAddress: String,
        startDateTime: String,
        waiting: Int,
        pass_count: Int,
        type: Int
    ) {
        val testData = OrderData(
            startAddress,
            endAddress,
            startDateTime,
            waiting,
            pass_count,
            type,
            "1",
        )
        val order = Order(token, uHash,id,testData)
        CoroutineScope(IO).launch {
            val result = try{
                Log.e(TAG, "orderTaxi: =${Gson().toJson( order)}")
            //api.orderTaxi2(order)

                val dataurl = testData.toGson()
                Log.e(TAG, "orderTaxi: =$dataurl")
                api.orderTaxi(token, uHash,id,dataurl)
            } catch (e:Exception){
                e.printStackTrace()
                LogRepository.addToLog(e.message.toString())
                return@launch
            }

            if (result.body()?.code =="200")
                LogRepository.addToLog("Такси заказано успешно")

            val resultMessage = " status = ${ result.body()?.status} ${result.body()?.message}"
            Log.e(TAG, "orderTaxi: result=$resultMessage")

        }
    }


}