import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Rotas de perfil: /me, /me/senha, /me/avatar, /timezones (§9.9)
 */
fun Route.perfilRoutes() {
    val perfilService by inject<PerfilService>()
    val fileStorageService by inject<FileStorageService>()

    authenticate("auth-jwt") {

        // GET /me — autenticado
        get("/me") {
            val userId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
            val me = perfilService.me(userId)
            call.respondSuccess(me)
        }

        // PATCH /me — autenticado
        patch("/me") {
            val userId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
            val request = call.receive<UpdatePerfilRequest>()
            perfilService.atualizarPerfil(userId, request)
            val atualizado = perfilService.me(userId)
            call.respondSuccess(atualizado, "Perfil atualizado")
        }

        // PATCH /me/senha — autenticado
        patch("/me/senha") {
            val userId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")
            val request = call.receive<AlterarSenhaRequest>()
            perfilService.alterarSenha(userId, request)
            call.respondEmptySuccess("Senha alterada")
        }

        // POST /me/avatar — autenticado (upload multipart)
        post("/me/avatar") {
            val userId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")

            val multipart = call.receiveMultipart()
            var caminho: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem && part.name == "avatar") {
                    val bytes = part.streamProvider().readBytes()
                    val contentType = part.contentType?.toString()
                    val fileName = part.originalFileName
                    caminho = fileStorageService.salvarAvatar(bytes, contentType, fileName)
                }
                part.dispose()
            }

            if (caminho == null) throw ApiException(400, "Nenhum arquivo enviado no campo 'avatar'")

            // Remove avatar anterior se existir
            val me = perfilService.me(userId)
            me.avatar?.let { fileStorageService.removerArquivo(it) }

            perfilService.salvarAvatar(userId, caminho!!)
            val atualizado = perfilService.me(userId)
            call.respondSuccess(atualizado, "Avatar atualizado")
        }

        // DELETE /me/avatar — autenticado
        delete("/me/avatar") {
            val userId = call.currentUserId() ?: throw ApiException(401, "Não autenticado")

            // Remove arquivo do disco
            val me = perfilService.me(userId)
            me.avatar?.let { fileStorageService.removerArquivo(it) }

            perfilService.removerAvatar(userId)
            call.respondEmptySuccess("Avatar removido")
        }
    }

    // GET /timezones — público
    get("/timezones") {
        val result = perfilService.listarTimezones()
        call.respondSuccess(result)
    }
}
