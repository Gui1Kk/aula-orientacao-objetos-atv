import kotlinx.serialization.Serializable

/**
 * DTOs de requisição para endpoints de perfil (§9.9).
 */

@Serializable
data class UpdatePerfilRequest(
    val nome: String? = null,
    val email: String? = null,
    val fusoHorario: String? = null
)

@Serializable
data class AlterarSenhaRequest(
    val senhaAtual: String,
    val novaSenha: String
)
