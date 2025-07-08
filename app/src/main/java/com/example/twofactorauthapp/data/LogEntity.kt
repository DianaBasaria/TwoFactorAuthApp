package com.example.twofactorauthapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val accountName: String,
    val timestamp: Long,
    val status: String
)
