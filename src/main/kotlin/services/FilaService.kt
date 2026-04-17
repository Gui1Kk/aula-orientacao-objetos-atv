import java.time.Instant
import java.util.UUID

class FilaService(
    private val filaRepository: FilaRepository,
    private val senhaRepository: SenhaRepository,
    private val auditoriaService: AuditoriaService,
    private val webSocketManager: WebSocketManager
) {
    fun listar(
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<Fila> {
        val (docs, total) = filaRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): Fila =
        filaRepository.findById(id)
            ?: throw ApiException(404, "Fila não encontrada")

    fun criar(request: CreateFilaRequest, instituicaoId: String, executorId: String): Fila {
        val tipoAtendimento = try {
            TipoAtendimento.valueOf(request.tipoAtendimento.uppercase())
        } catch (_: Exception) {
            throw ApiException(400, "tipoAtendimento inválido: ${request.tipoAtendimento}")
        }

        if (request.configuracaoQRCode != null &&
            tipoAtendimento !in setOf(TipoAtendimento.PRESENCIAL, TipoAtendimento.HIBRIDO)
        ) {
            throw ApiException(400, "Configuração de QR Code só é válida para filas presenciais ou híbridas")
        }

        val fila = Fila(
            id = UUID.randomUUID().toString(),
            instituicaoId = instituicaoId,
            nome = request.nome.trim(),
            tipoAtendimento = tipoAtendimento,
            ativa = request.ativa,
            prioridadesHabilitadas = request.prioridadesHabilitadas,
            fidelidadeHabilitada = request.fidelidadeHabilitada,
            tempoMaximoAtendimento = request.tempoMaximoAtendimento,
            configuracaoQRCode = request.configuracaoQRCode?.toModel(),
            mesas = request.mesas.map { it.toModel() },
            createdAt = Instant.now()
        )

        val criada = filaRepository.insert(fila)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "Fila",
            entidadeId = criada.id,
            usuarioId = executorId,
            instituicaoId = instituicaoId
        )
        criada.id?.let { _ ->
            webSocketManager.broadcast("instituicao:$instituicaoId", "fila:criada", criada)
        }
        return criada
    }

    fun atualizar(id: String, request: UpdateFilaRequest, executorId: String): Fila {
        val atual = buscarPorId(id)

        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it.trim() }
        request.ativa?.let { updates["ativa"] = it }
        request.prioridadesHabilitadas?.let { updates["prioridadesHabilitadas"] = it }
        request.fidelidadeHabilitada?.let { updates["fidelidadeHabilitada"] = it }
        request.tempoMaximoAtendimento?.let { updates["tempoMaximoAtendimento"] = it }
        request.tipoAtendimento?.let {
            updates["tipoAtendimento"] = try {
                TipoAtendimento.valueOf(it.uppercase())
            } catch (_: Exception) {
                throw ApiException(400, "tipoAtendimento inválido: $it")
            }
        }
        request.configuracaoQRCode?.let { updates["configuracaoQRCode"] = it.toModel() }
        request.mesas?.let { updates["mesas"] = it.map(MesaRequest::toModel) }
        updates["updatedAt"] = Instant.now()

        filaRepository.update(id, updates)

        auditoriaService.registrar(
            acao = AcaoAuditoria.ATUALIZAR,
            entidade = "Fila",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = atual.instituicaoId
        )
        val atualizada = buscarPorId(id)
        webSocketManager.broadcast("instituicao:${atual.instituicaoId}", "fila:atualizada", atualizada)
        return atualizada
    }

    fun deletar(id: String, executorId: String): Boolean {
        val fila = buscarPorId(id)
        val result = filaRepository.delete(id)
        if (result) {
            auditoriaService.registrar(
                acao = AcaoAuditoria.DELETAR,
                entidade = "Fila",
                entidadeId = id,
                usuarioId = executorId,
                instituicaoId = fila.instituicaoId
            )
            webSocketManager.broadcast(
                "instituicao:${fila.instituicaoId}",
                "fila:removida",
                mapOf("filaId" to id)
            )
        }
        return result
    }

    fun contagemSenhas(id: String): ContagemSenhasResponse {
        buscarPorId(id)
        val aguardando = senhaRepository.countByFilaIdAndStatus(id, StatusSenha.AGUARDANDO)
        return ContagemSenhasResponse(aguardando = aguardando)
    }
}
