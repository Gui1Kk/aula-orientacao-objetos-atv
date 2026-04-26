import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.landingPageRoutes() {
    val landingPageService by inject<LandingPageService>()

    get("/landing-page") {
        call.respondSuccess(landingPageService.buscarDefault())
    }
}
