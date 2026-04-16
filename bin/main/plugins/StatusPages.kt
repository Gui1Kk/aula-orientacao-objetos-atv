import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Plugin de tratamento centralizado de erros.
 *
 * Captura ApiException e exceções genéricas, retornando ApiResponse padronizado.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(cause.statusCode),
                ApiResponse.error(cause.message, cause.errors)
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error(cause.message ?: "Requisição inválida")
            )
        }

        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error("Erro interno do servidor")
            )
        }
    }
}
