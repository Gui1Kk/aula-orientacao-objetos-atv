import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.time.Instant

class QrCodeRepositoryImpl(
    private val collection: MongoCollection<QrCode>
) : QrCodeRepository {

    override fun findById(id: String): QrCode? = collection.findOneById(id)

    override fun findByCodigo(codigo: String): QrCode? =
        collection.findOne(QrCode::codigo eq codigo)

    override fun findAll(
        page: Int,
        limit: Int,
        filters: Map<String, Any?>
    ): Pair<List<QrCode>, Long> {
        val bsonFilters = buildFilters(filters)
        val filter = if (bsonFilters.isEmpty()) EMPTY_BSON else and(bsonFilters)
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun insert(qrCode: QrCode): QrCode {
        collection.insertOne(qrCode)
        return qrCode
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        if (updates.isEmpty()) return false
        val setUpdates = updates.map { (key, value) -> Updates.set(key, value) }
        val result = collection.updateOneById(id, combine(setUpdates))
        return result.modifiedCount > 0
    }

    override fun deactivate(id: String): Boolean =
        update(id, mapOf("ativo" to false, "updatedAt" to Instant.now()))

    override fun deactivateAllAtivosByFila(filaId: String): Long {
        val result = collection.updateMany(
            and(QrCode::filaId eq filaId, QrCode::ativo eq true),
            combine(
                Updates.set(QrCode::ativo.name, false),
                Updates.set(QrCode::updatedAt.name, Instant.now())
            )
        )
        return result.modifiedCount
    }

    private fun buildFilters(filters: Map<String, Any?>): List<Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "filaId" -> QrCode::filaId eq value.toString()
                key == "ativo" -> QrCode::ativo eq (value as Boolean)
                else -> null
            }
        }
    }

    companion object {
        private val EMPTY_BSON: Bson = org.bson.Document()
    }
}
