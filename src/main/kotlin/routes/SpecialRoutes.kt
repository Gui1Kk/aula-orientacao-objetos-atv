import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

/**
 * Rotas especiais: health check, documentação estática, assetlinks e deep link redirect (§9.11)
 */
fun Route.specialRoutes() {

    // GET /health — público (usado pelo Docker/load-balancer para verificar integridade)
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
    }

    // GET /documentacao/{...} — público (arquivos estáticos da pasta documentacao/)
    staticFiles("/documentacao", File("documentacao")) {
        default("index.html")
    }

    // GET /.well-known/assetlinks.json — público
    get("/.well-known/assetlinks.json") {
        val assetLinks = listOf(
            mapOf(
                "relation" to listOf("delegate_permission/common.handle_all_urls"),
                "target" to mapOf(
                    "namespace" to "android_app",
                    "package_name" to "br.com.filacidada",
                    "sha256_cert_fingerprints" to listOf(
                        System.getenv("ANDROID_CERT_FINGERPRINT") ?: ""
                    )
                )
            )
        )
        call.respond(HttpStatusCode.OK, assetLinks)
    }

    // GET /r/{codigo} — público (redirect para deep link)
    get("/r/{codigo}") {
        val codigo = call.parameters["codigo"] ?: throw ApiException(400, "Código obrigatório")
        val baseUrl = System.getenv("FRONTEND_URL") ?: "https://filacidada.com.br"
        call.respondRedirect("$baseUrl/fila/$codigo")
    }
}

