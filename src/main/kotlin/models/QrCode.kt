import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class QrCode(
    val id: String? = null,
    val filaId: String,
    val codigo: String,
    @Contextual val validoAte: Instant,
    @Contextual val toleranciaAte: Instant,
    val ativo: Boolean = true,
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
)
