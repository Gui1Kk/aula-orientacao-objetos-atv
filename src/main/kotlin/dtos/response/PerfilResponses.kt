import kotlinx.serialization.Serializable

/**
 * DTOs de resposta para endpoints de perfil (§9.9).
 */

@Serializable
data class MeResponse(
    val id: String,
    val nome: String,
    val email: String,
    val papeis: Set<Papel>,
    val ativo: Boolean,
    val avatar: String?,
    val fusoHorario: String,
    val instituicaoId: String?
)

@Serializable
data class TimezoneResponse(
    val fusos: List<String>,
    val padrao: String
)

