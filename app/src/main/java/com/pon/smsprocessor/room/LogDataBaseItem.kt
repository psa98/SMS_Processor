package com.pon.smsprocessor.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "logData", indices = [Index(value = ["timestamp"])])
class LogDataBaseItem  (val time:String,
                        val important:Boolean,
                        val text:String="",
                        @PrimaryKey(autoGenerate = true)
                        val id: Int=0,
                        val timestamp: Long = System.currentTimeMillis(),

)