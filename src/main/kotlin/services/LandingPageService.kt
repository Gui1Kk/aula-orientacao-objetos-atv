class LandingPageService(
    private val landingPageRepository: LandingPageRepository
) {
    fun buscarDefault(): LandingPage =
        landingPageRepository.findDefault()
            ?: throw ApiException(404, "Landing page não configurada")
}
