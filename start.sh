#!/bin/bash
set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔍 Verificando ambiente Java...${NC}"

# Tenta forçar o uso do JDK 17 que sabemos que funciona e existe no sistema
if [ -d "/usr/lib/jvm/java-1.17.0-openjdk-amd64" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-1.17.0-openjdk-amd64"
    echo -e "${GREEN}✅ JDK 17 encontrado e configurado: $JAVA_HOME${NC}"
else
    echo -e "${RED}⚠️ JDK 17 específico não encontrado. Tentando usar o padrão do sistema...${NC}"
fi

# Verifica versão do Java
$JAVA_HOME/bin/java -version

echo -e "\n${YELLOW}🏗️ Compilando o projeto (Skipping Tests)...${NC}"
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build concluído com sucesso!${NC}"
else
    echo -e "${RED}❌ Falha no build. Verifique os erros acima.${NC}"
    exit 1
fi

echo -e "\n${YELLOW}🚀 Iniciando containers Docker...${NC}"
echo -e "${YELLOW} OBS: Portas mapeadas para evitar conflitos locais:${NC}"
echo -e "   - API: http://localhost:8080"
echo -e "   - Swagger: http://localhost:8080/swagger-ui"
echo -e "   - MinIO Console: http://localhost:9011"
echo -e "   - Postgres (Externo): Porta 5438"

# Verifica se usa 'docker compose' ou 'docker-compose'
if docker compose version >/dev/null 2>&1; then
    docker compose up --build -d
else
    docker-compose up --build -d
fi

echo -e "\n${GREEN}✅ Ambiente iniciado com sucesso!${NC}"
echo -e "Para acompanhar os logs da API, execute: ${YELLOW}docker compose logs -f api${NC}"
echo -e "Para parar, execute: ${YELLOW}docker compose down${NC}"
