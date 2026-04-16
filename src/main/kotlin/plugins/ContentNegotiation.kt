import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

/**
 * Configuração do plugin ContentNegotiation com kotlinx.serialization.
 *
 * Usa a instância compartilhada AppJson que registra InstantSerializer
 * globalmente para serializar java.time.Instant como ISO 8601.
 */
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(AppJson)
    }
}
