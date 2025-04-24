package com.eysamarin.squadplay.domain.resource

import com.eysamarin.squadplay.contracts.StringRepository

interface StringProvider {
    val cannotSignText: String

    val alreadyInSquad: String
    fun squadNotFound(groupId: String): String
    fun wantToJoinSquad(groupTitle: String): String
    fun fromToDate(fromDate: String, toDate: String): String
    val youHaveNoSquad: String
    val eventSaved: String
    val eventSaveFailed: String
    val joinedSquad: String
    val joinSquadFailed: String
}

class StringProviderImpl(
    private val stringRepository: StringRepository,
) : StringProvider {
    override val cannotSignText: String = stringRepository.cannotSignText

    override val alreadyInSquad: String = stringRepository.alreadyInSquad
    override fun squadNotFound(groupId: String): String = stringRepository.squadNotFound(groupId)
    override fun wantToJoinSquad(groupTitle: String): String = stringRepository
        .wantToJoinSquad(groupTitle)
    override fun fromToDate(fromDate: String, toDate: String) = stringRepository
        .fromToDate(fromDate, toDate)
    override val youHaveNoSquad = stringRepository.youHaveNoSquad
    override val eventSaved = stringRepository.eventSaved
    override val eventSaveFailed = stringRepository.eventSaveFailed
    override val joinedSquad = stringRepository.joinedSquad
    override val joinSquadFailed = stringRepository.joinSquadFailed
}