package com.pon.smsprocessor

import android.Manifest.permission.RECEIVE_SMS
import android.Manifest.permission.SEND_SMS
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.pon.smsprocessor.databinding.ActivityMainBinding
import kotlinx.coroutines.Job


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
        val logView = binding.logView
        val adapter = LogAdapter()
        logView.adapter = adapter
        logView.layoutManager =LinearLayoutManager(this)
        adapter.items = Logger.logItemData.value?: emptyList()


        Logger.logItemData.observe(this) { log -> adapter.items = log }

        val readyToSend = checkAndShowPermissions()
        val should = shouldShowRequestPermissionRationale(RECEIVE_SMS)
        binding.permissionsText.text = when {
            readyToSend -> "Разрешение на СМС выдано"
            !readyToSend && should -> "Перезапустите приложение и выдайте разрешение на смс"
            else -> "Выдайте разрешение на доступ к смс в настройках"
        }
        ContextCompat.startForegroundService(
            this, Intent(this, ForegroundService::class.java)
        )

        binding.stopCodes.setText(DefaultsRepository.stopCodes)
        binding.divField.setText(DefaultsRepository.divider)
        binding.timeField.setText(DefaultsRepository.time.toString())
        binding.typeField.setText(DefaultsRepository.orderType.toString())
        binding.cancelTimeField.setText(DefaultsRepository.cancelTime.toString())
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

        binding.cancelTimeField.addTextChangedListener {
            DefaultsRepository.cancelTime = it.toString().toIntOrNull() ?: 3
        }


        binding.stopCodes.addTextChangedListener {
            DefaultsRepository.stopCodes = it.toString()
        }
    }


    private fun checkAndShowPermissions(): Boolean {
        val smsPermission =
            ContextCompat.checkSelfPermission(this, RECEIVE_SMS) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, SEND_SMS) == PERMISSION_GRANTED
        if (!smsPermission) {
            val permissions = arrayOf(RECEIVE_SMS, SEND_SMS)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_SMS)
        }
        return smsPermission
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_PERMISSION_SMS) return
        val readyToSend = grantResults.all { it == PERMISSION_GRANTED }
        val should = shouldShowRequestPermissionRationale(RECEIVE_SMS)
        binding.permissionsText.text = when {
            readyToSend -> "Разрешение на СМС выдано"
            !readyToSend && should -> "Выдайте разрешение на смс"
            else -> "Выдайте разрешение на доступ к смс в настройках"
        }
        if (!readyToSend) binding.permissionsText.setTextColor(Color.RED)

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

    fun sendLog(view: View) {
        Logger.sendLog (this)
    }
}







