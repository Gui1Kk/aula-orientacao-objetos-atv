import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.github.serpro69.kfaker.Faker
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Seed do banco de dados - popula as colecoes com dados de exemplo.
 *
 * Usa o driver MongoDB Java puro (Document) para evitar conflitos com
 * kmongo-serialization que exige @Serializable nos modelos.
 *
 * Usa Kotlin-Faker para gerar dados ficticios de forma recursiva.
 *
 * Uso:  ./gradlew seed
 */

// Quantidade de registros fake a gerar
const val FAKE_USUARIOS_COUNT = 20
const val FAKE_EXAMPLES_COUNT = 15

fun main() {
    val mongoUri = System.getenv("MONGO_URI")
        ?: "mongodb+srv://fs_aula:60egYQtIbESPneFx@cluster0.bpx8s93.mongodb.net/aula"
    val dbName = System.getenv("MONGO_DB") ?: "Aula"

    val faker = Faker()

    println("Conectando ao MongoDB: $dbName ...")
    val settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(mongoUri))
        .build()
    val client = MongoClients.create(settings)
    val db: MongoDatabase = client.getDatabase(dbName)

    val collections = listOf("usuarios", "examples")
    collections.forEach { col ->
        db.getCollection(col).drop()
        println("  Colecao '$col' removida.")
    }

    val now = Date.from(Instant.now())
    fun hash(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt(12))

    val papeis = listOf("ADMIN_PLATAFORMA", "ADMIN_INSTITUICAO", "OPERADOR", "USUARIO_FINAL")
    val enumExamples = listOf("EXAMPLE_ONE", "EXAMPLE_TWO", "EXAMPLE_THREE", "EXAMPLE_FOUR")

    // 1. USUARIOS - registros fixos (credenciais conhecidas)
    val usuarios = db.getCollection("usuarios")
    val fixedUsuarios = listOf(
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
            "nome" to "Admin Instituicao",
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
            "nome" to "Cidadao",
            "email" to "cidadao@example.com",
            "senhaHash" to hash("Admin@123"),
            "papeis" to listOf("USUARIO_FINAL"),
            "ativo" to true,
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        ))
    )

    // 1b. USUARIOS - registros gerados com Faker (recursivo)
    val fakeUsuarios = (1..FAKE_USUARIOS_COUNT).map { i ->
        val nome = faker.name.name()
        val email = faker.internet.email()
        val papel = papeis[i % papeis.size]
        val instId = if (papel in listOf("ADMIN_INSTITUICAO", "OPERADOR"))
            "inst-${faker.lorem.words()}" else null

        val doc = mutableMapOf<String, Any?>(
            "_id" to "usr-fake-$i-${UUID.randomUUID().toString().take(8)}",
            "nome" to nome,
            "email" to email,
            "senhaHash" to hash("Fake@${i}23"),
            "papeis" to listOf(papel),
            "ativo" to faker.random.nextBoolean(),
            "fusoHorario" to "America/Manaus",
            "createdAt" to now
        )
        if (instId != null) doc["instituicaoId"] = instId
        Document(doc)
    }

    usuarios.insertMany(fixedUsuarios + fakeUsuarios)
    println("  ${usuarios.countDocuments()} usuarios inseridos (${fixedUsuarios.size} fixos + ${fakeUsuarios.size} fake).")

    // 2. EXAMPLES - registros gerados com Faker (recursivo)
    val examples = db.getCollection("examples")
    val fakeExamples = (1..FAKE_EXAMPLES_COUNT).map { i ->
        val nome = faker.company.name()
        val email = faker.internet.email()
        // Seleciona de 1 a 3 enums aleatorios
        val selectedEnums = enumExamples.shuffled().take(faker.random.nextInt(1, enumExamples.size))

        Document(mapOf(
            "_id" to "example-$i-${UUID.randomUUID().toString().take(8)}",
            "nome" to nome,
            "email" to email,
            "enumExample" to selectedEnums,
            "ativo" to faker.random.nextBoolean(),
            "createdAt" to now
        ))
    }

    examples.insertMany(fakeExamples)
    println("  ${examples.countDocuments()} examples inseridos (${fakeExamples.size} fake).")

    // Resumo
    println()
    println("---------------------------------------")
    println("Seed concluido com sucesso!")
    println("---------------------------------------")
    println("  Usuarios ........... ${usuarios.countDocuments()}")
    println("  Examples ........... ${examples.countDocuments()}")
    println("---------------------------------------")
    println()
    println("Credenciais de acesso (senha em texto puro):")
    println("  ADMIN_PLATAFORMA:  admin@example.com              / Admin@123")
    println("  ADMIN_INSTITUICAO: admin.instituicao@example.com  / Admin@123")
    println("  OPERADOR:          operador@example.com           / Admin@123")
    println("  USUARIO_FINAL:     cidadao@example.com            / Admin@123")
    println("  FAKE USERS:        <faker-email>                  / Fake@<N>23")
    println()

    client.close()
}
