import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Updates
import org.litote.kmongo.*

/**
 * Implementação KMongo do repositório de Usuários.
 */
class UsuarioRepositoryImpl(
    private val collection: MongoCollection<Usuario>
) : UsuarioRepository {

    override fun findById(id: String): Usuario? {
        return collection.findOneById(id)
    }

    override fun findByEmail(email: String): Usuario? {
        return collection.findOne(Usuario::email eq email.lowercase())
    }

    override fun findAll(page: Int, limit: Int, filters: Map<String, Any?>): Pair<List<Usuario>, Long> {
        val bsonFilters = buildFilters(filters)
        val total = collection.countDocuments(and(bsonFilters))
        val docs = collection.find(and(bsonFilters))
            .skip((page - 1) * limit)
            .limit(limit)
            .toList()
        return Pair(docs, total)
    }

    override fun findByInstituicaoId(
        instituicaoId: String, page: Int, limit: Int, filters: Map<String, Any?>
    ): Pair<List<Usuario>, Long> {
        val allFilters = filters + ("instituicaoId" to instituicaoId)
        return findAll(page, limit, allFilters)
    }

    override fun insert(usuario: Usuario): Usuario {
        collection.insertOne(usuario)
        return usuario
    }

    override fun update(id: String, updates: Map<String, Any?>): Boolean {
        val setUpdates = updates.map { (key, value) -> Updates.set(key, value) }
        val result = collection.updateOneById(id, combine(setUpdates))
        return result.modifiedCount > 0
    }

    override fun delete(id: String): Boolean {
        return collection.deleteOneById(id).deletedCount > 0
    }

    override fun findByTokenUnico(token: String): Usuario? {
        return collection.findOne(Usuario::tokenUnico eq token)
    }

    override fun findByCodigoRecuperacao(codigo: String): Usuario? {
        return collection.findOne(Usuario::codigoRecuperaSenha eq codigo)
    }

    private fun buildFilters(filters: Map<String, Any?>): List<org.bson.conversions.Bson> {
        return filters.mapNotNull { (key, value) ->
            when {
                value == null -> null
                key == "nome" -> Usuario::nome regex Regex(".*$value.*", RegexOption.IGNORE_CASE)
                key == "email" -> Usuario::email regex Regex(".*$value.*", RegexOption.IGNORE_CASE)
                key == "ativo" -> Usuario::ativo eq (value as Boolean)
                key == "instituicaoId" -> Usuario::instituicaoId eq (value as String)
                else -> null
            }
        }
    }

}

