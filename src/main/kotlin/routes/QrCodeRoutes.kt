import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.qrCodeRoutes() {
    val qrCodeService by inject<QrCodeService>()

    authenticate("auth-jwt") {
        route("/qrcodes") {
            get("/codigo/{codigo}") {
                val codigo = call.parameters["codigo"] ?: throw ApiException(400, "Código obrigatório")
                call.respondSuccess(qrCodeService.buscarPorCodigo(codigo))
            }

            authorize(Papel.ADMIN_INSTITUICAO, Papel.OPERADOR) {
                get {
                    val pagination = call.parsePagination()
                    val filters = mapOf(
                        "filaId" to call.request.queryParameters["filaId"],
                        "ativo" to call.request.queryParameters["ativo"]?.toBooleanStrictOrNull()
                    )
                    call.respondSuccess(qrCodeService.listar(pagination, filters))
                }

                get("/{id}") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    call.respondSuccess(qrCodeService.buscarPorId(id))
                }
            }

            authorize(Papel.ADMIN_INSTITUICAO) {
                post {
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<CreateQrCodeRequest>()
                    val criado = qrCodeService.criar(request, executorId)
                    call.respondSuccess(criado, "QR Code criado", HttpStatusCode.Created)
                }

                patch("/{id}/desativar") {
                    val id = call.parameters["id"] ?: throw ApiException(400, "ID obrigatório")
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    call.respondSuccess(qrCodeService.desativar(id, executorId), "QR Code desativado")
                }

                post("/regenerar") {
                    val executorId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
                    val request = call.receive<RegenerarQrCodeRequest>()
                    val criado = qrCodeService.regenerar(request, executorId)
                    call.respondSuccess(criado, "QR Code regenerado", HttpStatusCode.Created)
                }
            }
        }
    }
}
