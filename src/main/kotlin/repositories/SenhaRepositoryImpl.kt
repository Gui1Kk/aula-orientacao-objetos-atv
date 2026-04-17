import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Sorts.ascending
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.time.Instant

class SenhaRepositoryImpl(
    private val collection: MongoCollection<Senha>
) : SenhaRepository {

    override fun findById(id: String): Senha? = collection.findOneById(id)

    override fun findAll(
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Senha>, Long> {
        val bsonFilters = buildFilters(filters)
        val filter = if (bsonFilters.isEmpty()) EMPTY_BSON else and(bsonFilters)
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun findByFilaId(
        filaId: String,
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Senha>, Long> {
        val allFilters = filters + ("filaId" to filaId)
        return findAll(page, limit, allFilters)
    }

    override fun insert(senha: Senha): Senha {
        collection.insertOne(senha)
        return senha
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        if (updates.isEmpty()) return false
        val setUpdates = updates.map { (key, value) -> Updates.set(key, value) }
        val result = collection.updateOneById(id, combine(setUpdates))
        return result.modifiedCount > 0
    }

    override fun countByFilaAndStatus(filaId: String, status: StatusSenha): Long =
        collection.countDocuments(and(Senha::filaId eq filaId, Senha::status eq status))

    override fun countByFilaAndStatuses(filaId: String, statuses: Set<StatusSenha>): Long =
        collection.countDocuments(and(Senha::filaId eq filaId, Senha::status `in` statuses))

    override fun findActiveByUsuarioIdAndFilaId(usuarioId: String, filaId: String): Senha? =
        collection.findOne(
            and(
                Senha::usuarioId eq usuarioId,
                Senha::filaId eq filaId,
                Senha::status `in` setOf(StatusSenha.AGUARDANDO, StatusSenha.EM_ATENDIMENTO)
            )
        )

    override fun findProximaAguardando(filaId: String): Senha? =
        collection.find(
            and(
                Senha::filaId eq filaId,
                Senha::status eq StatusSenha.AGUARDANDO
            )
        ).sort(ascending(Senha::posicao.name)).first()

    override fun countByInstituicaoAndStatus(instituicaoId: String, status: StatusSenha): Long =
        collection.countDocuments(and(Senha::instituicaoId eq instituicaoId, Senha::status eq status))

    override fun countCriadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long =
        collection.countDocuments(
            and(
                Senha::instituicaoId eq instituicaoId,
                Senha::createdAt gte inicio,
                Senha::createdAt lt fim
            )
        )

    override fun countFinalizadasEntre(instituicaoId: String, inicio: Instant, fim: Instant): Long =
        collection.countDocuments(
            and(
                Senha::instituicaoId eq instituicaoId,
                Senha::status eq StatusSenha.FINALIZADA,
                Senha::updatedAt gte inicio,
                Senha::updatedAt lt fim
            )
        )

    override fun countByFilaAndCreatedBetween(filaId: String, inicio: Instant, fim: Instant): Long =
        collection.countDocuments(
            and(
                Senha::filaId eq filaId,
                Senha::createdAt gte inicio,
                Senha::createdAt lt fim
            )
        )

    override fun countByFilaAndStatusAndCreatedBetween(
        filaId: String,
        status: StatusSenha,
        inicio: Instant,
        fim: Instant
    ): Long = collection.countDocuments(
        and(
            Senha::filaId eq filaId,
            Senha::status eq status,
            Senha::createdAt gte inicio,
            Senha::createdAt lt fim
        )
    )

    private fun buildFilters(filters: Map<String, Any?>): List<Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "filaId" -> Senha::filaId eq value.toString()
                key == "instituicaoId" -> Senha::instituicaoId eq value.toString()
                key == "status" -> Senha::status eq when (value) {
                    is StatusSenha -> value
                    else -> StatusSenha.valueOf(value.toString().uppercase())
                }
                key == "usuarioId" -> Senha::usuarioId eq value.toString()
                else -> null
            }
        }
    }

    companion object {
        private val EMPTY_BSON: Bson = org.bson.Document()
    }
}
