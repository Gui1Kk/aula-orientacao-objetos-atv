import kotlinx.serialization.Serializable

@Serializable
data class CreateSenhaRequest(
    val qrCode: String? = null,
    val prioridade: String? = null
)

@Serializable
data class CreateSenhaPresencialRequest(
    val nomeCidadao: String,
    val prioridade: String? = null
)
