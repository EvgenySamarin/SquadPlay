package com.eysamarin.squadplay.data.contract

import android.content.Context
import com.eysamarin.squadplay.contracts.StringRepository
import com.eysamarin.squadplay.data.R

class StringRepositoryImpl(
    val appContext: Context
) : StringRepository {

    override val cannotSignText: String = appContext.getString(R.string.cannot_sign_in)

    override val alreadyInSquad: String = appContext.getString(R.string.already_in_squad)
    override fun squadNotFound(groupId: String): String = appContext
        .getString(R.string.squad_not_found, groupId)

    override fun wantToJoinSquad(groupTitle: String): String = appContext
        .getString(R.string.want_to_join_squad, groupTitle)

    override fun fromToDate(fromDate: String, toDate: String): String = appContext
        .getString(R.string.from_to_date, fromDate, toDate)

    override val youHaveNoSquad: String = appContext.getString(R.string.you_have_no_squad)
    override val eventSaved: String = appContext.getString(R.string.event_saved)
    override val eventSaveFailed: String = appContext.getString(R.string.event_save_failed)
    override val joinedSquad: String = appContext.getString(R.string.join_squad_success)
    override val joinSquadFailed: String = appContext.getString(R.string.join_squad_failed)
}