import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateInstituicaoRequest(
    val nome: String,
    val ativo: Boolean = true,
    val configuracoes: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class SolicitarInstituicaoRequest(
    val nome: String,
    val cnpj: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val responsavel: String? = null,
    val endereco: String? = null,
    val descricao: String? = null
)

@Serializable
data class UpdateInstituicaoRequest(
    val nome: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val responsavel: String? = null,
    val endereco: String? = null,
    val descricao: String? = null,
    val ativo: Boolean? = null,
    val configuracoes: Map<String, JsonElement>? = null
)

@Serializable
data class RejeitarInstituicaoRequest(
    val motivoRejeicao: String? = null
)
