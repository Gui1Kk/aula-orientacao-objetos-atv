import com.mongodb.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.*

class AuditoriaRepositoryImpl(
    private val collection: MongoCollection<Auditoria>
) : AuditoriaRepository {

    override fun insert(auditoria: Auditoria): Auditoria {
        collection.insertOne(auditoria)
        return auditoria
    }

    override fun findAll(
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Auditoria>, Long> {
        val bsonFilters = buildFilters(filters)
        val filter = if (bsonFilters.isEmpty()) EMPTY_BSON else and(bsonFilters)
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    private fun buildFilters(filters: Map<String, Any?>): List<Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "instituicaoId" -> Auditoria::instituicaoId eq value.toString()
                key == "usuarioId" -> Auditoria::usuarioId eq value.toString()
                key == "acao" -> Auditoria::acao eq when (value) {
                    is AcaoAuditoria -> value
                    else -> AcaoAuditoria.valueOf(value.toString().uppercase())
                }
                key == "entidade" -> Auditoria::entidade regex Regex(".*${Regex.escape(value.toString())}.*", RegexOption.IGNORE_CASE)
                else -> null
            }
        }
    }

    companion object {
        private val EMPTY_BSON: Bson = org.bson.Document()
    }
}
