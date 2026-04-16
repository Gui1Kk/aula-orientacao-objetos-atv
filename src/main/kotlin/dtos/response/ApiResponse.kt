import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Wrapper não-genérico para todas as respostas da API.
 *
 * O campo `data` é armazenado como JsonElement para evitar problemas de
 * serialização com tipos genéricos no Ktor + kotlinx.serialization.
 *
 * Uso: ApiResponse.success(data), ApiResponse.error("mensagem")
 */
@Serializable
data class ApiResponse(
    val success: Boolean,
    val data: JsonElement? = null,
    val message: String? = null,
    val errors: List<String>? = null
) {
    companion object {
        inline fun <reified T> success(data: T, message: String? = null) = ApiResponse(
            success = true,
            data = AppJson.encodeToJsonElement(kotlinx.serialization.serializer<T>(), data),
            message = message
        )

        fun error(message: String, errors: List<String>? = null) = ApiResponse(
            success = false,
            message = message,
            errors = errors
        )
    }
}
