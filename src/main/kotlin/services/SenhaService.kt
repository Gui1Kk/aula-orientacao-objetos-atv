import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class SenhaService(
    private val senhaRepository: SenhaRepository,
    private val filaRepository: FilaRepository,
    private val qrCodeRepository: QrCodeRepository,
    private val auditoriaService: AuditoriaService,
    private val webSocketManager: WebSocketManager
) {
    fun listar(
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<Senha> {
        val (docs, total) = senhaRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun listarPorFila(
        filaId: String,
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<Senha> {
        val (docs, total) = senhaRepository.findByFilaId(filaId, pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): Senha =
        senhaRepository.findById(id)
            ?: throw ApiException(404, "Senha não encontrada")

    fun criar(filaId: String, usuarioId: String, request: CreateSenhaRequest): Senha {
        val fila = buscarFilaApta(filaId)

        if (senhaRepository.findActiveByUsuarioAndFila(usuarioId, filaId) != null) {
            throw ApiException(409, "O usuário já possui uma senha ativa nesta fila")
        }

        if (
            fila.configuracaoQRCode != null &&
            fila.tipoAtendimento in setOf(TipoAtendimento.PRESENCIAL, TipoAtendimento.HIBRIDO)
        ) {
            val codigo = request.qrCode?.trim()
            if (codigo.isNullOrBlank()) {
                throw ApiException(400, "QR Code é obrigatório para filas presenciais ou híbridas")
            }
            validarQrCode(filaId, codigo)
        }

        val prioridade = parsePrioridade(fila, request.prioridade)
        val senha = Senha(
            id = UUID.randomUUID().toString(),
            filaId = filaId,
            instituicaoId = fila.instituicaoId,
            usuarioId = usuarioId,
            presencial = false,
            posicao = calcularPosicao(filaId, prioridade),
            status = StatusSenha.AGUARDANDO,
            prioridade = prioridade.takeUnless { it == Prioridade.CRONOLOGICA }?.name,
            createdAt = Instant.now()
        )

        val criada = senhaRepository.insert(senha)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "Senha",
            entidadeId = criada.id,
            usuarioId = usuarioId,
            instituicaoId = fila.instituicaoId,
            dados = mapOf("origem" to "digital")
        )
        emitirEvento(fila, "senha:criada", criada)
        return criada
    }

    fun criarPresencial(filaId: String, request: CreateSenhaPresencialRequest, executorId: String): Senha {
        val fila = buscarFilaApta(filaId)
        val prioridade = parsePrioridade(fila, request.prioridade)

        val senha = Senha(
            id = UUID.randomUUID().toString(),
            filaId = filaId,
            instituicaoId = fila.instituicaoId,
            usuarioId = null,
            nomeCidadao = request.nomeCidadao.trim(),
            presencial = true,
            posicao = calcularPosicao(filaId, prioridade),
            status = StatusSenha.AGUARDANDO,
            prioridade = prioridade.takeUnless { it == Prioridade.CRONOLOGICA }?.name,
            createdAt = Instant.now()
        )

        val criada = senhaRepository.insert(senha)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "Senha",
            entidadeId = criada.id,
            usuarioId = executorId,
            instituicaoId = fila.instituicaoId,
            dados = mapOf("origem" to "presencial")
        )
        emitirEvento(fila, "senha:criada", criada)
        return criada
    }

    fun chamar(id: String, mesa: String?, mesaNome: String?, operadorId: String): Senha {
        val senha = buscarPorId(id)
        if (senha.status != StatusSenha.AGUARDANDO) {
            throw ApiException(409, "Apenas senhas aguardando podem ser chamadas")
        }

        val fila = filaRepository.findById(senha.filaId)
            ?: throw ApiException(404, "Fila da senha não encontrada")

        senhaRepository.update(
            id,
            mapOf(
                "status" to StatusSenha.EM_ATENDIMENTO,
                "mesa" to mesa,
                "mesaNome" to mesaNome,
                "operadorId" to operadorId,
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.CHAMAR,
            entidade = "Senha",
            entidadeId = id,
            usuarioId = operadorId,
            instituicaoId = senha.instituicaoId,
            dados = buildMap {
                mesa?.let { put("mesa", it) }
                mesaNome?.let { put("mesaNome", it) }
            }.ifEmpty { null }
        )

        val atualizada = buscarPorId(id)
        emitirEvento(fila, "senha:chamada", atualizada)
        return atualizada
    }

    fun cancelar(id: String, executorId: String): Senha {
        val senha = buscarPorId(id)
        if (senha.status != StatusSenha.AGUARDANDO) {
            throw ApiException(409, "Apenas senhas aguardando podem ser canceladas")
        }

        val fila = filaRepository.findById(senha.filaId)
            ?: throw ApiException(404, "Fila da senha não encontrada")

        senhaRepository.update(
            id,
            mapOf(
                "status" to StatusSenha.CANCELADA,
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.CANCELAR,
            entidade = "Senha",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = senha.instituicaoId
        )

        val atualizada = buscarPorId(id)
        emitirEvento(fila, "senha:atualizada", atualizada)
        return atualizada
    }

    fun assumir(id: String, operadorId: String): Senha {
        val senha = buscarPorId(id)
        if (senha.status != StatusSenha.EM_ATENDIMENTO) {
            throw ApiException(409, "Apenas senhas em atendimento podem ser assumidas")
        }

        val fila = filaRepository.findById(senha.filaId)
            ?: throw ApiException(404, "Fila da senha não encontrada")

        senhaRepository.update(
            id,
            mapOf(
                "operadorId" to operadorId,
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.ATUALIZAR,
            entidade = "Senha",
            entidadeId = id,
            usuarioId = operadorId,
            instituicaoId = senha.instituicaoId,
            dados = mapOf("acao" to "assumir_atendimento")
        )

        val atualizada = buscarPorId(id)
        emitirEvento(fila, "senha:atualizada", atualizada)
        return atualizada
    }

    fun finalizar(id: String, executorId: String): Senha {
        val senha = buscarPorId(id)
        if (senha.status != StatusSenha.EM_ATENDIMENTO) {
            throw ApiException(409, "Apenas senhas em atendimento podem ser finalizadas")
        }

        val fila = filaRepository.findById(senha.filaId)
            ?: throw ApiException(404, "Fila da senha não encontrada")

        senhaRepository.update(
            id,
            mapOf(
                "status" to StatusSenha.FINALIZADA,
                "updatedAt" to Instant.now()
            )
        )

        auditoriaService.registrar(
            acao = AcaoAuditoria.FINALIZAR,
            entidade = "Senha",
            entidadeId = id,
            usuarioId = executorId,
            instituicaoId = senha.instituicaoId
        )

        val atualizada = buscarPorId(id)
        emitirEvento(fila, "senha:finalizada", atualizada)
        return atualizada
    }

    fun stats(instituicaoId: String, timezone: String = Constants.FUSO_HORARIO_PADRAO): SenhaStatsResponse {
        val zoneId = try {
            ZoneId.of(timezone)
        } catch (_: Exception) {
            throw ApiException(400, "Timezone inválido: $timezone")
        }

        val inicioDia = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId).toInstant()
        val fimDia = inicioDia.plusSeconds(24 * 60 * 60)

        val emAtendimento = senhaRepository.countByInstituicaoAndStatus(instituicaoId, StatusSenha.EM_ATENDIMENTO)
        val aguardando = senhaRepository.countByInstituicaoAndStatus(instituicaoId, StatusSenha.AGUARDANDO)
        val finalizadasHoje = senhaRepository.countFinalizadasEntre(instituicaoId, inicioDia, fimDia)
        val senhasHoje = senhaRepository.countCriadasEntre(instituicaoId, inicioDia, fimDia)

        val porFila = filaRepository.findByInstituicaoId(instituicaoId).map { fila ->
            SenhaStatsPorFila(
                filaId = fila.id.orEmpty(),
                nomeFila = fila.nome,
                aguardando = senhaRepository.countByFilaAndStatus(fila.id.orEmpty(), StatusSenha.AGUARDANDO),
                emAtendimento = senhaRepository.countByFilaAndStatus(fila.id.orEmpty(), StatusSenha.EM_ATENDIMENTO),
                finalizadasHoje = senhaRepository.countByFilaAndStatusAndCreatedBetween(
                    fila.id.orEmpty(),
                    StatusSenha.FINALIZADA,
                    inicioDia,
                    fimDia
                ),
                senhasHoje = senhaRepository.countByFilaAndCreatedBetween(fila.id.orEmpty(), inicioDia, fimDia)
            )
        }

        return SenhaStatsResponse(
            emAtendimento = emAtendimento,
            aguardando = aguardando,
            finalizadasHoje = finalizadasHoje,
            senhasHoje = senhasHoje,
            porFila = porFila
        )
    }

    private fun buscarFilaApta(filaId: String): Fila {
        val fila = filaRepository.findById(filaId)
            ?: throw ApiException(404, "Fila não encontrada")

        if (!fila.ativa) {
            throw ApiException(400, "Filas inativas não aceitam novas senhas")
        }
        return fila
    }

    private fun parsePrioridade(fila: Fila, raw: String?): Prioridade {
        if (raw.isNullOrBlank()) return Prioridade.CRONOLOGICA
        if (!fila.prioridadesHabilitadas) {
            throw ApiException(400, "Esta fila não permite prioridades")
        }

        return try {
            Prioridade.valueOf(raw.uppercase())
        } catch (_: Exception) {
            throw ApiException(400, "Prioridade inválida: $raw")
        }
    }

    private fun validarQrCode(filaId: String, codigo: String): QrCode {
        val qrCode = qrCodeRepository.findByCodigo(codigo)
            ?: throw ApiException(409, "QR Code inválido")

        if (qrCode.filaId != filaId) {
            throw ApiException(409, "QR Code não pertence à fila informada")
        }

        if (!qrCode.ativo) {
            throw ApiException(409, "QR Code está inativo")
        }

        val agora = Instant.now()
        if (agora.isAfter(qrCode.toleranciaAte)) {
            throw ApiException(409, "QR Code expirado")
        }

        return qrCode
    }

    private fun calcularPosicao(filaId: String, prioridade: Prioridade): Int {
        val ativas = senhaRepository.countByFilaIdAndStatuses(
            filaId,
            setOf(StatusSenha.AGUARDANDO, StatusSenha.EM_ATENDIMENTO)
        )
        return ativas.toInt() + 1
    }

    private fun emitirEvento(fila: Fila, evento: String, senha: Senha) {
        val rooms = mutableListOf(
            "instituicao:${fila.instituicaoId}",
            "fila:${fila.id.orEmpty()}"
        )
        senha.usuarioId?.let { rooms.add("user:$it") }
        webSocketManager.broadcastMany(rooms, evento, senha)
    }
}
