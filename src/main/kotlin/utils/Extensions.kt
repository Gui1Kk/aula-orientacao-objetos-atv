import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/**
 * Extensões utilitárias para ApplicationCall e tipos comuns.
 */

/**
 * Obtém o ID do usuário autenticado a partir do JWT principal.
 * Retorna null se não autenticado.
 */
fun ApplicationCall.currentUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
}

/**
 * Obtém os papéis do usuário autenticado a partir do JWT principal.
 */
fun ApplicationCall.currentUserPapeis(): Set<Papel> {
    val papeisList = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("papeis")
        ?.asList(String::class.java)
        ?: return emptySet()

    return papeisList.mapNotNull { nome ->
        try { Papel.valueOf(nome) } catch (_: IllegalArgumentException) { null }
    }.toSet()
}

/**
 * Obtém o instituicaoId do usuário autenticado a partir do JWT principal.
 * Retorna null para USUARIO_FINAL e ADMIN_PLATAFORMA sem vínculo.
 */
fun ApplicationCall.currentInstituicaoId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("instituicaoId")?.asString()
}

/**
 * Verifica se o usuário autenticado possui ao menos um dos papéis informados.
 */
fun ApplicationCall.hasAnyRole(vararg papeis: Papel): Boolean {
    return currentUserPapeis().any { it in papeis }
}

