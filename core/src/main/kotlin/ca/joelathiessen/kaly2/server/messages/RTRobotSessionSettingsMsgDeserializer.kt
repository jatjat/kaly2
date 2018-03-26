package ca.joelathiessen.kaly2.server.messages

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RTRobotSessionSettingsMsgDeserializer : JsonDeserializer<RTRobotSessionSettingsReqMsg> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): RTRobotSessionSettingsReqMsg {
        val jsonObject = json.asJsonObject
        val sessionID: JsonElement? = jsonObject.get("sessionID")

        return RTRobotSessionSettingsReqMsg(sessionID?.asLong, jsonObject.get("shouldRun").asBoolean,
                jsonObject.get("shouldReset").asBoolean
        )
    }
}