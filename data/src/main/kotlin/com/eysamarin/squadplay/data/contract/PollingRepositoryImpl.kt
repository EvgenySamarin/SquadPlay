package com.eysamarin.squadplay.data.contract

import com.eysamarin.squadplay.contracts.PollingRepository
import com.eysamarin.squadplay.data.datasource.FirebaseDatabaseDataSource

class PollingRepositoryImpl(
    val realtimeDatabaseDataSource: FirebaseDatabaseDataSource,
) : PollingRepository {

    override fun savePollingData(data: String) {
        realtimeDatabaseDataSource.saveTestData(data)
    }
}