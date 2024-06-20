package com.example.testtask.ui

import androidx.lifecycle.*
import com.example.testtask.database.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _accountName = MutableStateFlow<String?>(null)
    val accountName: StateFlow<String?> = _accountName

    init {
        viewModelScope.launch {
            repository.accountName.collect { name ->
                _accountName.value = name
            }
        }
    }

    fun insertAccount(name: String) {
        viewModelScope.launch {
            repository.insertAccount(name)
        }
    }
}


class MainViewModelFactory(private val repository: AccountRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}