import java.util.UUID

/**
 * Service de Usuários: CRUD + listagem por instituição.
 */
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val emailService: EmailService
) {

    fun listar(pagination: PaginationParams, filters: Map<String, Any?>): PaginatedResponse<Usuario> {
        val (docs, total) = usuarioRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun listarPorInstituicao(
        instituicaoId: String,
        pagination: PaginationParams,
        filters: Map<String, Any?>
    ): PaginatedResponse<Usuario> {
        val (docs, total) = usuarioRepository.findByInstituicaoId(
            instituicaoId, pagination.page, pagination.limit, filters
        )
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): Usuario {
        return usuarioRepository.findById(id) ?: throw ApiException(404, "Usuário não encontrado")
    }

    /**
     * Criação password-less: cria usuário vinculado a uma instituição sem senha.
     * O usuário deverá definir senha via recuperação de senha (§8.5).
     */
    fun criarParaInstituicao(
        request: CreateUsuarioInstituicaoRequest,
        instituicaoId: String,
        criadorId: String
    ): Usuario {
        if (usuarioRepository.findByEmail(request.email) != null)
            throw ApiException(400, "E-mail já cadastrado")

        val papeis = request.papeis.map {
            try { Papel.valueOf(it) } catch (_: Exception) { throw ApiException(400, "Papel inválido: $it") }
        }.toSet()

        val usuario = Usuario(
            id = UUID.randomUUID().toString(),
            nome = request.nome,
            email = request.email.lowercase(),
            senhaHash = "", // password-less — sem senha inicial
            papeis = papeis,
            instituicaoId = instituicaoId
        )

        val criado = usuarioRepository.insert(usuario)

        // Gera token + código de recuperação para o usuário definir sua senha
        val tokenUnico = UUID.randomUUID().toString()
        val codigo = (1..Constants.CODIGO_RECUPERACAO_LENGTH)
            .map { ('A'..'Z').random() }
            .joinToString("")
        val expiracao = java.time.Instant.now().plusMillis(Constants.CODIGO_RECUPERACAO_EXPIRATION_MS)

        usuarioRepository.update(criado.id!!, mapOf(
            "tokenUnico" to tokenUnico,
            "codigoRecuperaSenha" to codigo,
            "expCodigoRecuperaSenha" to expiracao
        ))

        // Envia e-mail de boas-vindas quando MAIL_ENABLED=true
        emailService.sendWelcomeEmail(criado.email, criado.nome, tokenUnico, codigo)

        return criado
    }

    fun atualizar(id: String, request: UpdateUsuarioRequest, executorId: String): Boolean {
        buscarPorId(id) // verifica existência

        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it }
        request.ativo?.let { updates["ativo"] = it }
        request.instituicaoId?.let { updates["instituicaoId"] = it }
        request.papeis?.let { papeisList ->
            updates["papeis"] = papeisList.map { Papel.valueOf(it) }.toSet()
        }
        updates["updatedAt"] = java.time.Instant.now()

        val result = usuarioRepository.update(id, updates)
        return result
    }

    fun deletar(id: String, executorId: String): Boolean {
        buscarPorId(id) // verifica existência
        return usuarioRepository.delete(id)
    }
}

