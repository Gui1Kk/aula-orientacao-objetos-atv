/**
 * Interface do repositório de Examples.
 * Contrato para operações CRUD e consultas específicas.
 */
interface ExampleRepository {
    fun findById(id: String): Example?
    fun findByEmail(email: String): Example?
    fun findAll(page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<Example>, Long>
    fun findByEnum(enumValue: EnumExample, page: Int, limit: Int): Pair<List<Example>, Long>
    fun insert(example: Example): Example
    fun update(id: String, updates: Map<String, Any?>): Boolean
    fun delete(id: String): Boolean
}

