interface LandingPageRepository {
    fun findDefault(): LandingPage?
    fun upsertDefault(landingPage: LandingPage): LandingPage
}
