interface AuditoriaRepository {
    fun insert(auditoria: Auditoria): Auditoria
    fun findAll(page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Auditoria>, Long>
}
