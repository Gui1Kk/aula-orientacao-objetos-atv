/**
 * Interface do repositório de Usuários.
 * Contrato para operações CRUD e consultas específicas.
 * cria obrigações de implementação, garantindo consistência e flexibilidade.
 */
interface UsuarioRepository {
    fun findById(id: String): Usuario?
    fun findByEmail(email: String): Usuario?
    fun findAll(page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Usuario>, Long>
    fun findByInstituicaoId(instituicaoId: String, page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Usuario>, Long>
    fun insert(usuario: Usuario): Usuario
    fun update(id: String, updates: Map<String, Any?>): Boolean
    fun delete(id: String): Boolean
    fun findByTokenUnico(token: String): Usuario?
    fun findByCodigoRecuperacao(codigo: String): Usuario?
}

