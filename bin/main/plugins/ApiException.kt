/**
 * Exceção customizada para erros de negócio da API.
 *
 * Capturada pelo plugin StatusPages para gerar resposta padronizada.
 */
class ApiException(
    val statusCode: Int,
    override val message: String,
    val errors: List<String>? = null
) : RuntimeException(message)

