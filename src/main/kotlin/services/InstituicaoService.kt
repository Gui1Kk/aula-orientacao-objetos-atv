import java.time.Instant
import java.util.UUID

class InstituicaoService(
    private val instituicaoRepository: InstituicaoRepository,
    private val auditoriaService: AuditoriaService
) {
    fun listar(
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<Instituicao> {
        val (docs, total) = instituicaoRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): Instituicao =
        instituicaoRepository.findById(id)
            ?: throw ApiException(404, "Instituição não encontrada")

    fun criar(request: CreateInstituicaoRequest, executorId: String): Instituicao {
        validarNomeUnico(request.nome)
        val agora = Instant.now()
        val instituicao = Instituicao(
            id = UUID.randomUUID().toString(),
            nome = request.nome.trim(),
            ativo = request.ativo,
            status = StatusInstituicao.APROVADA,
            aprovadoPor = executorId,
            aprovadoEm = agora,
            configuracoes = request.configuracoes,
            createdAt = agora
        )
        val criada = instituicaoRepository.insert(instituicao)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "Instituicao",
            entidadeId = criada.id,
            usuarioId = executorId,
            instituicaoId = criada.id,
            dados = mapOf("modo" to "criacao_direta")
        )
        return criada
    }

    fun solicitar(request: SolicitarInstituicaoRequest, solicitanteId: String): Instituicao {
        val instituicao = Instituicao(
            id = UUID.randomUUID().toString(),
            nome = request.nome.trim(),
            cnpj = request.cnpj?.trim().orEmpty(),
            email = request.email?.trim().orEmpty(),
            telefone = request.telefone?.trim().orEmpty(),
            responsavel = request.responsavel?.trim().orEmpty(),
            endereco = request.endereco?.trim().orEmpty(),
            descricao = request.descricao?.trim().orEmpty(),
            status = StatusInstituicao.PENDENTE,
            solicitanteId = solicitanteId,
            createdAt = Instant.now()
        )
        val criada = instituicaoRepository.insert(instituicao)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "Instituicao",
            entidadeId = criada.id,
            usuarioId = solicitanteId,
            instituicaoId = criada.id,
            dados = mapOf("modo" to "solicitacao")
        )
        return criada
    }

    fun atualizar(id: String, request: UpdateInstituicaoRequest, executorId: String): Instituicao {
        val atual = buscarPorId(id)
        request.nome?.let { novoNome ->
            val existente = instituicaoRepository.findByNome(novoNome.trim())
            if (existente != null && existente.id != id) {
                throw ApiException(400, "Já existe uma instituição com este nome")
            }
        }

        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it.trim() }
        request.cnpj?.let { updates["cnpj"] = it.trim() }
        request.email?.let { updates["email"] = it.trim() }
        request.telefone?.let { updates["telefone"] = it.trim() }
        request.responsavel?.let { updates["responsavel"] = it.trim() }
        request.endereco?.let { updates["endereco"] = it.trim() }
        request.descricao?.let { updates["descricao"] = it.trim() }
        request.ativo?.let { updates["ativo"] = it }
        request.configuracoes?.let { updates["configuracoes"] = it }
        updates["updatedAt"] = Instant.now()

        if (updates.size == 1) return atual

        instituicaoRepository.update(id, updates)
        auditoriaService.registrar(
            acao = AcaoAuditoria.ATUALIZAR,
            entidade = "Instituicao",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = id
        )
        return buscarPorId(id)
    }

    fun deletar(id: String, executorId: String): Boolean {
        buscarPorId(id)
        val result = instituicaoRepository.delete(id)
        if (result) {
            auditoriaService.registrar(
                acao = AcaoAuditoria.DELETAR,
                entidade = "Instituicao",
                entidadeId = id,
                usuarioId = executorId,
                instituicaoId = id
            )
        }
        return result
    }

    fun aprovar(id: String, executorId: String): Instituicao {
        val instituicao = buscarPorId(id)
        if (instituicao.status != StatusInstituicao.PENDENTE) {
            throw ApiException(409, "Apenas instituições pendentes podem ser aprovadas")
        }

        instituicaoRepository.update(
            id,
            mapOf(
                "status" to StatusInstituicao.APROVADA,
                "ativo" to true,
                "aprovadoPor" to executorId,
                "aprovadoEm" to Instant.now(),
                "motivoRejeicao" to "",
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.APROVAR,
            entidade = "Instituicao",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = id
        )
        return buscarPorId(id)
    }

    fun rejeitar(id: String, motivoRejeicao: String?, executorId: String): Instituicao =
        rejeitar(id, RejeitarInstituicaoRequest(motivoRejeicao), executorId)

    fun rejeitar(id: String, request: RejeitarInstituicaoRequest, executorId: String): Instituicao {
        val instituicao = buscarPorId(id)
        if (instituicao.status != StatusInstituicao.PENDENTE) {
            throw ApiException(409, "Apenas instituições pendentes podem ser rejeitadas")
        }

        val motivo = request.motivoRejeicao?.trim().orEmpty()

        instituicaoRepository.update(
            id,
            mapOf(
                "status" to StatusInstituicao.REJEITADA,
                "motivoRejeicao" to motivo,
                "aprovadoPor" to executorId,
                "aprovadoEm" to Instant.now(),
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.REJEITAR,
            entidade = "Instituicao",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = id,
            dados = if (motivo.isBlank()) null else mapOf("motivoRejeicao" to motivo)
        )
        return buscarPorId(id)
    }

    fun reconsiderar(id: String, executorId: String): Instituicao {
        val instituicao = buscarPorId(id)
        if (instituicao.status != StatusInstituicao.REJEITADA) {
            throw ApiException(404, "Apenas instituições rejeitadas podem voltar para pendente")
        }

        instituicaoRepository.update(
            id,
            mapOf(
                "status" to StatusInstituicao.PENDENTE,
                "motivoRejeicao" to "",
                "aprovadoPor" to null,
                "aprovadoEm" to null,
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.RECONSIDERAR,
            entidade = "Instituicao",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = id
        )
        return buscarPorId(id)
    }

    private fun validarNomeUnico(nome: String) {
        if (instituicaoRepository.findByNome(nome.trim()) != null) {
            throw ApiException(400, "Nome de instituição já cadastrado")
        }
    }
}
