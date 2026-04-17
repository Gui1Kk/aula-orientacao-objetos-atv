import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*

class FilaRepositoryImpl(
    private val collection: MongoCollection<Fila>
) : FilaRepository {

    override fun findById(id: String): Fila? = collection.findOneById(id)

    override fun findAll(
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Fila>, Long> {
        val bsonFilters = buildFilters(filters)
        val filter = if (bsonFilters.isEmpty()) EMPTY_BSON else and(bsonFilters)
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun findByInstituicaoId(instituicaoId: String): List<Fila> =
        collection.find(Fila::instituicaoId eq instituicaoId).toList()

    override fun insert(fila: Fila): Fila {
        collection.insertOne(fila)
        return fila
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        if (updates.isEmpty()) return false
        val setUpdates = updates.map { (key, value) -> Updates.set(key, value) }
        val result = collection.updateOneById(id, combine(setUpdates))
        return result.modifiedCount > 0
    }

    override fun delete(id: String): Boolean =
        collection.deleteOneById(id).deletedCount > 0

    private fun buildFilters(filters: Map<String, Any?>): List<Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "instituicaoId" -> Fila::instituicaoId eq value.toString()
                key == "nome" -> Fila::nome regex Regex(".*${Regex.escape(value.toString())}.*", RegexOption.IGNORE_CASE)
                key == "ativa" -> Fila::ativa eq (value as Boolean)
                key == "tipoAtendimento" -> Fila::tipoAtendimento eq when (value) {
                    is TipoAtendimento -> value
                    else -> TipoAtendimento.valueOf(value.toString().uppercase())
                }
                else -> null
            }
        }
    }

    companion object {
        private val EMPTY_BSON: Bson = org.bson.Document()
    }
}
