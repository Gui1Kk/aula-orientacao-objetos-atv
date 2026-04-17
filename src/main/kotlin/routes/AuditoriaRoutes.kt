import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.auditoriaRoutes() {
    val auditoriaService by inject<AuditoriaService>()

    authenticate("auth-jwt") {
        route("/auditorias") {
            authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
                get {
                    val pagination = call.parsePagination(Constants.LIMIT_AUDITORIA_DEFAULT)
                    val filters = mapOf(
                        "instituicaoId" to call.request.queryParameters["instituicaoId"],
                        "usuarioId" to call.request.queryParameters["usuarioId"],
                        "acao" to call.request.queryParameters["acao"],
                        "entidade" to call.request.queryParameters["entidade"]
                    )
                    call.respondSuccess(auditoriaService.listar(pagination, filters))
                }
            }
        }
    }
}
