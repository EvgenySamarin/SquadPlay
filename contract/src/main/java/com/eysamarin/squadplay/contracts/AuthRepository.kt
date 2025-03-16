package com.eysamarin.squadplay.contracts

interface AuthRepository {
    fun authWithGoogle(idToken: String)
    fun isUserSigned(): Boolean
}