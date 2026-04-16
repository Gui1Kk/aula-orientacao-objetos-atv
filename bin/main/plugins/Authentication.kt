import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

/**
 * Configuração do plugin de autenticação JWT (Ktor Auth).
 */
fun Application.configureAuthentication(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("id").asString()
                if (userId != null) JWTPrincipal(credential.payload)
                else null
            }
            challenge { _, _ ->
                call.respond(
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    ApiResponse.error("Token inválido ou ausente")
                )
            }
        }
    }
}

