interface QrCodeRepository {
    fun findById(id: String): QrCode?
    fun findByCodigo(codigo: String): QrCode?
    fun findAll(page: Int, limit: Int, filters: Map<String, Any?> = emptyMap()): Pair<List<QrCode>, Long>
    fun insert(qrCode: QrCode): QrCode
    fun update(id: String, updates: Map<String, Any?>): Boolean
    fun deactivate(id: String): Boolean
    fun deactivateAllAtivosByFila(filaId: String): Long
}
