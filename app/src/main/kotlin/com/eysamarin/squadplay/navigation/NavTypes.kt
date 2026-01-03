package com.eysamarin.squadplay.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


/**
 * Allow pass kotlin serializable object between fragments with navigation component
 */
inline fun <reified T> serializableNavType(serializer: KSerializer<T>): NavType<T> {
    return object : NavType<T>(isNullableAllowed = false) {


        override fun get(bundle: Bundle, key: String): T? {
            return Json.decodeFromString(serializer, bundle.getString(key) ?: return null)
        }

        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putString(key, Json.encodeToString(serializer, value))
        }

        override fun parseValue(value: String): T {
            return Json.decodeFromString(serializer, Uri.decode(value))
        }

        override fun serializeAsValue(value: T): String {
            return Uri.encode(Json.encodeToString(serializer, value))
        }
    }
}