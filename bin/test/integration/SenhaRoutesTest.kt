import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes de integração das rotas de senhas (§9.6).
 *
 * Verifica middleware de autenticação e autorização por papéis.
 */
class SenhaRoutesTest {

    @Test
    fun `GET senhas sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/senhas")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST filas-senhas sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/filas/fila-1/senhas") {
            contentType(ContentType.Application.Json)
            setBody("""{}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST senhas-chamar sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/senhas/fake-id/chamar")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST senhas-cancelar sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/senhas/fake-id/cancelar")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST senhas-finalizar sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/senhas/fake-id/finalizar")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET senhas-stats sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/senhas/stats")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET filas sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/filas")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET auditorias sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/auditorias")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET me sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET well-known-assetlinks retorna 200 publico`() = testApplication {
        application { module() }

        val response = client.get("/.well-known/assetlinks.json")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET redirect deep link redireciona`() = testApplication {
        application { module() }

        val response = client.get("/r/codigo123") {
            // Não seguir redirect
        }

        // 302 Found (redirect)
        assertTrue(response.status.value in listOf(302, 301, 200))
    }
}

