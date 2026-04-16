import org.mindrot.jbcrypt.BCrypt

/**
 * Utilitários para hash e verificação de senhas com BCrypt (jBCrypt).
 */
object PasswordUtils {

    private const val BCRYPT_ROUNDS = 12

    /**
     * Gera o hash BCrypt de uma senha em texto puro.
     */
    fun hashPassword(plainPassword: String): String {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS))
    }

    /**
     * Verifica se uma senha em texto puro corresponde ao hash armazenado.
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}

