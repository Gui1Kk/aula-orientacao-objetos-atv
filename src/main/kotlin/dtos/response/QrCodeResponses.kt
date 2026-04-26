import kotlinx.serialization.Serializable

@Serializable
data class QrCodeFilaResponse(
    val qrCode: QrCode,
    val fila: Fila
)
