import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.util.Properties

/**
 * Serviço de envio de e-mail via SMTP (similar ao Nodemailer).
 *
 * Configuração via variáveis de ambiente:
 *  - MAIL_HOST (default: smtp.gmail.com)
 *  - MAIL_PORT (default: 587)
 *  - MAIL_USER (obrigatório em produção)
 *  - MAIL_PASSWORD (obrigatório em produção)
 *  - MAIL_FROM (default: MAIL_USER)
 *  - MAIL_TLS (default: true)
 *  - MAIL_ENABLED (default: false — em dev não envia)
 */
class EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    private val host = System.getenv("MAIL_HOST") ?: "smtp.gmail.com"
    private val port = System.getenv("MAIL_PORT") ?: "587"
    private val user = System.getenv("MAIL_USER") ?: ""
    private val password = System.getenv("MAIL_PASSWORD") ?: ""
    private val from = System.getenv("MAIL_FROM") ?: user
    private val tlsEnabled = System.getenv("MAIL_TLS")?.toBooleanStrictOrNull() ?: true
    private val enabled = System.getenv("MAIL_ENABLED")?.toBooleanStrictOrNull() ?: false

    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", tlsEnabled.toString())
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        }

        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(user, password)
            }
        })
    }

    /**
     * Envia um e-mail simples (text/html).
     *
     * @param to       Destinatário
     * @param subject  Assunto
     * @param htmlBody Corpo do e-mail em HTML
     * @return true se enviado com sucesso, false se desabilitado ou falhou
     */
    fun send(to: String, subject: String, htmlBody: String): Boolean {
        if (!enabled) {
            logger.info("E-mail desabilitado (MAIL_ENABLED=false). Destinatário: {}, Assunto: {}", to, subject)
            return false
        }

        if (user.isBlank() || password.isBlank()) {
            logger.warn("MAIL_USER ou MAIL_PASSWORD não configurados. E-mail não enviado para: {}", to)
            return false
        }

        return try {
            val senderAddress = InternetAddress(from).apply { personal = "FilaCidadã" }
            val message = MimeMessage(session).apply {
                setFrom(senderAddress)
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject, "UTF-8")
                setContent(htmlBody, "text/html; charset=UTF-8")
            }
            Transport.send(message)
            logger.info("E-mail enviado para: {}", to)
            true
        } catch (e: MessagingException) {
            logger.error("Falha ao enviar e-mail para {}: {}", to, e.message)
            false
        }
    }

    /**
     * Envia e-mail de recuperação de senha com token e código.
     */
    fun sendRecoveryEmail(to: String, nome: String, token: String, codigo: String) {
        val frontendUrl = System.getenv("FRONTEND_URL") ?: "http://localhost:3000"
        val resetLink = "$frontendUrl/reset-password?token=$token"

        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', sans-serif; background: #f8fafc; padding: 40px;">
                <div style="max-width: 500px; margin: 0 auto; background: #fff; border-radius: 8px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.08);">
                    <h2 style="color: #1e3a5f; margin-bottom: 16px;">Recuperação de Senha</h2>
                    <p>Olá, <strong>$nome</strong>!</p>
                    <p>Recebemos uma solicitação para redefinir sua senha na plataforma <strong>FilaCidadã</strong>.</p>
                    
                    <div style="background: #f1f5f9; border-radius: 8px; padding: 20px; margin: 24px 0; text-align: center;">
                        <p style="margin: 0 0 8px; color: #64748b; font-size: 14px;">Seu código de recuperação:</p>
                        <p style="font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #1e3a5f; margin: 0;">$codigo</p>
                    </div>
                    
                    <p>Ou clique no botão abaixo para redefinir via link:</p>
                    <a href="$resetLink" style="display: inline-block; background: #0ea5e9; color: #fff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: 600;">Redefinir Senha</a>
                    
                    <p style="color: #94a3b8; font-size: 13px; margin-top: 24px;">Este código expira em 30 minutos. Se você não solicitou esta recuperação, ignore este e-mail.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;">
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">FilaCidadã — Plataforma de Gestão de Filas</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        send(to, "FilaCidadã — Código de Recuperação de Senha", html)
    }

    /**
     * Envia e-mail de boas-vindas para usuários criados via password-less.
     */
    fun sendWelcomeEmail(to: String, nome: String, token: String, codigo: String) {
        val frontendUrl = System.getenv("FRONTEND_URL") ?: "http://localhost:3000"
        val resetLink = "$frontendUrl/reset-password?token=$token"

        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', sans-serif; background: #f8fafc; padding: 40px;">
                <div style="max-width: 500px; margin: 0 auto; background: #fff; border-radius: 8px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.08);">
                    <h2 style="color: #1e3a5f; margin-bottom: 16px;">Bem-vindo ao FilaCidadã!</h2>
                    <p>Olá, <strong>$nome</strong>!</p>
                    <p>Sua conta foi criada na plataforma <strong>FilaCidadã</strong>. Para acessar, defina sua senha usando o código ou link abaixo.</p>
                    
                    <div style="background: #f1f5f9; border-radius: 8px; padding: 20px; margin: 24px 0; text-align: center;">
                        <p style="margin: 0 0 8px; color: #64748b; font-size: 14px;">Seu código de ativação:</p>
                        <p style="font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #1e3a5f; margin: 0;">$codigo</p>
                    </div>
                    
                    <a href="$resetLink" style="display: inline-block; background: #0ea5e9; color: #fff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: 600;">Definir Minha Senha</a>
                    
                    <p style="color: #94a3b8; font-size: 13px; margin-top: 24px;">Este código expira em 30 minutos.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;">
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">FilaCidadã — Plataforma de Gestão de Filas</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        send(to, "FilaCidadã — Bem-vindo! Defina sua senha", html)
    }
}
