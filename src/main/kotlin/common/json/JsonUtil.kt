package org.example.common.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

object JsonUtil {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun <T> encodeToJson(v: T, serializer: KSerializer<T>): String {
        return json.encodeToString(serializer, v)
    }

    fun <T> decodeFromJson(jsonString: String, deserializer: KSerializer<T>): T {
        return json.decodeFromString(deserializer, jsonString)
    }
}