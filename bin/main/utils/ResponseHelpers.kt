import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/**
 * Helper para responses padronizados que evitam problemas de serialização.
 */
suspend inline fun <reified T> ApplicationCall.respondSuccess(
    data: T,
    message: String? = null,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    response.status(status)
    respond(ApiResponse.success(data, message))
}

suspend inline fun ApplicationCall.respondError(
    message: String,
    errors: List<String>? = null,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    response.status(status)
    respond(ApiResponse.error(message, errors))
}

/**
 * DTO para responses vazias (success sem dados)
 */
@Serializable
data class EmptyResponse(val message: String = "")

/**
 * Helper para success sem dados relevantes
 */
suspend inline fun ApplicationCall.respondEmptySuccess(
    message: String,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    respondSuccess(EmptyResponse(message), status = status)
}
