import kotlinx.serialization.json.JsonElement
import org.koin.dsl.module
import java.time.Instant

val testAppModule = module {
    single { JwtConfig() }

    single<UsuarioRepository> { InMemoryUsuarioRepository() }
    single<InstituicaoRepository> { InMemoryInstituicaoRepository() }
    single<FilaRepository> { InMemoryFilaRepository() }
    single<SenhaRepository> { InMemorySenhaRepository() }
    single<QrCodeRepository> { InMemoryQrCodeRepository() }
    single<AuditoriaRepository> { InMemoryAuditoriaRepository() }
    single<LandingPageRepository> { InMemoryLandingPageRepository() }
    single<ExampleRepository> { InMemoryExampleRepository() }

    single { FileStorageService() }
    single { EmailService() }
    single { WebSocketManager() }

    single { AuditoriaService(get()) }
    single { AuthService(get(), get(), get(), get()) }
    single { UsuarioService(get(), get(), get()) }
    single { InstituicaoService(get(), get()) }
    single { FilaService(get(), get(), get(), get()) }
    single { SenhaService(get(), get(), get(), get(), get()) }
    single { QrCodeService(get(), get(), get(), get()) }
    single { LandingPageService(get()) }
    single { PerfilService(get()) }
    single { ExampleService(get()) }
}

private class InMemoryUsuarioRepository : UsuarioRepository {
    private val usuarios = linkedMapOf<String, Usuario>()

    override fun findById(id: String): Usuario? = usuarios[id]

    override fun findByEmail(email: String): Usuario? =
        usuarios.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Usuario>, Long> =
        paginate(
            usuarios.values.filter {
                matchesText(filters["nome"], it.nome) &&
                    matchesText(filters["email"], it.email) &&
                    matchesBoolean(filters["ativo"], it.ativo)
            },
            page,
            limit
        )

