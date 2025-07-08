package com.example.twofactorauthapp.data

import androidx.room.*

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun getAccountByIdSync(accountId: Int): AccountEntity?
}
