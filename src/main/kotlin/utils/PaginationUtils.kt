import io.ktor.server.application.*
import kotlinx.serialization.Serializable

/**
 * Utilitários de paginação para endpoints com listagem.
 *
 * Uso: val (page, limit) = call.parsePagination()
 */
data class PaginationParams(
    val page: Int,
    val limit: Int
) {
    val skip: Int get() = (page - 1) * limit
}

@Serializable
data class PaginatedResponse<T>(
    val docs: List<T>,
    val totalDocs: Long,
    val limit: Int,
    val totalPages: Int,
    val page: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean
)

/**
 * Extrai parâmetros de paginação da query string.
 */
fun ApplicationCall.parsePagination(defaultLimit: Int = Constants.LIMIT_DEFAULT): PaginationParams {
    val page = request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: Constants.PAGE_DEFAULT
    val limit = request.queryParameters["limit"]?.toIntOrNull()
        ?.coerceIn(1, Constants.LIMIT_MAX) ?: defaultLimit
    return PaginationParams(page, limit)
}

/**
 * Constrói a resposta paginada padrão (conforme Apêndice C da especificação).
 */
fun <T> buildPaginatedResponse(
    docs: List<T>,
    totalDocs: Long,
    pagination: PaginationParams
): PaginatedResponse<T> {
    val totalPages = if (pagination.limit > 0) {
        ((totalDocs + pagination.limit - 1) / pagination.limit).toInt()
    } else 1

    return PaginatedResponse(
        docs = docs,
        totalDocs = totalDocs,
        limit = pagination.limit,
        totalPages = totalPages,
        page = pagination.page,
        hasPrevPage = pagination.page > 1,
        hasNextPage = pagination.page < totalPages
    )
}
