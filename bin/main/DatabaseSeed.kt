import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.Date

/**
 * Seed do banco de dados — popula as coleções com dados de exemplo.
 *
 * Usa o driver MongoDB Java puro (Document) para evitar conflitos com
 * kmongo-serialization que exige @Serializable nos modelos.
 *
 * Uso:  ./gradlew seed
 */
fun main() {
    val mongoUri = System.getenv("MONGO_URI")
        ?: "mongodb+srv://fs_aula:60egYQtIbESPneFx@cluster0.bpx8s93.mongodb.net/aula"
    val dbName = System.getenv("MONGO_DB") ?: "Aula"

    println("🌱 Conectando ao MongoDB: $dbName ...")
    val settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(mongoUri))
        .build()
    val client = MongoClients.create(settings)
    val db: MongoDatabase = client.getDatabase(dbName)

    val collections = listOf("usuarios")
    collections.forEach { col ->
        db.getCollection(col).drop()
        println("  🗑  Coleção '$col' removida.")
    }

    val now = Date.from(Instant.now())
    fun hash(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt(12))

    // ════════════════════════════════════════════
    // 1. USUÁRIOS
    // ════════════════════════════════════════════
    val usuarios = db.getCollection("usuarios")
    usuarios.insertMany(listOf(
        Document(mapOf(
            "_id" to "usr-admin-plataforma",
            "nome" to "Admin Plataforma",
            "email" to "admin@example.com",
            "senhaHash" to hash("Admin@123"),
            "papeis" to listOf("ADMIN_PLATAFORMA"),
            "ativo" to true,
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        )),
        Document(mapOf(
            "_id" to "usr-admin-instituicao",
            "nome" to "Admin Instituição",
            "email" to "admin.instituicao@example.com",
            "senhaHash" to hash("Admin@123"),
            "papeis" to listOf("ADMIN_INSTITUICAO"),
            "instituicaoId" to "inst-exemplo",
            "ativo" to true,
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        )),
        Document(mapOf(
            "_id" to "usr-operador",
            "nome" to "Operador",
            "email" to "operador@example.com",
            "senhaHash" to hash("Admin@123"),
            "papeis" to listOf("OPERADOR"),
            "instituicaoId" to "inst-exemplo",
            "ativo" to true,
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        )),
        Document(mapOf(
            "_id" to "usr-cidadao",
            "nome" to "Cidadão",
            "email" to "cidadao@example.com",
            "senhaHash" to hash("Admin@123"),
            "papeis" to listOf("USUARIO_FINAL"),
            "ativo" to true,
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        ))
    ))
    println("  ✅ ${usuarios.countDocuments()} usuários inseridos.")

    // ════════════════════════════════════════════
    // Resumo
    // ════════════════════════════════════════════
    println()
    println("═══════════════════════════════════════")
    println("🌱 Seed concluído com sucesso!")
    println("═══════════════════════════════════════")
    println("  Usuários ........... ${usuarios.countDocuments()}")
    println("═══════════════════════════════════════")
    println()
    println("📋 Credenciais de acesso (senha em texto puro):")
    println("  ADMIN_PLATAFORMA:  admin@example.com              / Admin@123")
    println("  ADMIN_INSTITUICAO: admin.instituicao@example.com  / Admin@123")
    println("  OPERADOR:          operador@example.com           / Admin@123")
    println("  USUARIO_FINAL:     cidadao@example.com            / Admin@123")
    println()

    client.close()
}
