package com.example.testtask.database

import com.example.testtask.models.Account
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    val accountName: Flow<String?> = accountDao.getAccountName()

    suspend fun insertAccount(name: String) {
        accountDao.insert(
            Account(name = name)
        )
    }
}