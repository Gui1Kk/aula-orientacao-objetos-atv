import org.koin.dsl.module

val appModule = module {
    // Config
    single { MongoConfig() }
    single { JwtConfig() }
    single { WebSocketManager() }

    // Repositories
    single<UsuarioRepository> { UsuarioRepositoryImpl(get<MongoConfig>().usuarios) }
    single<InstituicaoRepository> { InstituicaoRepositoryImpl(get<MongoConfig>().instituicoes) }
    single<FilaRepository> { FilaRepositoryImpl(get<MongoConfig>().filas) }
    single<SenhaRepository> { SenhaRepositoryImpl(get<MongoConfig>().senhas) }
    single<QrCodeRepository> { QrCodeRepositoryImpl(get<MongoConfig>().qrcodes) }
    single<AuditoriaRepository> { AuditoriaRepositoryImpl(get<MongoConfig>().auditorias) }
    single<LandingPageRepository> { LandingPageRepositoryImpl(get<MongoConfig>().landingPages) }
    single<ExampleRepository> { ExampleRepositoryImpl(get<MongoConfig>().examples) }

    // Infra / auxiliares
    single { FileStorageService() }
    single { EmailService() }

    // Services
    single { AuditoriaService(get()) }
    single { AuthService(get(), get(), get()) }
    single { UsuarioService(get(), get()) }
    single { PerfilService(get()) }
    single { InstituicaoService(get(), get()) }
    single { FilaService(get(), get(), get(), get()) }
    single { SenhaService(get(), get(), get(), get(), get()) }
    single { QrCodeService(get(), get(), get(), get()) }
    single { LandingPageService(get()) }
    single { ExampleService(get()) }
}
