# Aula Orientações Objetos

API RESTful desenvolvida em Kotlin com Ktor para gerenciar usuários, perfis e autenticação. Este projeto serve como material de aula de programação orientada a objetos.

## 🚀 Tecnologias

- **Kotlin** - Linguagem de programação
- **Ktor** - Framework web assíncrono
- **MongoDB** - Banco de dados NoSQL
- **JWT** - Autenticação baseada em tokens
- **Gradle** - Gerenciador de dependências e build
- **Docker** - Containerização
- **Swagger/OpenAPI** - Documentação da API

## 📋 Pré-requisitos

- JDK 11 ou superior
- MongoDB 4.0+
- Docker e Docker Compose (para execução containerizada)
- Gradle 7.0+ (ou use o gradlew incluído)

## 🔧 Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente ou crie um arquivo `.env`:

```bash
MONGODB_URI=mongodb://localhost:27017
JWT_SECRET=sua_chave_secreta_aqui
JWT_ISSUER=seu_issuer
JWT_AUDIENCE=seu_audience
```

### Instalação Local

1. Clone o repositório:
```bash
git clone https://gitlab.fslab.dev/gilberto/aula-orientacoes-objetos.git
cd aula-orientacoes-objetos
```

2. Instale as dependências e compile:
```bash
./gradlew build
```

3. Execute os testes:
```bash
./gradlew test
```

4. Inicie a aplicação:
```bash
./gradlew run
```

A API estará disponível em `http://localhost:8080`

## 🐳 Executar com Docker

### Usando Docker Compose

```bash
docker-compose up -d
```

Isso iniciará tanto a aplicação quanto o MongoDB.

### Build e execução manual

```bash
docker build -t filacidada-api .
docker run -p 8080:8080 -e MONGODB_URI=mongodb://mongo:27017 filacidada-api
```

## 📚 Estrutura do Projeto

```
src/main/kotlin/
├── Application.kt              # Entrada da aplicação
├── DatabaseSeed.kt             # Seed de dados iniciais
├── config/                      # Configurações (JWT, Koin, MongoDB)
├── dtos/                        # Data Transfer Objects
│   ├── request/                 # DTOs de requisição
│   └── response/                # DTOs de resposta
├── models/                      # Modelos de dados (Usuario, etc)
├── plugins/                     # Plugins do Ktor
│   ├── Authentication.kt
│   ├── Authorization.kt
│   ├── CORS.kt
│   ├── StatusPages.kt
│   └── Swagger.kt
├── repositories/                # Camada de dados (interfaces e implementações)
├── routes/                      # Definição de rotas da API
├── services/                    # Lógica de negócio
│   ├── AuthService.kt
│   ├── UsuarioService.kt
│   ├── EmailService.kt
│   └── FileStorageService.kt
└── utils/                       # Utilitários
    ├── Constants.kt
    ├── Extensions.kt
    └── PasswordUtils.kt
```

## 🔌 Endpoints Principais

### Autenticação
- `POST /auth/login` - Login e obtenção de token JWT
- `POST /auth/register` - Registro de novo usuário

### Usuários
- `GET /usuarios` - Listar usuários (paginado)
- `GET /usuarios/{id}` - Obter usuário por ID
- `PUT /usuarios/{id}` - Atualizar usuário
- `DELETE /usuarios/{id}` - Deletar usuário

### Perfis
- `GET /perfis` - Listar perfis
- `POST /perfis` - Criar perfil
- `PUT /perfis/{id}` - Atualizar perfil
- `DELETE /perfis/{id}` - Deletar perfil

Acesse a documentação completa em `http://localhost:8080/swagger-ui` após iniciar a aplicação.

## 🧪 Testes

O projeto inclui testes unitários e de integração:

```bash
# Executar todos os testes
./gradlew test

# Executar apenas testes unitários
./gradlew test --tests "*Unit*"

# Executar apenas testes de integração
./gradlew test --tests "*Integration*"
```

Testes unitários cobrem:
- Autenticação e criptografia de senha
- Serviços de negócio
- Serialização de dados

Testes de integração cobrem:
- Endpoints de autenticação
- Endpoints de usuários e perfis

## 📖 Documentação

A documentação técnica está disponível na pasta `documentacao/`:
- `Especificacao_Tecnica_API_Kotlin.html` - Especificações técnicas
- `Tutorial_Estrutura_API_Ktor.html` - Tutorial da estrutura
- `parte 1 - Classes_e_Repositorios_Kotlin.html` - Classes e repositórios
- `parte 2 - Services_e_Regras_de_Negocio_Kotlin.html` - Serviços e regras

A documentação OpenAPI está disponível em `src/main/resources/openapi/`.

## 🚢 Deploy

### Preparar para produção

```bash
# Build com otimizações
./gradlew build -x test

# Build de imagem Docker otimizada
docker build -f Dockerfile.deploy -t filacidada-api:prod .
```

### Usando Docker Compose para produção

```bash
docker-compose -f docker-compose.deploy.yml up -d
```

## 📝 Convenções de Código

- **Linguagem**: Kotlin
- **Padrão de nomes**: camelCase para variáveis e métodos, PascalCase para classes
- **Organização**: Separação clara entre camadas (models, repositories, services, routes)
- **Injeção de dependências**: Koin para IoC

## 🤝 Contribuindo

1. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
2. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
3. Push para a branch (`git push origin feature/AmazingFeature`)
4. Abra um Pull Request

## ✉️ Suporte

Para dúvidas ou problemas, abra uma issue no GitLab.

## 📄 Licença

Este projeto é um material de aula e está disponível para fins educacionais.

## 👤 Autor

Gilberto - Projeto de Aula de Orientação a Objetos
