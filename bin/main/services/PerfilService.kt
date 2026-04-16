import java.time.Instant
import java.time.ZoneId

/**
 * Service de Perfil: me, atualizar, alterar senha, avatar, timezones.
 */
class PerfilService(
    private val usuarioRepository: UsuarioRepository
) {

    fun me(userId: String): MeResponse {
        val usuario = usuarioRepository.findById(userId)
            ?: throw ApiException(404, "Usuário não encontrado")

        return MeResponse(
            id = usuario.id!!,
            nome = usuario.nome,
            email = usuario.email,
            papeis = usuario.papeis,
            ativo = usuario.ativo,
            avatar = usuario.avatar,
            fusoHorario = usuario.fusoHorario,
            instituicaoId = usuario.instituicaoId
        )
    }

    fun atualizarPerfil(userId: String, request: UpdatePerfilRequest): Boolean {
        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it }
        request.email?.let { updates["email"] = it.lowercase() }
        request.fusoHorario?.let {
            // Valida se o fuso horário existe
            try { ZoneId.of(it) } catch (_: Exception) { throw ApiException(400, "Fuso horário inválido") }
            updates["fusoHorario"] = it
        }
        updates["updatedAt"] = Instant.now()

        return usuarioRepository.update(userId, updates)
    }

    fun alterarSenha(userId: String, request: AlterarSenhaRequest) {
        val usuario = usuarioRepository.findById(userId)
            ?: throw ApiException(404, "Usuário não encontrado")

        if (!PasswordUtils.verifyPassword(request.senhaAtual, usuario.senhaHash))
            throw ApiException(401, "Senha atual incorreta")

        usuarioRepository.update(userId, mapOf(
            "senhaHash" to PasswordUtils.hashPassword(request.novaSenha),
            "updatedAt" to Instant.now()
        ))
    }

    fun salvarAvatar(userId: String, caminhoArquivo: String): Boolean {
        return usuarioRepository.update(userId, mapOf(
            "avatar" to caminhoArquivo,
            "updatedAt" to Instant.now()
        ))
    }

    fun removerAvatar(userId: String): Boolean {
        return usuarioRepository.update(userId, mapOf(
            "avatar" to null,
            "updatedAt" to Instant.now()
        ))
    }

    fun listarTimezones(): TimezoneResponse {
        val fusos = ZoneId.getAvailableZoneIds().sorted()
        return TimezoneResponse(fusos = fusos, padrao = Constants.FUSO_HORARIO_PADRAO)
    }
}

