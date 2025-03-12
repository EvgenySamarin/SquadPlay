package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.PollingRepository
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource

class PollingRepositoryImpl(
    val firebaseDatabaseDataSource: FirebaseDatabaseDataSource,
) : PollingRepository {

    override fun savePollingData(data: String) {
        firebaseDatabaseDataSource.saveTestData(data)
    }
}