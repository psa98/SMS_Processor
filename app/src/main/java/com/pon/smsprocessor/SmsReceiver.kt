package com.pon.smsprocessor


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.pon.smsprocessor.TaxiRepository.makeOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isEmpty()) return
        var previousSMS = SMSListElement(0, smsMessages[0], smsMessages[0].getString())
        var smsIndex = 0
        val interimList = mutableListOf<SMSListElement>()
        // в пришедшем пакете могут быть разные смс (к примеру у них разный отправитель)
        // или продолжение одного СМС, эти приходят пачкой от одного номера и их мы объединяем
        // критерий начала нового смс в пачке - сильно разное время прихода, MIR в начале или
        // иной отправитель чем у предыдущего.
        smsMessages.forEachIndexed { index, smsMessage ->
            val sender = smsMessage.originatingAddress ?: ""
            if (index > 0) previousSMS =
                SMSListElement(index, smsMessages[index - 1], smsMessages[index - 1].getString())
            when {
                index == 0 -> smsIndex = 0
                (previousSMS.smsMessage.originatingAddress == sender
                        && kotlin.math.abs(previousSMS.smsMessage.timestampMillis - smsMessage.timestampMillis)
                        < 5000L) -> {
                }

                else -> {
                    smsIndex++
                }
            }
            interimList.add(SMSListElement(smsIndex, smsMessage, smsMessage.getString()))
        }
        val resultList = mutableListOf<SMS>()
        var currentBody = ""
        var currentIndex = 0
        var currentSender = ""

        interimList.forEachIndexed { index, sms ->
            when {
                index == 0 -> {
                    currentBody = sms.smsMessage.messageBody
                    currentSender = sms.smsMessage.originatingAddress ?: ""
                }

                sms.smsIndex == currentIndex -> {
                    currentBody += sms.smsMessage.messageBody
                    //продолжение предыдущего сообщения - дописываем в конец текущего собираемого
                }

                sms.smsIndex != currentIndex -> {
                    currentIndex = sms.smsIndex
                    //следующее сообщение в пачке - добавляем в итоговый список
                    resultList.add(SMS(currentSender, currentBody))
                    currentBody = sms.smsMessage.messageBody
                    currentSender = sms.smsMessage.originatingAddress ?: ""
                }
            }

        }
        resultList.add(SMS(currentSender, currentBody))
        processFiltered(resultList)
    }

    data class SMS(
        val sender: String,
        val body: String,
        val received: String = currentDateTime(),
        val baseReceived: Long = 0L

    ) {
        override fun toString(): String {
            return "${this.received} ${this.sender} ${this.body} "
        }

    }


    private fun processFiltered(smsList: List<SMS>) {
        smsList.forEach { sms ->
            if (sms.body.startsWith(DefaultsRepository.okMessage.take(15)) || sms.body.startsWith(
                    DefaultsRepository.failMessage.take(15)) || sms.body.startsWith("*")

            ) return
            Logger.addToLog("Получено смс от  ${sms.sender}: ${sms.body} \n От ${currentDateTime()}\n")
            makeOrder(sms.sender, sms.body)
        }
    }


}

// промежуточный класс для сборки длинных смс
data class SMSListElement(val smsIndex: Int, val smsMessage: SmsMessage, var string: String) {
    init {
        string = smsMessage.getString()
    }
}


fun currentDateTime(): String {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .format(LocalDateTime.now())
}

fun SmsMessage.getString(): String {
    return "${this.originatingAddress} ${this.messageBody} ${this.timestampMillis}"
}

