import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * Serializer customizado para java.time.Instant ↔ kotlinx.serialization.
 *
 * Serializa como String ISO 8601 (ex: "2026-02-25T14:30:00Z").
 *
 * Uso nos models:
 *   @Serializable
 *   data class Exemplo(
 *       @Serializable(with = InstantSerializer::class)
 *       val createdAt: Instant = Instant.now()
 *   )
 *
 * Ou registrar globalmente no módulo de serialização.
 */
object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

