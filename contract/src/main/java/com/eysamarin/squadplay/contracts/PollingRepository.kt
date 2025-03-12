package com.eysamarin.squadplay.contracts

fun interface PollingRepository {
    fun savePollingData(data: String)
}