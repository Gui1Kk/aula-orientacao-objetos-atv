import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.senhaRoutes() {
    val senhaService by inject<SenhaService>()

    authenticate("auth-jwt") {
        route("/senhas") {
            authorize(Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get("/stats") {
                    val instituicaoId = call.currentInstituicaoId()
                        ?: throw ApiException(400, "Usuário sem instituição vinculada")
                    val timezone = call.request.queryParameters["timezone"] ?: Constants.FUSO_HORARIO_PADRAO
                    call.respondSuccess(senhaService.stats(instituicaoId, timezone))
                }
            }

            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get {
                    val pagination = call.parsePagination()
                    val filters = mapOf(
                        "filaId" to call.request.queryParameters["filaId"],
                        "status" to call.request.queryParameters["status"]
                    )
                    call.respondSuccess(senhaService.listar(pagination, filters))
                }
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                call.respondSuccess(senhaService.buscarPorId(id))
            }

            post("/{id}/cancelar") {
                val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                call.respondSuccess(senhaService.cancelar(id, executorId), "Senha cancelada")
            }

            authorize(Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                post("/{id}/chamar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val mesa = call.request.queryParameters["mesa"]
                    val mesaNome = call.request.queryParameters["mesaNome"]
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    call.respondSuccess(senhaService.chamar(id, mesa, mesaNome, executorId), "Senha chamada")
                }
            }

            authorize(Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                post("/{id}/finalizar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    call.respondSuccess(senhaService.finalizar(id, executorId), "Senha finalizada")
                }
            }
        }
    }
}
