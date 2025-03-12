package com.eysamarin.squadplay.domain.polling

import com.eysamarin.squadplay.contracts.PollingRepository

fun interface PollingProvider {
    fun savePollingData(data: String)
}

class PollingProviderImpl(
    private val pollingRepository: PollingRepository,
) : PollingProvider {
    override fun savePollingData(data: String) {
        pollingRepository.savePollingData(data)
    }
}
