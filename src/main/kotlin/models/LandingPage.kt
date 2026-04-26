import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.Instant

@Serializable
data class LandingPage(
    val id: String? = null,
    val key: String = DEFAULT_KEY,
    val header: JsonElement? = null,
    val hero: JsonElement? = null,
    val statistics: JsonElement? = null,
    val features: JsonElement? = null,
    val steps: JsonElement? = null,
    val targets: JsonElement? = null,
    val testimonials: JsonElement? = null,
    val faq: JsonElement? = null,
    val cta: JsonElement? = null,
    val footer: JsonElement? = null,
    @Contextual val createdAt: Instant = Instant.now(),
    @Contextual val updatedAt: Instant? = null
) {
    companion object {
        const val DEFAULT_KEY = "default"
    }
}