    override fun findByInstituicaoId(
        instituicaoId: String,
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Usuario>, Long> =
        paginate(
            usuarios.values.filter {
                it.instituicaoId == instituicaoId &&
                    matchesText(filters["nome"], it.nome) &&
                    matchesText(filters["email"], it.email) &&
                    matchesBoolean(filters["ativo"], it.ativo)
            },
            page,
            limit
        )

    override fun insert(usuario: Usuario): Usuario {
        usuarios[usuario.id!!] = usuario
        return usuario
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = usuarios[id] ?: return false
        usuarios[id] = atual.copy(
            nome = updates["nome"] as? String ?: atual.nome,
            email = updates["email"] as? String ?: atual.email,
            senhaHash = updates["senhaHash"] as? String ?: atual.senhaHash,
            papeis = updates["papeis"] as? Set<Papel> ?: atual.papeis,
            instituicaoId = updates["instituicaoId"] as? String ?: atual.instituicaoId,
            ativo = updates["ativo"] as? Boolean ?: atual.ativo,
            avatar = updates["avatar"] as? String ?: atual.avatar,
            accesstoken = updates.getOrDefault("accesstoken", atual.accesstoken) as String?,
            refreshtoken = updates.getOrDefault("refreshtoken", atual.refreshtoken) as String?,
            tokenUnico = updates.getOrDefault("tokenUnico", atual.tokenUnico) as String?,
            codigoRecuperaSenha = updates.getOrDefault("codigoRecuperaSenha", atual.codigoRecuperaSenha) as String?,
            expCodigoRecuperaSenha = updates.getOrDefault("expCodigoRecuperaSenha", atual.expCodigoRecuperaSenha) as Instant?,
            ultimoLoginEm = updates.getOrDefault("ultimoLoginEm", atual.ultimoLoginEm) as Instant?,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }

    override fun delete(id: String): Boolean = usuarios.remove(id) != null

    override fun findByTokenUnico(token: String): Usuario? =
        usuarios.values.firstOrNull { it.tokenUnico == token }

    override fun findByCodigoRecuperacao(codigo: String): Usuario? =
        usuarios.values.firstOrNull { it.codigoRecuperaSenha == codigo }
}

private class InMemoryInstituicaoRepository : InstituicaoRepository {
    private val instituicoes = linkedMapOf<String, Instituicao>()

    override fun findById(id: String): Instituicao? = instituicoes[id]

    override fun findByNome(nome: String): Instituicao? =
        instituicoes.values.firstOrNull { it.nome.equals(nome, ignoreCase = true) }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Instituicao>, Long> =
        paginate(
            instituicoes.values.filter {
                matchesText(filters["nome"], it.nome) &&
                    matchesBoolean(filters["ativo"], it.ativo) &&
                    (filters["status"] == null || it.status.name == filters["status"].toString())
            },
            page,
            limit
        )

    override fun insert(instituicao: Instituicao): Instituicao {
        instituicoes[instituicao.id!!] = instituicao
        return instituicao
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = instituicoes[id] ?: return false
        instituicoes[id] = atual.copy(
            nome = updates["nome"] as? String ?: atual.nome,
            cnpj = updates["cnpj"] as? String ?: atual.cnpj,
            email = updates["email"] as? String ?: atual.email,
            telefone = updates["telefone"] as? String ?: atual.telefone,
            responsavel = updates["responsavel"] as? String ?: atual.responsavel,
            endereco = updates["endereco"] as? String ?: atual.endereco,
            descricao = updates["descricao"] as? String ?: atual.descricao,
            ativo = updates["ativo"] as? Boolean ?: atual.ativo,
            status = updates["status"] as? StatusInstituicao ?: atual.status,
            contratoUrl = updates["contratoUrl"] as? String ?: atual.contratoUrl,
            motivoRejeicao = updates["motivoRejeicao"] as? String ?: atual.motivoRejeicao,
            aprovadoPor = updates.getOrDefault("aprovadoPor", atual.aprovadoPor) as String?,
            aprovadoEm = updates.getOrDefault("aprovadoEm", atual.aprovadoEm) as Instant?,
            configuracoes = updates["configuracoes"] as? Map<String, JsonElement> ?: atual.configuracoes,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }

    override fun delete(id: String): Boolean = instituicoes.remove(id) != null
}

private class InMemoryFilaRepository : FilaRepository {
    private val filas = linkedMapOf<String, Fila>()

    override fun findById(id: String): Fila? = filas[id]

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Fila>, Long> =
        paginate(
            filas.values.filter {
                (filters["instituicaoId"] == null || it.instituicaoId == filters["instituicaoId"]) &&
                    (filters["tipoAtendimento"] == null || it.tipoAtendimento.name == filters["tipoAtendimento"].toString()) &&
                    matchesBoolean(filters["ativa"], it.ativa)
            },
            page,
            limit
        )

    override fun findByInstituicaoId(instituicaoId: String): List<Fila> =
        filas.values.filter { it.instituicaoId == instituicaoId }

    override fun insert(fila: Fila): Fila {
        filas[fila.id!!] = fila
        return fila
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = filas[id] ?: return false
        filas[id] = atual.copy(
            nome = updates["nome"] as? String ?: atual.nome,
            tipoAtendimento = updates["tipoAtendimento"] as? TipoAtendimento ?: atual.tipoAtendimento,
            ativa = updates["ativa"] as? Boolean ?: atual.ativa,
            prioridadesHabilitadas = updates["prioridadesHabilitadas"] as? Boolean ?: atual.prioridadesHabilitadas,
            fidelidadeHabilitada = updates["fidelidadeHabilitada"] as? Boolean ?: atual.fidelidadeHabilitada,
            tempoMaximoAtendimento = updates["tempoMaximoAtendimento"] as? Int ?: atual.tempoMaximoAtendimento,
            configuracaoQRCode = updates.getOrDefault("configuracaoQRCode", atual.configuracaoQRCode) as ConfiguracaoQRCode?,
            mesas = updates["mesas"] as? List<Mesa> ?: atual.mesas,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }

    override fun delete(id: String): Boolean = filas.remove(id) != null
}

private class InMemorySenhaRepository : SenhaRepository {
    private val senhas = linkedMapOf<String, Senha>()

    override fun findById(id: String): Senha? = senhas[id]

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Senha>, Long> =
        paginate(
            senhas.values.filter {
                (filters["filaId"] == null || it.filaId == filters["filaId"]) &&
                    (filters["instituicaoId"] == null || it.instituicaoId == filters["instituicaoId"]) &&
                    (filters["status"] == null || it.status.name == filters["status"].toString())
            },
            page,
            limit
        )

    override fun findByFilaId(filaId: String, page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Senha>, Long> =
        findAll(page, limit, filters + ("filaId" to filaId))

    override fun findActiveByUsuarioIdAndFilaId(usuarioId: String, filaId: String): Senha? =
        senhas.values.firstOrNull {
            it.usuarioId == usuarioId &&
                it.filaId == filaId &&
                it.status in setOf(StatusSenha.AGUARDANDO, StatusSenha.EM_ATENDIMENTO)
        }

    override fun countByFilaAndStatus(filaId: String, status: StatusSenha): Long =
        senhas.values.count { it.filaId == filaId && it.status == status }.toLong()

    override fun countByFilaAndStatuses(filaId: String, statuses: Set<StatusSenha>): Long =
        senhas.values.count { it.filaId == filaId && it.status in statuses }.toLong()

    override fun findProximaAguardando(filaId: String): Senha? =
        senhas.values
            .filter { it.filaId == filaId && it.status == StatusSenha.AGUARDANDO }
            .minByOrNull { it.posicao }

    override fun countByInstituicaoAndStatus(instituicaoId: String, status: StatusSenha): Long =
        senhas.values.count { it.instituicaoId == instituicaoId && it.status == status }.toLong()

    override fun countCriadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long =
        senhas.values.count { it.instituicaoId == instituicaoId && it.createdAt >= inicio && it.createdAt < fim }.toLong()

    override fun countFinalizadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long =
        senhas.values.count {
            it.instituicaoId == instituicaoId &&
                it.status == StatusSenha.FINALIZADA &&
                it.updatedAt != null &&
                it.updatedAt >= inicio &&
                it.updatedAt < fim
        }.toLong()

    override fun countByFilaAndCreatedBetween(filaId: String, inicio: Instant, fim: Instant): Long =
        senhas.values.count { it.filaId == filaId && it.createdAt >= inicio && it.createdAt < fim }.toLong()

    override fun countByFilaAndStatusAndCreatedBetween(
        filaId: String,
        status: StatusSenha,
        inicio: Instant,
        fim: Instant
    ): Long = senhas.values.count {
        it.filaId == filaId &&
            it.status == status &&
            it.createdAt >= inicio &&
            it.createdAt < fim
    }.toLong()

    override fun insert(senha: Senha): Senha {
        senhas[senha.id!!] = senha
        return senha
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = senhas[id] ?: return false
        senhas[id] = atual.copy(
            usuarioId = updates.getOrDefault("usuarioId", atual.usuarioId) as String?,
            nomeCidadao = updates.getOrDefault("nomeCidadao", atual.nomeCidadao) as String?,
            presencial = updates["presencial"] as? Boolean ?: atual.presencial,
            posicao = updates["posicao"] as? Int ?: atual.posicao,
            status = updates["status"] as? StatusSenha ?: atual.status,
            prioridade = updates.getOrDefault("prioridade", atual.prioridade) as String?,
            mesa = updates.getOrDefault("mesa", atual.mesa) as String?,
            mesaNome = updates.getOrDefault("mesaNome", atual.mesaNome) as String?,
            operadorId = updates.getOrDefault("operadorId", atual.operadorId) as String?,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }
}

private class InMemoryQrCodeRepository : QrCodeRepository {
    private val qrcodes = linkedMapOf<String, QrCode>()

    override fun findById(id: String): QrCode? = qrcodes[id]

    override fun findByCodigo(codigo: String): QrCode? =
        qrcodes.values.firstOrNull { it.codigo == codigo }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<QrCode>, Long> =
        paginate(
            qrcodes.values.filter {
                (filters["filaId"] == null || it.filaId == filters["filaId"]) &&
                    matchesBoolean(filters["ativo"], it.ativo)
            },
            page,
            limit
        )

    override fun insert(qrCode: QrCode): QrCode {
        qrcodes[qrCode.id!!] = qrCode
        return qrCode
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = qrcodes[id] ?: return false
        qrcodes[id] = atual.copy(
            codigo = updates["codigo"] as? String ?: atual.codigo,
            ativo = updates["ativo"] as? Boolean ?: atual.ativo,
            validoAte = updates.getOrDefault("validoAte", atual.validoAte) as Instant,
            toleranciaAte = updates.getOrDefault("toleranciaAte", atual.toleranciaAte) as Instant,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }

    override fun deactivate(id: String): Boolean =
        update(id, mapOf("ativo" to false, "updatedAt" to Instant.now()))

    override fun deactivateAllAtivosByFila(filaId: String): Long {
        val ativos = qrcodes.values.filter { it.filaId == filaId && it.ativo }
        ativos.forEach { qr ->
            qrcodes[qr.id!!] = qr.copy(ativo = false, updatedAt = Instant.now())
        }
        return ativos.size.toLong()
    }
}

private class InMemoryAuditoriaRepository : AuditoriaRepository {
    private val auditorias = linkedMapOf<String, Auditoria>()

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Auditoria>, Long> =
        paginate(
            auditorias.values.filter {
                (filters["instituicaoId"] == null || it.instituicaoId == filters["instituicaoId"]) &&
                    (filters["usuarioId"] == null || it.usuarioId == filters["usuarioId"]) &&
                    (filters["acao"] == null || it.acao.name == filters["acao"].toString()) &&
                    (filters["entidade"] == null || it.entidade == filters["entidade"])
            },
            page,
            limit
        )

    override fun insert(auditoria: Auditoria): Auditoria {
        auditorias[auditoria.id!!] = auditoria
        return auditoria
    }
}

private class InMemoryLandingPageRepository : LandingPageRepository {
    private var landingPage: LandingPage? = null

    override fun findDefault(): LandingPage? = landingPage

    override fun upsertDefault(landingPage: LandingPage): LandingPage {
        val normalized = landingPage.copy(key = LandingPage.DEFAULT_KEY)
        this.landingPage = normalized
        return normalized
    }
}

private class InMemoryExampleRepository : ExampleRepository {
    private val examples = linkedMapOf<String, Example>()

    override fun findById(id: String): Example? = examples[id]

    override fun findByEmail(email: String): Example? =
        examples.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Example>, Long> =
        paginate(
            examples.values.filter {
                matchesText(filters["nome"], it.nome) &&
                    matchesText(filters["email"], it.email) &&
                    matchesBoolean(filters["ativo"], it.ativo)
            },
            page,
            limit
        )

    override fun findByEnum(enumValue: EnumExample, page: Int, limit: Int): Pair<List<Example>, Long> =
        paginate(examples.values.filter { enumValue in it.enumExample }, page, limit)

    override fun insert(example: Example): Example {
        examples[example.id!!] = example
        return example
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val atual = examples[id] ?: return false
        examples[id] = atual.copy(
            nome = updates["nome"] as? String ?: atual.nome,
            email = updates["email"] as? String ?: atual.email,
            enumExample = updates["enumExample"] as? Set<EnumExample> ?: atual.enumExample,
            ativo = updates["ativo"] as? Boolean ?: atual.ativo,
            updatedAt = updates["updatedAt"] as? Instant ?: atual.updatedAt
        )
        return true
    }

    override fun delete(id: String): Boolean = examples.remove(id) != null
}

private fun <T> paginate(items: List<T>, page: Int, limit: Int): Pair<List<T>, Long> {
    val total = items.size.toLong()
    val fromIndex = ((page - 1) * limit).coerceAtMost(items.size)
    val toIndex = (fromIndex + limit).coerceAtMost(items.size)
    return Pair(items.subList(fromIndex, toIndex), total)
}

private fun matchesText(filter: Any?, value: String): Boolean =
    filter == null || value.contains(filter.toString(), ignoreCase = true)

private fun matchesBoolean(filter: Any?, value: Boolean): Boolean =
    filter == null || value == filter
