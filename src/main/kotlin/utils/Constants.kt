/**
 * Constantes globais da API FilaCidadã - Em construção.
 */
object Constants {

    // Colecoes MongoDB
    const val COLLECTION_USUARIOS = "usuarios"
    const val COLLECTION_INSTITUICOES = "instituicoes"
    const val COLLECTION_FILAS = "filas"
    const val COLLECTION_SENHAS = "senhas"
    const val COLLECTION_QRCODES = "qrcodes"
    const val COLLECTION_AUDITORIAS = "auditorias"
    const val COLLECTION_LANDING_PAGE = "landing_page"
    const val COLLECTION_EXAMPLES = "examples"

    // Paginacao
    const val PAGE_DEFAULT = 1
    const val LIMIT_DEFAULT = 10
    const val LIMIT_AUDITORIA_DEFAULT = 20
    const val LIMIT_MAX = 100

    // JWT
    const val JWT_ACCESS_EXPIRATION_MS = 15 * 60 * 1000L        // 15 minutos
    const val JWT_REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L // 7 dias
    const val JWT_ISSUER = "filacidada-api"
    const val JWT_AUDIENCE = "filacidada-client"
    const val JWT_REALM = "FilaCidadã"

    // Upload
    const val AVATAR_MAX_SIZE = 50L * 1024 * 1024    // 50 MB
    const val CONTRATO_MAX_SIZE = 10L * 1024 * 1024   // 10 MB
    const val UPLOAD_DIR = "uploads"

    // Fuso horario padrao
    const val FUSO_HORARIO_PADRAO = "America/Manaus"

    // Codigo de recuperacao
    const val CODIGO_RECUPERACAO_LENGTH = 4
    const val CODIGO_RECUPERACAO_EXPIRATION_MS = 30 * 60 * 1000L  // 30 minutos
}

