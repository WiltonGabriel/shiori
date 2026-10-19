# ==========================================
# Estágio 1: Build (Maven + Eclipse Temurin JDK 25)
# ==========================================
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Otimização de camadas de cache Docker: copia dependências primeiro
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Valida e baixa dependências do Maven
RUN ./mvnw dependency:go-offline -B || true

# Copia o código-fonte e compila o artefato
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Estágio 2: Runtime (Eclipse Temurin JRE 25 Enxuto)
# ==========================================
FROM eclipse-temurin:25-jre AS runner
WORKDIR /app

# Instala curl para healthcheck do orquestrador e cria usuário não-root por segurança
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r shiori && \
    useradd -r -g shiori -u 1001 -m -d /app shiori

# Copia o artefato gerado no estágio de build com permissões do usuário shiori
COPY --from=builder --chown=shiori:shiori /workspace/target/*.jar /app/app.jar

# Define usuário de execução não-root
USER shiori:shiori

# Expõe a porta oficial da aplicação
EXPOSE 8080

# Healthcheck do container integrado ao Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Inicialização com Garbage Collector ZGC de alta performance e baixa latência
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
