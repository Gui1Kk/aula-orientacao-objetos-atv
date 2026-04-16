import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin

/**
 * Entry point da API — Kotlin + Ktor + Netty.
 *
 * Inicializa: Koin (DI), plugins (JSON, CORS, JWT, StatusPages, Swagger)
 * e registra as rotas de Usuário.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 7351
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {

    // Injecao de dependencia (Koin)
    install(Koin) {
        modules(appModule)
    }

    val jwtConfig = JwtConfig()

    // Plugins
    configureContentNegotiation()
    configureCORS()
    configureAuthentication(jwtConfig)
    configureStatusPages()
    configureSwagger()

    // Rotas
    routing {
        authRoutes()
        usuarioRoutes()
        perfilRoutes()
        specialRoutes()
        exampleRoutes()
    }
}

