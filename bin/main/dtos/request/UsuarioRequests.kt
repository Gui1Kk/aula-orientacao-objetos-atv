import kotlinx.serialization.Serializable

/**
 * DTOs de requisição para endpoints de usuários (§9.4).
 */

@Serializable
data class CreateUsuarioInstituicaoRequest(
    val nome: String,
    val email: String,
    val papeis: List<String>
)

@Serializable
data class UpdateUsuarioRequest(
    val nome: String? = null,
    val papeis: List<String>? = null,
    val ativo: Boolean? = null,
    val instituicaoId: String? = null
)
