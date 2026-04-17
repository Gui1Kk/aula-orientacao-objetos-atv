import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.filaRoutes() {
    val filaService by inject<FilaService>()
    val senhaService by inject<SenhaService>()

    authenticate("auth-jwt") {
        route("/filas") {
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get {
                    val pagination = call.parsePagination()
                    val filters = mapOf(
                        "instituicaoId" to call.request.queryParameters["instituicaoId"],
                        "tipoAtendimento" to call.request.queryParameters["tipoAtendimento"],
                        "ativa" to call.request.queryParameters["ativa"]?.toBooleanStrictOrNull()
                    )
                    call.respondSuccess(filaService.listar(pagination, filters))
                }
            }

            authorize(Papel.ADMIN_INSTITUICAO) {
                post {
                    val instituicaoId = call.currentInstituicaoId()
                        ?: throw ApiException(400, "Usuário sem instituição vinculada")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<CreateFilaRequest>()
                    val criada = filaService.criar(request, instituicaoId, executorId)
                    call.respondSuccess(criada, "Fila criada", HttpStatusCode.Created)
                }
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                call.respondSuccess(filaService.buscarPorId(id))
            }

            authorize(Papel.ADMIN_INSTITUICAO) {
                patch("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<UpdateFilaRequest>()
                    val atualizada = filaService.atualizar(id, request, executorId)
                    call.respondSuccess(atualizada, "Fila atualizada")
                }
            }

            authorize(Papel.ADMIN_INSTITUICAO) {
                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    filaService.deletar(id, executorId)
                    call.respondEmptySuccess("Fila removida")
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get("/{id}/contagem") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    call.respondSuccess(filaService.contagemSenhas(id))
                }
            }
        }

        route("/filas/{filaId}/senhas") {
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get {
                    val filaId = call.parameters["filaId"] ?: throw ApiException(400, "ID obrigatório")
                    val pagination = call.parsePagination()
                    val filters = mapOf("status" to call.request.queryParameters["status"])
                    call.respondSuccess(senhaService.listarPorFila(filaId, pagination, filters))
                }
            }

            authorize(Papel.USUARIO_FINAL) {
                post {
                    val filaId = call.parameters["filaId"] ?: throw ApiException(400, "ID obrigatório")
                    val usuarioId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<CreateSenhaRequest>()
                    val criada = senhaService.criar(filaId, usuarioId, request)
                    call.respondSuccess(criada, "Senha criada", HttpStatusCode.Created)
                }
            }

            authorize(Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                post("/presencial") {
                    val filaId = call.parameters["filaId"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<CreateSenhaPresencialRequest>()
                    val criada = senhaService.criarPresencial(filaId, request, executorId)
                    call.respondSuccess(criada, "Senha presencial criada", HttpStatusCode.Created)
                }
            }
        }
    }
}
