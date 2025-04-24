package com.eysamarin.squadplay.contracts

interface StringRepository {
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