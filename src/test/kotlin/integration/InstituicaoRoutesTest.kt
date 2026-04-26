import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes de integração das rotas de instituições (§9.3).
 *
 * Verifica middleware de autenticação e autorização por papéis.
 */
class InstituicaoRoutesTest {

    @Test
    fun `GET instituicoes sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/instituicoes")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST instituicoes sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/instituicoes") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Nova Inst"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST instituicoes-solicitar sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/instituicoes/solicitar") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Solicitação"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET instituicoes com token invalido retorna 401`() = testApplication {
        application { module() }

        val response = client.get("/instituicoes") {
            header(HttpHeaders.Authorization, "Bearer token-invalido")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PATCH instituicoes-aprovar sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.patch("/instituicoes/fake-id/aprovar")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST instituicoes-contrato sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.post("/instituicoes/fake-id/contrato")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE instituicoes sem autenticacao retorna 401`() = testApplication {
        application { module() }

        val response = client.delete("/instituicoes/fake-id")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}

