import kotlinx.serialization.Serializable

/**
 * DTOs de requisição para endpoints de examples.
 */

@Serializable
data class CreateExampleRequest(
    val nome: String,
    val email: String,
    val enumExample: List<String>
)

@Serializable
data class UpdateExampleRequest(
    val nome: String? = null,
    val email: String? = null,
    val enumExample: List<String>? = null,
    val ativo: Boolean? = null
)

