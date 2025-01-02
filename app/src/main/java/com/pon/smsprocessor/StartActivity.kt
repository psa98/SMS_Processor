package com.pon.smsprocessor

import android.Manifest.permission.RECEIVE_SMS
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.widget.addTextChangedListener
import com.pon.smsprocessor.TaxiRepository.authAdmin
import com.pon.smsprocessor.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


const val REQUEST_PERMISSION_SMS = 1004

class StartActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var job: Job? = null
    private fun stringTemplate() = ("Откуда (адрес) %%Куда %% кол-во мест%% время %% " +
            "Код режима заказа (Голосование, Подача )").replace("%%", DefaultsRepository.divider)


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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
        binding.divField.setText(DefaultsRepository.divider)
        binding.timeField.setText(DefaultsRepository.time.toString())
        binding.typeField.setText(DefaultsRepository.orderType.toString())

        binding.testSMS.setText(stringTemplate())
        binding.divField.addTextChangedListener {
            DefaultsRepository.divider = it.toString()
            binding.testSMS.setText(stringTemplate())
        }

        binding.timeField.addTextChangedListener {
            DefaultsRepository.time = it.toString().toIntOrNull() ?: 0
        }

        binding.typeField.addTextChangedListener {
            DefaultsRepository.orderType = it.toString().toIntOrNull() ?: 1
        }


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


    @SuppressLint("SimpleDateFormat")
    fun makeOrder(view: View) {
        val phone = binding.testPhone.text.toString()
        val sms = binding.testSMS.text.toString()
        TaxiRepository.makeOrder(phone,sms)

    }
}







