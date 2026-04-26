import java.time.Instant
import java.util.UUID

class QrCodeService(
    private val qrCodeRepository: QrCodeRepository,
    private val filaRepository: FilaRepository,
    private val auditoriaService: AuditoriaService,
    private val webSocketManager: WebSocketManager
) {
    fun listar(
        pagination: PaginationParams,
        filters: Map<String, Any?> = emptyMap()
    ): PaginatedResponse<QrCode> {
        val (docs, total) = qrCodeRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): QrCode =
        qrCodeRepository.findById(id)
            ?: throw ApiException(404, "QR Code não encontrado")

    fun buscarPorCodigo(codigo: String): QrCodeFilaResponse {
        val qrCode = qrCodeRepository.findByCodigo(codigo.trim())
            ?: throw ApiException(404, "QR Code não encontrado")

        if (!qrCode.ativo) {
            throw ApiException(404, "QR Code não encontrado")
        }

        if (Instant.now().isAfter(qrCode.toleranciaAte)) {
            throw ApiException(409, "QR Code expirado")
        }

        val fila = filaRepository.findById(qrCode.filaId)
            ?: throw ApiException(404, "Fila do QR Code não encontrada")

        return QrCodeFilaResponse(qrCode, fila)
    }

    fun criar(request: CreateQrCodeRequest, executorId: String): QrCode {
        val fila = buscarFila(request.filaId)
        val codigo = request.codigo?.trim().takeUnless { it.isNullOrBlank() } ?: gerarCodigoUnico()
        validarCodigoUnico(codigo)

        val validade = calcularValidade(fila, request.validoAte, request.toleranciaAte)
        val qrCode = QrCode(
            id = UUID.randomUUID().toString(),
            filaId = fila.id.orEmpty(),
            codigo = codigo,
            validoAte = validade.first,
            toleranciaAte = validade.second,
            ativo = true,
            createdAt = Instant.now()
        )

        val criado = qrCodeRepository.insert(qrCode)
        auditoriaService.registrar(
            acao = AcaoAuditoria.CRIAR,
            entidade = "QrCode",
            entidadeId = criado.id,
            usuarioId = executorId,
            instituicaoId = fila.instituicaoId,
            dados = mapOf("filaId" to fila.id.orEmpty())
        )
        return criado
    }

    fun desativar(id: String, executorId: String): QrCode {
        val atual = buscarPorId(id)
        val fila = buscarFila(atual.filaId)

        if (atual.ativo) {
            qrCodeRepository.deactivate(id)
            auditoriaService.registrar(
                acao = AcaoAuditoria.DESATIVAR,
                entidade = "QrCode",
                entidadeId = id,
                usuarioId = executorId,
                instituicaoId = fila.instituicaoId
            )
        }

        return buscarPorId(id)
    }

    fun regenerar(request: RegenerarQrCodeRequest, executorId: String): QrCode {
        val fila = buscarFila(request.filaId)
        val desativados = qrCodeRepository.deactivateAllAtivosByFila(fila.id.orEmpty())
        val validade = calcularValidade(fila, null, null)
        val novo = QrCode(
            id = UUID.randomUUID().toString(),
            filaId = fila.id.orEmpty(),
            codigo = gerarCodigoUnico(),
            validoAte = validade.first,
            toleranciaAte = validade.second,
            ativo = true,
            createdAt = Instant.now()
        )

        val criado = qrCodeRepository.insert(novo)
        auditoriaService.registrar(
            acao = AcaoAuditoria.REGENERAR,
            entidade = "QrCode",
            entidadeId = criado.id,
            usuarioId = executorId,
            instituicaoId = fila.instituicaoId,
            dados = mapOf(
                "filaId" to fila.id.orEmpty(),
                "qrcodesDesativados" to desativados.toString()
            )
        )
        emitirEvento(fila, criado)
        return criado
    }

    private fun buscarFila(filaId: String): Fila =
        filaRepository.findById(filaId)
            ?: throw ApiException(404, "Fila não encontrada")

    private fun calcularValidade(
        fila: Fila,
        validoAteRequest: Instant?,
        toleranciaAteRequest: Instant?
    ): Pair<Instant, Instant> {
        val configuracao = fila.configuracaoQRCode
        val agora = Instant.now()
        val validadeMin = configuracao?.tempoExpiracaoMin ?: 5
        val toleranciaMin = configuracao?.toleranciaMin ?: 2
        val validoAte = validoAteRequest ?: agora.plusSeconds(validadeMin.toLong() * 60)
        val toleranciaAte = toleranciaAteRequest ?: validoAte.plusSeconds(toleranciaMin.toLong() * 60)

        if (validoAte.isBefore(agora)) {
            throw ApiException(400, "validade do QR Code não pode estar no passado")
        }
        if (toleranciaAte.isBefore(validoAte)) {
            throw ApiException(400, "toleranciaAte não pode ser anterior a validoAte")
        }

        return Pair(validoAte, toleranciaAte)
    }

    private fun validarCodigoUnico(codigo: String) {
        if (qrCodeRepository.findByCodigo(codigo) != null) {
            throw ApiException(409, "QR Code já cadastrado")
        }
    }

    private fun gerarCodigoUnico(): String {
        repeat(5) {
            val codigo = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
            if (qrCodeRepository.findByCodigo(codigo) == null) return codigo
        }
        throw ApiException(409, "Não foi possível gerar um QR Code único")
    }

    private fun emitirEvento(fila: Fila, qrCode: QrCode) {
        webSocketManager.broadcastMany(
            listOf("instituicao:${fila.instituicaoId}", "fila:${fila.id.orEmpty()}"),
            "qrcode:regenerado",
            qrCode
        )
    }
}
