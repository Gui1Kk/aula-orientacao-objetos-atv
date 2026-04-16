import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

/**
 * Configuração de geração e validação de tokens JWT.
 *
 * Gera access token (curta duração) e refresh token (longa duração).
 * Payload do access token: { id, papeis, instituicaoId }.
 */
class JwtConfig(
    private val secret: String = System.getenv("JWT_SECRET") ?: "filacidada-secret-dev",
    private val issuer: String = Constants.JWT_ISSUER,
    private val audience: String = Constants.JWT_AUDIENCE,
    val realm: String = Constants.JWT_REALM
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    /**
     * Gera um access token JWT de curta duração.
     */
    fun generateAccessToken(userId: String, papeis: Set<Papel>, instituicaoId: String?): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("id", userId)
            .withClaim("papeis", papeis.map { it.name })
            .withClaim("instituicaoId", instituicaoId)
            .withExpiresAt(Date(System.currentTimeMillis() + Constants.JWT_ACCESS_EXPIRATION_MS))
            .withIssuedAt(Date())
            .sign(algorithm)
    }

    /**
     * Gera um refresh token JWT de longa duração.
     */
    fun generateRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("id", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + Constants.JWT_REFRESH_EXPIRATION_MS))
            .withIssuedAt(Date())
            .sign(algorithm)
    }

    /**
     * Retorna a data de expiração do access token (em ISO 8601).
     */
    fun accessTokenExpiresAt(): String {
        val expiry = Date(System.currentTimeMillis() + Constants.JWT_ACCESS_EXPIRATION_MS)
        return expiry.toInstant().toString()
    }
}

