import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Representa um model exemplo da plataforma FilaCidadã.
 *
 * Vamos colocar um ENUM de exemplo
 */
@Serializable
data class Example(
    val id: String? = null,
    val nome: String,
    val email: String,
    val enumExample: Set<EnumExample>,
    val ativo: Boolean = true,
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
)

/**
 * Papéis do sistema conforme §6 da especificação.
 *
 * - EXAMPLES ENUM: visão global, gerencia instituições e usuários.
 */
enum class EnumExample {
    EXAMPLE_ONE,
    EXAMPLE_TWO,
    EXAMPLE_THREE,
    EXAMPLE_FOUR
}