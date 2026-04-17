import kotlinx.serialization.Serializable

@Serializable
data class ConfiguracaoQRCodeRequest(
    val modoQRCode: String = "ROTATIVO",
    val tempoExibicaoMin: Int? = null,
    val tempoExpiracaoMin: Int? = null,
    val toleranciaMin: Int? = null,
    val tempoAlertaSegundos: Int? = null
) {
    fun toModel(): ConfiguracaoQRCode =
        ConfiguracaoQRCode(
            modoQRCode = try {
                ModoQRCode.valueOf(modoQRCode.uppercase())
            } catch (_: Exception) {
                throw ApiException(400, "modoQRCode inválido: $modoQRCode")
            },
            tempoExibicaoMin = tempoExibicaoMin,
            tempoExpiracaoMin = tempoExpiracaoMin,
            toleranciaMin = toleranciaMin,
            tempoAlertaSegundos = tempoAlertaSegundos
        )
}

@Serializable
data class MesaRequest(
    val numero: String,
    val nome: String? = null,
    val ativa: Boolean = true
) {
    fun toModel(): Mesa = Mesa(
        numero = numero.trim(),
        nome = nome?.trim(),
        ativa = ativa
    )
}

@Serializable
data class CreateFilaRequest(
    val nome: String,
    val tipoAtendimento: String,
    val ativa: Boolean = true,
    val prioridadesHabilitadas: Boolean = false,
    val fidelidadeHabilitada: Boolean = false,
    val tempoMaximoAtendimento: Int? = null,
    val configuracaoQRCode: ConfiguracaoQRCodeRequest? = null,
    val mesas: List<MesaRequest> = emptyList()
)

@Serializable
data class UpdateFilaRequest(
    val nome: String? = null,
    val tipoAtendimento: String? = null,
    val ativa: Boolean? = null,
    val prioridadesHabilitadas: Boolean? = null,
    val fidelidadeHabilitada: Boolean? = null,
    val tempoMaximoAtendimento: Int? = null,
    val configuracaoQRCode: ConfiguracaoQRCodeRequest? = null,
    val mesas: List<MesaRequest>? = null
)
