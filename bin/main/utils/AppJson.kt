import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Instância compartilhada de Json para serialização consistente em toda a API.
 *
 * Usada tanto no ContentNegotiation quanto no ApiResponse.success() para
 * garantir que serializers customizados (ex.: InstantSerializer) sejam aplicados.
 */
val AppJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        contextual(InstantSerializer)
    }
}

