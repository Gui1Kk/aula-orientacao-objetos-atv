import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class MongoConfig(
    connectionString: String = System.getenv("MONGO_URI")
        ?: "mongodb+srv://fs_aula:60egYQtIbESPneFx@cluster0.bpx8s93.mongodb.net/Aula",
    databaseName: String = System.getenv("MONGO_DB") ?: "Aula"
) {
    private val client = KMongo.createClient(connectionString)
    val database: MongoDatabase = client.getDatabase(databaseName)

    val usuarios get() = database.getCollection<Usuario>(Constants.COLLECTION_USUARIOS)
    val instituicoes get() = database.getCollection<Instituicao>(Constants.COLLECTION_INSTITUICOES)
    val filas get() = database.getCollection<Fila>(Constants.COLLECTION_FILAS)
    val senhas get() = database.getCollection<Senha>(Constants.COLLECTION_SENHAS)
    val qrcodes get() = database.getCollection<QrCode>(Constants.COLLECTION_QRCODES)
    val auditorias get() = database.getCollection<Auditoria>(Constants.COLLECTION_AUDITORIAS)
    val landingPages get() = database.getCollection<LandingPage>(Constants.COLLECTION_LANDING_PAGE)
    val examples get() = database.getCollection<Example>(Constants.COLLECTION_EXAMPLES)
}
