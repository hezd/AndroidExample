package com.hezd.practice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author hezd
 * @date 2023/8/24 09:19
 * @description
 */
class DataFlowViewModel : ViewModel() {
    val countFlow = flow{
        var count = 0
        while (true) {
            println("emit data:$count")
            emit(count)
            delay(1000)
            count++
        }
    }.onEach {

    }.map {

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),0)
    var dataFlow = flowOf("").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),0)
    private val _mutableStateFlow = MutableStateFlow<Int>(0)
    val mutableStateFlow= _mutableStateFlow
    init {
        viewModelScope.launch {
            delay(3000)
            _mutableStateFlow.emit(111)
        }
    }


}


