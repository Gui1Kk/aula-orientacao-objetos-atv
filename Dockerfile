# ╔══════════════════════════════════════════════════════════════╗
# ║  FilaCidadã API — Dockerfile (multi-stage)                  ║
# ║  Stage 1 → Build do fat JAR com Gradle                      ║
# ║  Stage 2 → Imagem de runtime enxuta (JRE 17)                ║
# ╚══════════════════════════════════════════════════════════════╝

# ── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copia os wrappers primeiro para aproveitar o cache de camadas
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/

# Dá permissão de execução ao wrapper
RUN chmod +x gradlew

# Copia os descritores de build antes do código-fonte
# (o cache da camada é invalidado só quando esses arquivos mudarem)
COPY build.gradle.kts settings.gradle.kts ./

# Baixa as dependências em cache (sem compilar ainda)
RUN ./gradlew dependencies --no-daemon --quiet || true

# Copia o código-fonte
COPY src/ src/

# Gera o fat JAR (shadowJar — sufixo "-all" adicionado pelo plugin Ktor)
RUN ./gradlew buildFatJar --no-daemon -x test

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Usuário sem privilégios de root
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
USER appuser

# Copia apenas o fat JAR gerado no stage anterior
COPY --from=builder /app/build/libs/filacidada-api-all.jar app.jar

# Porta padrão da API (pode ser sobrescrita via env PORT)
EXPOSE 7351

# Opções de JVM: limites de memória sensatos para container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
