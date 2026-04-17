import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class CreateQrCodeRequest(
    val filaId: String,
    val codigo: String? = null,
    @Contextual val validoAte: Instant? = null,
    @Contextual val toleranciaAte: Instant? = null
)

@Serializable
data class RegenerarQrCodeRequest(
    val filaId: String
)
