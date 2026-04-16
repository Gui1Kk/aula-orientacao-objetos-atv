import java.time.Instant
import java.util.UUID

/**
 * Service de Examples — regras de negócio para o modelo Example.
 *
 * Regras implementadas:
 * - Email deve ser único por example
 * - Enums fornecidos devem ser valores válidos de EnumExample
 * - Não é possível deletar um example ativo
 * - Filtro por enum disponível via buscarPorEnum
 */
class ExampleService(
    private val exampleRepository: ExampleRepository
) {

    fun listar(pagination: PaginationParams, filters: Map<String, Any?>): PaginatedResponse<Example> {
        val (docs, total) = exampleRepository.findAll(pagination.page, pagination.limit, filters)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun buscarPorId(id: String): Example {
        return exampleRepository.findById(id)
            ?: throw ApiException(404, "Example não encontrado")
    }

    fun buscarPorEnum(enumValue: String, pagination: PaginationParams): PaginatedResponse<Example> {
        val enum = parseEnum(enumValue)
        val (docs, total) = exampleRepository.findByEnum(enum, pagination.page, pagination.limit)
        return buildPaginatedResponse(docs, total, pagination)
    }

    fun criar(request: CreateExampleRequest): Example {
        // Regra: email único
        if (exampleRepository.findByEmail(request.email) != null)
            throw ApiException(400, "E-mail já cadastrado para outro example")

        // Regra: todos os enums devem ser válidos
        val enums = parseEnums(request.enumExample)

        val example = Example(
            id = UUID.randomUUID().toString(),
            nome = request.nome.trim(),
            email = request.email.lowercase().trim(),
            enumExample = enums,
            ativo = true,
            createdAt = Instant.now()
        )

        return exampleRepository.insert(example)
    }

    fun atualizar(id: String, request: UpdateExampleRequest): Example {
        buscarPorId(id) // verifica existência

        val updates = mutableMapOf<String, Any?>()
        request.nome?.let { updates["nome"] = it.trim() }
        request.email?.let {
            val novoEmail = it.lowercase().trim()
            val existente = exampleRepository.findByEmail(novoEmail)
            // Regra: novo email não pode pertencer a outro example
            if (existente != null && existente.id != id)
                throw ApiException(400, "E-mail já em uso por outro example")
            updates["email"] = novoEmail
        }
        request.enumExample?.let { updates["enumExample"] = parseEnums(it) }
        request.ativo?.let { updates["ativo"] = it }
        updates["updatedAt"] = Instant.now()

        exampleRepository.update(id, updates)
        return buscarPorId(id)
    }

    fun ativar(id: String): Example {
        buscarPorId(id) // verifica existência
        exampleRepository.update(id, mapOf("ativo" to true, "updatedAt" to Instant.now()))
        return buscarPorId(id)
    }

    fun desativar(id: String): Example {
        buscarPorId(id) // verifica existência
        exampleRepository.update(id, mapOf("ativo" to false, "updatedAt" to Instant.now()))
        return buscarPorId(id)
    }

    fun deletar(id: String) {
        val example = buscarPorId(id)
        // Regra: não é permitido deletar example ativo
        if (example.ativo)
            throw ApiException(400, "Desative o example antes de deletá-lo")
        exampleRepository.delete(id)
    }

    // Helpers privados para validação e parsing do enum ou enum list fornecidos nas requisições

    private fun parseEnum(value: String): EnumExample {
        return try {
            EnumExample.valueOf(value.uppercase())
        } catch (_: Exception) {
            throw ApiException(400, "Enum inválido: $value. Valores aceitos: ${EnumExample.entries.joinToString()}")
        }
    }

    private fun parseEnums(values: List<String>): Set<EnumExample> {
        if (values.isEmpty())
            throw ApiException(400, "Ao menos um enumExample deve ser informado")
        return values.map { parseEnum(it) }.toSet()
    }
}

