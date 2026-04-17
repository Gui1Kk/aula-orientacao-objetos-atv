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

fun Route.instituicaoRoutes() {
    val instituicaoService by inject<InstituicaoService>()

    authenticate("auth-jwt") {
        route("/instituicoes") {
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                get {
                    val pagination = call.parsePagination()
                    val filters = mapOf(
                        "nome" to call.request.queryParameters["nome"],
                        "ativo" to call.request.queryParameters["ativo"]?.toBooleanStrictOrNull(),
                        "status" to call.request.queryParameters["status"]
                    )
                    call.respondSuccess(instituicaoService.listar(pagination, filters))
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA) {
                post {
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<CreateInstituicaoRequest>()
                    val criada = instituicaoService.criar(request, executorId)
                    call.respondSuccess(criada, "Instituição criada", HttpStatusCode.Created)
                }
            }

            post("/solicitar") {
                val solicitanteId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                val request = call.receive<SolicitarInstituicaoRequest>()
                val criada = instituicaoService.solicitar(request, solicitanteId)
                call.respondSuccess(criada, "Instituição solicitada", HttpStatusCode.Created)
            }

            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                get("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    call.respondSuccess(instituicaoService.buscarPorId(id))
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                patch("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<UpdateInstituicaoRequest>()
                    val atualizada = instituicaoService.atualizar(id, request, executorId)
                    call.respondSuccess(atualizada, "Instituição atualizada")
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA) {
                patch("/{id}/aprovar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val aprovada = instituicaoService.aprovar(id, executorId)
                    call.respondSuccess(aprovada, "Instituição aprovada")
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA) {
                patch("/{id}/rejeitar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<RejeitarInstituicaoRequest>()
                    val rejeitada = instituicaoService.rejeitar(id, request, executorId)
                    call.respondSuccess(rejeitada, "Instituição rejeitada")
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA) {
                patch("/{id}/reconsiderar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val reconsiderada = instituicaoService.reconsiderar(id, executorId)
                    call.respondSuccess(reconsiderada, "Instituição reconsiderada")
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA) {
                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    instituicaoService.deletar(id, executorId)
                    call.respondEmptySuccess("Instituição removida")
                }
            }
        }
    }
}
