import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configura Swagger UI servindo a spec OpenAPI em /api-docs.
 * O arquivo JSON fica em resources/openapi/documentation.json.
 * A raiz (/) redireciona automaticamente para /api-docs.
 */
fun Application.configureSwagger() {
    routing {
        get("/") {
            call.respondRedirect("/api-docs")
        }
        swaggerUI(path = "api-docs", swaggerFile = "openapi/documentation.json") {
            version = "5.17.14"
        }
    }
}
