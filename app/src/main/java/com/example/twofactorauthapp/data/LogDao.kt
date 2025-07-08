package com.example.twofactorauthapp.data

import androidx.room.*

@Dao
interface LogDao {

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Delete
    suspend fun deleteLog(log: LogEntity)
}
