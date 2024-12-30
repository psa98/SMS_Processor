package com.pon.smsprocessor

import android.Manifest.permission.RECEIVE_SMS
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.pon.smsprocessor.UserRepository.checkAndRegisterUser
import com.pon.smsprocessor.UserRepository.orderTaxi
import com.pon.smsprocessor.api.Api
import com.pon.smsprocessor.api.RetrofitClient
import com.pon.smsprocessor.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TimeZone.SHORT


const val REQUEST_PERMISSION_SMS = 1004

class StartActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val api: Api by lazy { RetrofitClient.api }
    private var job: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        authAdmin()
    }


    override fun onResume() {
        super.onResume()

        val logView = binding.scrollLog
        LogRepository.logData.observe(this) { log -> logView.text = "Последние SMS: \n \n" + log }
        val readyToSend = checkAndShowPermissions()
        logView.text = "Последние SMS: \n \n" + LogRepository.logString
        val should = shouldShowRequestPermissionRationale(RECEIVE_SMS)
        binding.permissionsText.text = when {
            readyToSend -> "Разрешение на СМС выдано"
            !readyToSend && should -> "Перезапустите приложение и выдайте разрешение на смс"
            else -> "Выдайте разрешение на доступ к смс в настройках"
        }
        ContextCompat.startForegroundService(
            this, Intent(this, ForegroundService::class.java)
        )
    }

    private fun checkAndShowPermissions(): Boolean {
        val smsPermission =
            ContextCompat.checkSelfPermission(this, RECEIVE_SMS) == PERMISSION_GRANTED
        if (!smsPermission) {
            val permissions = arrayOf(RECEIVE_SMS)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_SMS)
        }
        return smsPermission
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_SMS -> checkAndShowPermissions()
            else -> {}
        }
        val readyToSend = checkAndShowPermissions()
        val should = shouldShowRequestPermissionRationale(RECEIVE_SMS)
        binding.permissionsText.text = when {
            readyToSend -> "Разрешение на СМС выдано"
            !readyToSend && should -> "Выдайте разрешение на смс"
            else -> "Выдайте разрешение на доступ к смс в настройках"
        }

    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }

    fun authorizeAdmin(view: View) {
        authAdmin()
    }

    private fun authAdmin() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val result = try {
                api.authAsAdmin()
            } catch (e: Exception) {
                LogRepository.addToLog("${e.message.toString()}\n")
                return@launch
            }
            var hash = ""
            when {
                result.isSuccessful -> {
                    if (result.body()?.code == "200") {
                        hash = result.body()?.auth_hash ?: ""
                        UserRepository.renewTokensForHash(hash)
                        api.getTokenHash(hash)
                        println("Hash=  $hash")
                        Log.d(TAG, hash)
                        LogRepository.addToLog("Авторизация выполнена")
                    } else {
                        val errorMessage =
                            "Auth error, code=${result.body()?.code} Message: ${result.body()?.message}\n"
                        LogRepository.addToLog(errorMessage)
                        Log.e(TAG, errorMessage)
                    }

                }

                else -> {
                    LogRepository.addToLog(result.message())
                }
            }
        }
    }


    fun registerUser(view: View) {
        val phone = binding.testPhone.text.toString().filter { it.isDigit() || it == '+' }

        if (phone.length != 12) LogRepository.addToLog(
            "Неправильный номер, правильный формат " +
                    "+7ХХХХХХХХХХ"
        )
        CoroutineScope(Dispatchers.IO).launch {
            checkAndRegisterUser(phone, "SMSUser ${LocalDateTime.now()}")
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun makeOrder(view: View) {
        val phone = binding.testPhone.text.toString()
        val idFromMap = UserRepository.userIdMap[phone]
        if (idFromMap == null) CoroutineScope(IO).launch {
            val id = UserRepository.getUserId(phone)
            if (id == null) {
                LogRepository.addToLog("Пользователь не зарегистрирован, нужна регистрация")
                return@launch
            }
            Log.e(TAG, "makeOrder: id=${id}")
            val address = "Красная площадь, дом 1"
            val z= TimeZone.getDefault()
            println(z)
            // очень нестандартное получение зоны, надо переписать
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val zoneOffsetHr = z.getOffset(Date().time)/3600000
            val textZ = "+${if (zoneOffsetHr < 10) "0$zoneOffsetHr" else zoneOffsetHr}:00"
            // поддерживается только положительная зона!

            val date = dateFormat.format(Date())+textZ
            Log.e(TAG, date )


            orderTaxi(id, "sdf", "sdf", date,
                24 * 60 * 60, 1, 1)
        }

    }


}