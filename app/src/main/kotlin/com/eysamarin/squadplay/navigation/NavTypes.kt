package com.eysamarin.squadplay.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.YearMonth


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

object YearMonthSerializer : KSerializer<YearMonth> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "YearMonth",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: YearMonth) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): YearMonth {
        return YearMonth.parse(decoder.decodeString())
    }
}