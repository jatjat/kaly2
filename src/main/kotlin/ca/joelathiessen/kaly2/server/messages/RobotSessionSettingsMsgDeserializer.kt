package ca.joelathiessen.kaly2.server.messages

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RobotSessionSettingsMsgDeserializer : JsonDeserializer<RobotSessionSettingsReqMsg> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, type: Type,
                             context: JsonDeserializationContext): RobotSessionSettingsReqMsg {
        val jsonObject = json.asJsonObject
        val sessionID: JsonElement? = jsonObject.get("sessionID")

        return RobotSessionSettingsReqMsg(sessionID?.asLong, jsonObject.get("shouldRun").asBoolean,
                jsonObject.get("shouldReset").asBoolean
        )
    }
}