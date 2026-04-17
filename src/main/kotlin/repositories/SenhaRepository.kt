import java.time.Instant

interface SenhaRepository {
    fun findById(id: String): Senha?
    fun findAll(page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Senha>, Long>
    fun findByFilaId(filaId: String, page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Senha>, Long>
    fun insert(senha: Senha): Senha
    fun update(id: String, updates: Map<String, Any?>): Boolean
    fun countByFilaAndStatus(filaId: String, status: StatusSenha): Long
    fun countByFilaAndStatuses(filaId: String, statuses: Set<StatusSenha>): Long
    fun findActiveByUsuarioIdAndFilaId(usuarioId: String, filaId: String): Senha?
    fun findProximaAguardando(filaId: String): Senha?
    fun countByInstituicaoAndStatus(instituicaoId: String, status: StatusSenha): Long
    fun countCriadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long
    fun countFinalizadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long
    fun countByFilaAndCreatedBetween(filaId: String, inicio: Instant, fim: Instant): Long
    fun countByFilaAndStatusAndCreatedBetween(filaId: String, status: StatusSenha, inicio: Instant, fim: Instant): Long

    fun findActiveByUsuarioAndFila(usuarioId: String, filaId: String): Senha? =
        findActiveByUsuarioIdAndFilaId(usuarioId, filaId)

    fun countByFilaIdAndStatus(filaId: String, status: StatusSenha): Long =
        countByFilaAndStatus(filaId, status)

    fun countByFilaIdAndStatuses(filaId: String, statuses: Set<StatusSenha>): Long =
        countByFilaAndStatuses(filaId, statuses)
}
