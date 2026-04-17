class WebSocketManager {
    fun broadcast(room: String, event: String, payload: Any) {
        // Implementação intencionalmente leve/no-op.
        // Aqui é o ponto para integrar com WebSockets reais depois.
    }

    fun broadcastMany(rooms: List<String>, event: String, payload: Any) {
        rooms.distinct().forEach { broadcast(it, event, payload) }
    }
}
