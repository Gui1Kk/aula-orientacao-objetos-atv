import java.time.Instant
import java.util.UUID

/**
 * Service de Usuários: CRUD + listagem por instituição.
 */
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val auditoriaService: AuditoriaService? = null,
    private val emailService: EmailService = EmailService()
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
        val email = request.email.lowercase().trim()
        if (usuarioRepository.findByEmail(email) != null)
            throw ApiException(400, "E-mail já cadastrado")

        val papeis = request.papeis.map { papel ->
            try {
                Papel.valueOf(papel)
            } catch (_: Exception) {
                throw ApiException(400, "Papel inválido: $papel")
            }
        }.toSet()

        val tokenUnico = UUID.randomUUID().toString()
        val codigo = (1..Constants.CODIGO_RECUPERACAO_LENGTH)
            .map { ('A'..'Z').random() }
            .joinToString("")
        val expiracao = Instant.now().plusMillis(Constants.CODIGO_RECUPERACAO_EXPIRATION_MS)

        val usuario = Usuario(
            id = UUID.randomUUID().toString(),
            nome = request.nome.trim(),
            email = email,
            senhaHash = "", // password-less — sem senha inicial
            papeis = papeis,
            instituicaoId = instituicaoId,
            tokenUnico = tokenUnico,
            codigoRecuperaSenha = codigo,
            expCodigoRecuperaSenha = expiracao
        )

        val criado = usuarioRepository.insert(usuario)

        // Envia e-mail de boas-vindas quando MAIL_ENABLED=true
        emailService.sendWelcomeEmail(criado.email, criado.nome, tokenUnico, codigo)
        auditoriaService?.registrar(AcaoAuditoria.CRIAR, "Usuario", criado.id, criadorId)

        return criado
    }

    fun atualizar(id: String, request: UpdateUsuarioRequest, executorId: String): Boolean {
        buscarPorId(id) // verifica existência

        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it.trim() }
        request.ativo?.let { updates["ativo"] = it }
        request.instituicaoId?.let { updates["instituicaoId"] = it }
        request.papeis?.let { papeisList ->
            updates["papeis"] = papeisList.map { papel ->
                try {
                    Papel.valueOf(papel)
                } catch (_: Exception) {
                    throw ApiException(400, "Papel inválido: $papel")
                }
            }.toSet()
        }
        updates["updatedAt"] = Instant.now()

        val result = usuarioRepository.update(id, updates)
        if (result) {
            auditoriaService?.registrar(AcaoAuditoria.ATUALIZAR, "Usuario", id, executorId)
        }
        return result
    }

    fun deletar(id: String, executorId: String): Boolean {
        buscarPorId(id) // verifica existência
        val result = usuarioRepository.delete(id)
        if (result) {
            auditoriaService?.registrar(AcaoAuditoria.DELETAR, "Usuario", id, executorId)
        }
        return result
    }
}

