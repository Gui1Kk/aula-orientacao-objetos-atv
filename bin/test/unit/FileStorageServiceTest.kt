import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File

/**
 * Testes unitários do FileStorageService.
 */
class FileStorageServiceTest {

    private val fileStorageService = FileStorageService()

    @AfterEach
    fun cleanup() {
        // Remove arquivos criados durante o teste
        File("${Constants.UPLOAD_DIR}/avatars").listFiles()?.forEach { it.delete() }
        File("${Constants.UPLOAD_DIR}/contratos").listFiles()?.forEach { it.delete() }
    }

    @Test
    fun `salvarAvatar com tipo valido salva arquivo e retorna caminho`() {
        val bytes = "fake-image-content".toByteArray()
        val caminho = fileStorageService.salvarAvatar(bytes, "image/png", "foto.png")

        assertTrue(caminho.startsWith("${Constants.UPLOAD_DIR}/avatars/"))
        assertTrue(caminho.endsWith(".png"))
        assertTrue(File(caminho).exists())
    }

    @Test
    fun `salvarAvatar com jpeg funciona`() {
        val bytes = "fake-image-content".toByteArray()
        val caminho = fileStorageService.salvarAvatar(bytes, "image/jpeg", "foto.jpg")

        assertTrue(caminho.endsWith(".jpg"))
        assertTrue(File(caminho).exists())
    }

    @Test
    fun `salvarAvatar com tipo invalido lanca 400`() {
        val bytes = "fake-content".toByteArray()

        val exception = assertThrows<ApiException> {
            fileStorageService.salvarAvatar(bytes, "application/pdf", "file.pdf")
        }
        assertEquals(400, exception.statusCode)
        assertTrue(exception.message.contains("Tipo de imagem"))
    }

    @Test
    fun `salvarAvatar com contentType null lanca 400`() {
        val bytes = "fake-content".toByteArray()

        val exception = assertThrows<ApiException> {
            fileStorageService.salvarAvatar(bytes, null, "foto.png")
        }
        assertEquals(400, exception.statusCode)
    }

    @Test
    fun `salvarContrato com PDF valido salva arquivo`() {
        val bytes = "fake-pdf-content".toByteArray()
        val caminho = fileStorageService.salvarContrato(bytes, "application/pdf", "contrato.pdf")

        assertTrue(caminho.startsWith("${Constants.UPLOAD_DIR}/contratos/"))
        assertTrue(caminho.endsWith(".pdf"))
        assertTrue(File(caminho).exists())
    }

    @Test
    fun `salvarContrato com tipo invalido lanca 400`() {
        val bytes = "fake-content".toByteArray()

        val exception = assertThrows<ApiException> {
            fileStorageService.salvarContrato(bytes, "image/png", "imagem.png")
        }
        assertEquals(400, exception.statusCode)
        assertTrue(exception.message.contains("PDF"))
    }

    @Test
    fun `removerArquivo remove arquivo existente`() {
        val bytes = "temp-content".toByteArray()
        val caminho = fileStorageService.salvarAvatar(bytes, "image/png", "temp.png")
        assertTrue(File(caminho).exists())

        val removed = fileStorageService.removerArquivo(caminho)

        assertTrue(removed)
        assertFalse(File(caminho).exists())
    }

    @Test
    fun `removerArquivo com caminho null retorna false`() {
        assertFalse(fileStorageService.removerArquivo(null))
    }

    @Test
    fun `removerArquivo com caminho vazio retorna false`() {
        assertFalse(fileStorageService.removerArquivo(""))
    }

    @Test
    fun `removerArquivo com caminho inexistente retorna false`() {
        assertFalse(fileStorageService.removerArquivo("uploads/avatars/nao-existe.png"))
    }
}

