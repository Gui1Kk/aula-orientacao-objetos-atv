import java.io.File
import java.util.UUID

/**
 * Service de armazenamento de arquivos em disco local.
 *
 * Gerencia upload de avatares e contratos com validação de tipo MIME e tamanho.
 * Arquivos são salvos em: {UPLOAD_DIR}/{subdir}/{uuid}.{ext}
 */
class FileStorageService {

    private val uploadDir = Constants.UPLOAD_DIR

    companion object {
        val AVATAR_ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
        val CONTRATO_ALLOWED_TYPES = setOf("application/pdf")
    }

    init {
        // Garante que as pastas de upload existem
        File("$uploadDir/avatars").mkdirs()
        File("$uploadDir/contratos").mkdirs()
    }

    /**
     * Salva um avatar de usuário.
     *
     * @return Caminho relativo do arquivo salvo (ex: "uploads/avatars/uuid.png")
     * @throws ApiException se tipo MIME ou tamanho inválido
     */
    fun salvarAvatar(bytes: ByteArray, contentType: String?, originalFileName: String?): String {
        validarTipo(contentType, AVATAR_ALLOWED_TYPES, "Tipo de imagem não permitido. Aceitos: JPEG, PNG, WebP, GIF")
        validarTamanho(bytes.size.toLong(), Constants.AVATAR_MAX_SIZE, "Avatar excede o tamanho máximo de 50 MB")

        val ext = extrairExtensao(originalFileName, contentType)
        val nomeArquivo = "${UUID.randomUUID()}.$ext"
        val caminho = "$uploadDir/avatars/$nomeArquivo"

        File(caminho).writeBytes(bytes)
        return caminho
    }

    /**
     * Salva um contrato de instituição (PDF).
     *
     * @return Caminho relativo do arquivo salvo (ex: "uploads/contratos/uuid.pdf")
     * @throws ApiException se tipo MIME ou tamanho inválido
     */
    fun salvarContrato(bytes: ByteArray, contentType: String?, originalFileName: String?): String {
        validarTipo(contentType, CONTRATO_ALLOWED_TYPES, "Tipo de arquivo não permitido. Aceito: PDF")
        validarTamanho(bytes.size.toLong(), Constants.CONTRATO_MAX_SIZE, "Contrato excede o tamanho máximo de 10 MB")

        val ext = "pdf"
        val nomeArquivo = "${UUID.randomUUID()}.$ext"
        val caminho = "$uploadDir/contratos/$nomeArquivo"

        File(caminho).writeBytes(bytes)
        return caminho
    }

    /**
     * Remove um arquivo do disco.
     *
     * @return true se removido, false se não existia
     */
    fun removerArquivo(caminho: String?): Boolean {
        if (caminho.isNullOrBlank()) return false
        val file = File(caminho)
        return if (file.exists()) file.delete() else false
    }

    // Validacoes

    private fun validarTipo(contentType: String?, tipos: Set<String>, mensagem: String) {
        if (contentType == null || contentType.lowercase() !in tipos) {
            throw ApiException(400, mensagem)
        }
    }

    private fun validarTamanho(size: Long, maxSize: Long, mensagem: String) {
        if (size > maxSize) {
            throw ApiException(400, mensagem)
        }
    }

    private fun extrairExtensao(originalFileName: String?, contentType: String?): String {
        // Tenta extrair do nome original
        originalFileName?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.isNotEmpty() }?.let {
            return it
        }
        // Fallback pelo content type
        return when (contentType?.lowercase()) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "application/pdf" -> "pdf"
            else -> "bin"
        }
    }
}

