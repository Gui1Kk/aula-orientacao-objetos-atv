import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Rotas de examples: /examples
 *
 * GET    /examples                     - lista paginada (AP, AI)
 * GET    /examples/{id}                - busca por ID (AP, AI, OP)
 * GET    /examples/enum/{enumValue}    - lista por enum (AP, AI, OP)
 * POST   /examples                     - cria novo (AP, AI)
 * PATCH  /examples/{id}                - atualiza (AP, AI)
 * PATCH  /examples/{id}/ativar         - ativa (AP, AI)
 * PATCH  /examples/{id}/desativar      - desativa (AP, AI)
 * DELETE /examples/{id}                - deleta (somente se inativo) (AP)
 */
fun Route.exampleRoutes() {
    val exampleService by inject<ExampleService>()

    authenticate("auth-jwt") {
        route("/examples") {

            // GET /examples
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                get {
                    val pagination = call.parsePagination()
                    val filters = mapOf(
                        "nome" to call.request.queryParameters["nome"],
                        "email" to call.request.queryParameters["email"],
                        "ativo" to call.request.queryParameters["ativo"]?.toBooleanStrictOrNull(),
                        "enumExample" to call.request.queryParameters["enumExample"]
                    )
                    val result = exampleService.listar(pagination, filters)
                    call.respondSuccess(result)
                }
            }

            // GET /examples/enum/{enumValue}
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get("/enum/{enumValue}") {
                    val enumValue = call.parameters["enumValue"]!!
                    val pagination = call.parsePagination()
                    val result = exampleService.buscarPorEnum(enumValue, pagination)
                    call.respondSuccess(result)
                }
            }

            // GET /examples/{id}
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get("/{id}") {
                    val id = call.parameters["id"]!!
                    val example = exampleService.buscarPorId(id)
                    call.respondSuccess(example)
                }
            }

            // POST /examples
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                post {
                    val request = call.receive<CreateExampleRequest>()
                    val criado = exampleService.criar(request)
                    call.respondSuccess(criado, "Example criado com sucesso", HttpStatusCode.Created)
                }
            }

            // PATCH /examples/{id}
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                patch("/{id}") {
                    val id = call.parameters["id"]!!
                    val request = call.receive<UpdateExampleRequest>()
                    val atualizado = exampleService.atualizar(id, request)
                    call.respondSuccess(atualizado, "Example atualizado")
                }
            }

            // PATCH /examples/{id}/ativar
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                patch("/{id}/ativar") {
                    val id = call.parameters["id"]!!
                    val resultado = exampleService.ativar(id)
                    call.respondSuccess(resultado, "Example ativado")
                }
            }

            // PATCH /examples/{id}/desativar
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                patch("/{id}/desativar") {
                    val id = call.parameters["id"]!!
                    val resultado = exampleService.desativar(id)
                    call.respondSuccess(resultado, "Example desativado")
                }
            }

            // DELETE /examples/{id}
            authorize(Papel.ADMIN_PLATAFORMA) {
                delete("/{id}") {
                    val id = call.parameters["id"]!!
                    exampleService.deletar(id)
                    call.respondEmptySuccess("Example removido")
                }
            }
        }
    }
}

