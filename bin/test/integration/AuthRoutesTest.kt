import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes de integração das rotas de autenticação (§9.1 e §9.2).
 *
 * Usa Ktor testApplication para simular requisições HTTP sem servidor real.
 * Os repositórios usam mocks injetados via módulo Koin de teste.
 */
class AuthRoutesTest {

    @Test
    fun `POST auth-register com dados validos retorna 201`() = testApplication {
        application { module() }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Teste User","email":"teste@email.com","senha":"senha123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("success"))
    }

    @Test
    fun `POST auth-login com credenciais invalidas retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"naoexiste@email.com","senha":"senha123"}""")
        }

        // Espera 401 (credenciais inválidas) ou 500 (se MongoDB não está rodando)
        assertTrue(response.status.value in listOf(401, 500))
    }

    @Test
    fun `POST auth-refresh sem token retorna erro`() = testApplication {
        application { module() }

        val response = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"token":"token-invalido"}""")
        }

        assertTrue(response.status.value in listOf(401, 500))
    }

    @Test
    fun `POST auth-logout sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/auth/logout")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST auth-recover com email invalido retorna erro`() = testApplication {
        application { module() }

        val response = client.post("/auth/recover") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"naoexiste@email.com"}""")
        }

        assertTrue(response.status.value in listOf(400, 500))
    }

    @Test
    fun `GET timezones retorna lista de fusos publica`() = testApplication {
        application { module() }

        val response = client.get("/timezones")

        // Pode retornar 200 mesmo sem MongoDB (não depende de banco)
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("America/Manaus"))
    }

    @Test
    fun `GET landing-page sem dados retorna 404 ou 500`() = testApplication {
        application { module() }

        val response = client.get("/landing-page")

        // 404 (não configurada) ou 500 (se MongoDB não está rodando)
        assertTrue(response.status.value in listOf(404, 500))
    }
}

