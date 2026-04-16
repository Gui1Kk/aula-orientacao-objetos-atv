import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.litote.kmongo.*

/**
 * Implementação KMongo do repositório de Examples.
 */
class ExampleRepositoryImpl(
    private val collection: MongoCollection<Example>
) : ExampleRepository {

    override fun findById(id: String): Example? {
        return collection.findOneById(id)
    }

    override fun findByEmail(email: String): Example? {
        return collection.findOne(Example::email eq email.lowercase())
    }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Example>, Long> {
        val bsonFilters = buildFilters(filters)
        val total = collection.countDocuments(and(bsonFilters))
        val docs = collection.find(and(bsonFilters))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun findByEnum(enumValue: EnumExample, page: Int, limit: Int): Pair<List<Example>, Long> {
        val filter = Example::enumExample contains enumValue
        val total = collection.countDocuments(filter)
        val docs = collection.find(filter)
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun insert(example: Example): Example {
        collection.insertOne(example)
        return example
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val setUpdates = updates.map { (key, value) -> Updates.set(key, value) }
        val result = collection.updateOneById(id, combine(setUpdates))
        return result.modifiedCount > 0
    }

    override fun delete(id: String): Boolean {
        return collection.deleteOneById(id).deletedCount > 0
    }

    private fun buildFilters(filters: Map<String, Any?>): List<org.bson.conversions.Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "nome" -> Example::nome regex Regex(".*$value.*", RegexOption.IGNORE_CASE)
                key == "email" -> Example::email regex Regex(".*$value.*", RegexOption.IGNORE_CASE)
                key == "ativo" -> Example::ativo eq (value as Boolean)
                key == "enumExample" -> Example::enumExample contains EnumExample.valueOf(value as String)
                else -> null
            }
        }
    }
}

