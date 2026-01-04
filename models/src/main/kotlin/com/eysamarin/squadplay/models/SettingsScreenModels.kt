package com.eysamarin.squadplay.models

sealed interface SettingsScreenAction {
    object OnBackButtonTap : SettingsScreenAction
    object OnLicensesTap : SettingsScreenAction
}