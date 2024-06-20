package com.example.testtask.models


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: Int = 0,
    val name: String
)
