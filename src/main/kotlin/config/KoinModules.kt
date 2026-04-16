import org.koin.dsl.module

/**
 * Módulos Koin para injeção de dependência.
 *
 * Registra: config, repositories e services de Usuário.
 */
val appModule = module {

    // Config
    single { MongoConfig() }
    single { JwtConfig() }

    // Repositories
    single<UsuarioRepository> { UsuarioRepositoryImpl(get<MongoConfig>().usuarios) }
    single<ExampleRepository> { ExampleRepositoryImpl(get<MongoConfig>().examples) }

    // File Storage
    single { FileStorageService() }

    // Email
    single { EmailService() }

    // Services
    single { AuthService(get(), get(), get()) }
    single { UsuarioService(get(), get()) }
    single { PerfilService(get()) }
    single { ExampleService(get()) }
}

