package com.pon.smsprocessor.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LogsDao {

    @Query("SELECT * FROM logData Order by timestamp DESC limit :limit ")
    suspend fun getLatest(limit:Int): List<LogDataBaseItem>

    @Query("SELECT * FROM logData Order by timestamp DESC")
    suspend fun getAll(): List<LogDataBaseItem>

    @Query(   "DELETE from logData")
    suspend fun clearBase()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(newRecord: LogDataBaseItem)

    @Query("SELECT COUNT(*) FROM logData" )
    suspend fun getCount():Int


}