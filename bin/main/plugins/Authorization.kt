import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Plugin custom de autorização por papéis.
 *
 * Uso nas rotas:
 *   authorize(Papel.ADMIN_PLATAFORMA, Papel.ADMIN_INSTITUICAO) {
 *       get("/recurso") { ... }
 *   }
 */
fun Route.authorize(vararg papeis: Papel, build: Route.() -> Unit): Route {
    val route = createChild(AuthorizationRouteSelector())

    route.intercept(ApplicationCallPipeline.Call) {
        val userPapeis = call.currentUserPapeis()
        if (userPapeis.none { it in papeis }) {
                call.respond(HttpStatusCode.Forbidden, ApiResponse.error("Acesso negado"))
            finish()
        }
    }

    route.build()
    return route
}

/**
 * Selector customizado para não interferir com o roteamento Ktor.
 */
class AuthorizationRouteSelector : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
        RouteSelectorEvaluation.Transparent
}
