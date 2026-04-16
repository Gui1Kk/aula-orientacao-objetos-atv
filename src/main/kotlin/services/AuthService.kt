import java.time.Instant
import java.util.UUID

/**
 * Service de autenticação: login, register, refresh, logout, introspect, recover, reset.
 */
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val jwtConfig: JwtConfig,
    private val emailService: EmailService
) {

    fun login(request: LoginRequest): TokenResponse {
        val usuario = usuarioRepository.findByEmail(request.email)
            ?: throw ApiException(401, "Credenciais inválidas")

        if (!usuario.ativo) throw ApiException(403, "Usuário desativado")

        if (!PasswordUtils.verifyPassword(request.senha, usuario.senhaHash))
            throw ApiException(401, "Credenciais inválidas")

        val accessToken = jwtConfig.generateAccessToken(usuario.id!!, usuario.papeis, usuario.instituicaoId)
        val refreshToken = jwtConfig.generateRefreshToken(usuario.id)

        usuarioRepository.update(usuario.id, mapOf(
            "accesstoken" to accessToken,
            "refreshtoken" to refreshToken,
            "ultimoLoginEm" to Instant.now()
        ))

        return TokenResponse(accessToken, refreshToken, jwtConfig.accessTokenExpiresAt())
    }

    fun register(request: RegisterRequest): TokenResponse {
        if (usuarioRepository.findByEmail(request.email) != null)
            throw ApiException(409, "E-mail já cadastrado")

        val usuario = Usuario(
            id = UUID.randomUUID().toString(),
            nome = request.nome,
            email = request.email.lowercase(),
            senhaHash = PasswordUtils.hashPassword(request.senha),
            papeis = setOf(Papel.USUARIO_FINAL)
        )

        usuarioRepository.insert(usuario)

        val accessToken = jwtConfig.generateAccessToken(usuario.id!!, usuario.papeis, usuario.instituicaoId)
        val refreshToken = jwtConfig.generateRefreshToken(usuario.id)

        usuarioRepository.update(usuario.id, mapOf(
            "accesstoken" to accessToken,
            "refreshtoken" to refreshToken
        ))

        return TokenResponse(accessToken, refreshToken, jwtConfig.accessTokenExpiresAt())
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val decoded = try {
            jwtConfig.verifier.verify(request.token)
        } catch (_: Exception) {
            throw ApiException(401, "Refresh token inválido ou expirado")
        }

        val userId = decoded.getClaim("id").asString()
            ?: throw ApiException(401, "Token inválido")

        val usuario = usuarioRepository.findById(userId)
            ?: throw ApiException(401, "Usuário não encontrado")

        if (!usuario.ativo) throw ApiException(403, "Usuário desativado")

        val accessToken = jwtConfig.generateAccessToken(usuario.id!!, usuario.papeis, usuario.instituicaoId)
        val refreshToken = jwtConfig.generateRefreshToken(usuario.id)

        usuarioRepository.update(usuario.id, mapOf(
            "accesstoken" to accessToken,
            "refreshtoken" to refreshToken
        ))

        return TokenResponse(accessToken, refreshToken, jwtConfig.accessTokenExpiresAt())
    }

    fun logout(userId: String) {
        usuarioRepository.update(userId, mapOf(
            "accesstoken" to null,
            "refreshtoken" to null
        ))
    }

    fun recover(request: RecoverRequest): Map<String, String?> {
        val usuario = usuarioRepository.findByEmail(request.email)
            ?: throw ApiException(400, "E-mail não encontrado")

        val tokenUnico = UUID.randomUUID().toString()
        val codigo = (1..Constants.CODIGO_RECUPERACAO_LENGTH)
            .map { ('A'..'Z').random() }
            .joinToString("")
        val expiracao = Instant.now().plusMillis(Constants.CODIGO_RECUPERACAO_EXPIRATION_MS)

        usuarioRepository.update(usuario.id!!, mapOf(
            "tokenUnico" to tokenUnico,
            "codigoRecuperaSenha" to codigo,
            "expCodigoRecuperaSenha" to expiracao
        ))

        // Envia e-mail de recuperação quando MAIL_ENABLED=true
        emailService.sendRecoveryEmail(usuario.email, usuario.nome, tokenUnico, codigo)

        // Em dev retorna token e código; em prod apenas a mensagem genérica
        return mapOf("token" to tokenUnico, "codigo" to codigo)
    }

    fun resetByToken(token: String, request: ResetSenhaByTokenRequest) {
        val usuario = usuarioRepository.findByTokenUnico(token)
            ?: throw ApiException(401, "Token inválido ou expirado")

        usuarioRepository.update(usuario.id!!, mapOf(
            "senhaHash" to PasswordUtils.hashPassword(request.senha),
            "tokenUnico" to null,
            "codigoRecuperaSenha" to null,
            "expCodigoRecuperaSenha" to null
        ))
    }

    fun resetByCode(request: ResetSenhaByCodeRequest) {
        val usuario = usuarioRepository.findByCodigoRecuperacao(request.codigo)
            ?: throw ApiException(401, "Código inválido")

        if (usuario.expCodigoRecuperaSenha == null || Instant.now().isAfter(usuario.expCodigoRecuperaSenha))
            throw ApiException(401, "Código expirado")

        usuarioRepository.update(usuario.id!!, mapOf(
            "senhaHash" to PasswordUtils.hashPassword(request.senha),
            "tokenUnico" to null,
            "codigoRecuperaSenha" to null,
            "expCodigoRecuperaSenha" to null
        ))
    }
}

