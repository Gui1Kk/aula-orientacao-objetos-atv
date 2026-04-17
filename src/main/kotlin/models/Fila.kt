import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
enum class TipoAtendimento {
    ONLINE,
    PRESENCIAL,
    HIBRIDO
}

@Serializable
enum class ModoQRCode {
    ROTATIVO,
    FIXO
}

@Serializable
data class ConfiguracaoQRCode(
    val modoQRCode: ModoQRCode = ModoQRCode.ROTATIVO,
    val tempoExibicaoMin: Int? = null,
    val tempoExpiracaoMin: Int? = null,
    val toleranciaMin: Int? = null,
    val tempoAlertaSegundos: Int? = null
)

@Serializable
data class Mesa(
    val numero: String,
    val nome: String? = null,
    val ativa: Boolean = true
)

@Serializable
data class Fila(
    val id: String? = null,
    val instituicaoId: String,
    val nome: String,
    val tipoAtendimento: TipoAtendimento,
    val ativa: Boolean = true,
    val prioridadesHabilitadas: Boolean = false,
    val fidelidadeHabilitada: Boolean = false,
    val tempoMaximoAtendimento: Int? = null,
    val configuracaoQRCode: ConfiguracaoQRCode? = null,
    val mesas: List<Mesa> = emptyList(),
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
)
