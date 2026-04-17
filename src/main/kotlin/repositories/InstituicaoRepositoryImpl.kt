import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*

class InstituicaoRepositoryImpl(
    private val collection: MongoCollection<Instituicao>
) : InstituicaoRepository {

    override fun findById(id: String): Instituicao? = collection.findOneById(id)

    override fun findByNome(nome: String): Instituicao? =
        collection.findOne(Instituicao::nome eq nome.trim())

    override fun findAll(
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<Instituicao>, Long> {
        val bsonFilters = buildFilters(filters)
        val filter = if (bsonFilters.isEmpty()) EMPTY_BSON else and(bsonFilters)
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun insert(instituicao: Instituicao): Instituicao {
        collection.insertOne(instituicao)
        return instituicao
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
                key == "nome" -> Instituicao::nome regex Regex(".*${Regex.escape(value.toString())}.*", RegexOption.IGNORE_CASE)
                key == "ativo" -> Instituicao::ativo eq (value as Boolean)
                key == "status" -> Instituicao::status eq when (value) {
                    is StatusInstituicao -> value
                    else -> StatusInstituicao.valueOf(value.toString())
                }
                else -> null
            }
        }
    }

    companion object {
        private val EMPTY_BSON: Bson = org.bson.Document()
    }
}
