import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

/**
 * Configuração da conexão MongoDB via KMongo.
 *
 * Fornece acesso tipado às coleções do banco.
 */
class MongoConfig(
    connectionString: String = System.getenv("MONGO_URI") ?: "mongodb+srv://fs_aula:60egYQtIbESPneFx@cluster0.bpx8s93.mongodb.net/Aula",
    databaseName: String = System.getenv("MONGO_DB") ?: "Aula"
) {
    private val client = KMongo.createClient(connectionString)
    val database: MongoDatabase = client.getDatabase(databaseName)

    val usuarios get() = database.getCollection<Usuario>(Constants.COLLECTION_USUARIOS)
    val examples get() = database.getCollection<Example>(Constants.COLLECTION_EXAMPLES)
}

