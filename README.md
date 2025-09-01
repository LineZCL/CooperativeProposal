# Sistema de Votação de Pauta para Cooperativas

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-ff6600)

## 📋 Descrição

O Sistema de Votação de Pautas para Cooperativas é uma aplicação backend desenvolvida em Spring Boot que permite o gerenciamento de pautas e sessões de votação para cooperativas. O sistema oferece funcionalidades completas para criação de pautas, abertura de sessões de votação com tempo controlado, e registro de votos de associados.

## 🚀 Funcionalidades Principais

### 📝 Gestão de Pautas
- **Criação de propostas**: Criação de novas pautas com título e descrição
- **Listagem paginada**: Visualização de todas as pautas com paginação e ordenação
- **Detalhes da proposta**: Consulta de informações detalhadas de uma pauta específica

### 🗳️ Sistema de Votação
- **Abertura de sessões**: Abertura de sessões de votação com duração configurável (padrão: 60 segundos)
- **Controle temporal**: Fechamento automático de sessões após o tempo limite
- **Validação de votos**: Prevenção de votos duplicados por associado
- **Validação de CPF**: Verificação de permissão de voto através de validação de CPF ⚠️
- **Contabilização**: Contagem automática de votos "Sim" e "Não"

### 📱 Interface Mobile
- **Telas mobile**: Endpoints específicos para aplicações mobile com formato JSON customizado
- **Lista de propostas**: Tela de seleção mobile para navegação entre propostas
- **Formulário de votação**: Interface mobile para registro de votos
- **Criação de propostas**: Formulário mobile para criação de novas pautas

### ⚡ Funcionalidades Assíncronas
- **Mensageria RabbitMQ**: Processamento assíncrono de eventos, para agendamento e fechamento automático de sessões de votação.

## 🏗️ Arquitetura

### Tecnologias Utilizadas
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.5** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Cloud OpenFeign** - Comunicação entre serviços
- **PostgreSQL** - Banco de dados principal
- **RabbitMQ** - Sistema de mensageria
- **Flyway** - Migração de banco de dados
- **MapStruct** - Mapeamento entre objetos
- **Lombok** - Redução de código boilerplate
- **Swagger/OpenAPI** - Documentação da API

### Ferramentas de Qualidade
- **JaCoCo** - Cobertura de testes (mínimo 90%)
- **Checkstyle** - Verificação de estilo de código
- **SpotBugs** - Análise estática de código
- **JUnit 5** - Testes unitários

### Escolha do Banco de Dados (PostgreSQL)

O PostgreSQL foi escolhido como banco de dados principal pelos seguintes motivos:

- **JSONB**: PostgreSQL oferece excelente suporte a documentos JSON
- **ACID**: Necessário para votações (integridade crítica)  
- **Simplicidade**: Uma tecnologia resolve ambos os casos
- **Performance**: Consultas relacionais + JSON em um só lugar
- **Manutenção**: Menos complexidade operacional

Esta escolha permite que o sistema mantenha dados estruturados para as entidades principais (pautas, votos, sessões) enquanto oferece flexibilidade para armazenar dados dinâmicos como configurações de telas mobile e metadados adicionais em formato JSON quando necessário.

## ⚠️ Alertas Importantes

### Validação de CPF
O serviço externo de validação de CPF (`https://user-info.herokuapp.com`) atualmente **não está funcionando**. Por este motivo, existe a flag `CPF_VALIDATION_ENABLED` que permite desabilitar essa validação:

```yaml
app:
  cpf-validation-enabled: false  # Desabilita validação de CPF
```

**Recomendação**: Mantenha a validação desabilitada até que um novo serviço de validação seja configurado ou o serviço atual seja restaurado.

## 🛠️ Configuração e Execução

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Gradle

### 🐳 Execução com Docker

1. **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd CooperativeProposals
```

2. **Execute os serviços de infraestrutura**
```bash
docker-compose up -d
```

Isso iniciará:
- PostgreSQL na porta 5432
- RabbitMQ na porta 5672 (Management UI na porta 15672)

3. **Execute a aplicação**
```bash
./gradlew bootRun
```

### ⚙️ Configuração Manual

#### Banco de Dados PostgreSQL
```bash
# Criar banco de dados
createdb cooperative_proposals

# As migrações serão executadas automaticamente pelo Flyway
```

#### RabbitMQ
```bash
# Executar RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  heidiks/rabbitmq-delayed-message-exchange:3.13.3-management
```

### 🔧 Variáveis de Ambiente

```bash
# Banco de dados
POSTGRES_DB=jdbc:postgresql://localhost:5432/cooperative_proposals
POSTGRES_USER=coop
POSTGRES_PASSWORD=coop

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=coop-guest
RABBITMQ_PASSWORD=coop-guest

# Aplicação
APP_PORT=8080
APP_BASE_URL=http://localhost:8080
CPF_VALIDATION_ENABLED=true
CPF_VALIDATION_URL=https://user-info.herokuapp.com
```

## 📚 Documentação da API

Após iniciar a aplicação, a documentação Swagger estará disponível em:
```
http://localhost:8080/api/v1/docs
```

### Principais Endpoints

#### Pautas
- `POST /api/v1/proposal` - Criar nova pauta
- `GET /api/v1/proposal` - Listar pautas (paginado)
- `GET /api/v1/proposal/{id}` - Obter detalhes da pauta
- `POST /api/v1/proposal/{id}/open` - Abrir sessão de votação
- `POST /api/v1/proposal/{id}/vote` - Registrar voto

#### Mobile
- `GET /api/v1/mobile/proposals` - Lista de pauta (formato mobile)
- `GET /api/v1/mobile/proposal/{id}` - Detalhes da pauta (formato mobile)
- `GET /api/v1/mobile/vote-form/{id}/{choice}` - Formulário de votação
- `GET /api/v1/mobile/new-proposal` - Formulário de nova pauta

## 🧪 Testes e Qualidade

### Executar Testes
```bash
# Executar todos os testes
./gradlew test

# Gerar relatório de cobertura
./gradlew jacocoTestReport

# Verificar cobertura mínima
./gradlew jacocoTestCoverageVerification
```

### Linting e Análise de Código
```bash
# Executar todas as verificações
./gradlew lint

# Apenas Checkstyle
./gradlew lintCheckstyle

# Apenas SpotBugs
./gradlew lintSpotbugs

# Apenas código principal
./gradlew lintMain
```

### Relatórios
Os relatórios são gerados em:
- **Testes**: `build/reports/tests/test/index.html`
- **Cobertura**: `build/reports/jacoco/test/html/index.html`
- **Checkstyle**: `build/reports/checkstyle/main.html`
- **SpotBugs**: `build/reports/spotbugs/main.html`

## 🗄️ Modelo de Dados

### Principais Entidades

#### Proposal (Proposta)
- `id`: UUID único
- `title`: Título da proposta
- `description`: Descrição detalhada

#### VotingSession (Sessão de Votação)
- `id`: UUID único
- `proposal_id`: Referência à proposta
- `opened_at`: Data/hora de abertura
- `closes_at`: Data/hora de fechamento
- `status`: Status da sessão

#### Vote (Voto)
- `id`: UUID único
- `proposal_id`: Referência à proposta
- `associate_id`: ID do associado
- `voting_session_id`: Referência à sessão
- `vote`: Valor do voto (true/false)
- `voted_at`: Data/hora do voto

## 🔄 Fluxo de Votação

1. **Criação da Proposta**: Associado cria uma nova proposta
2. **Abertura da Sessão**: Sessão de votação é aberta com duração definida
3. **Período de Votação**: Associados registram seus votos
4. **Validações**: Sistema valida CPF e previne votos duplicados, caso a flag esteja ligada. 
5. **Fechamento Automático**: Sessão é fechada automaticamente após o tempo limite
6. **Contabilização**: Votos são contabilizados e resultado é disponibilizado

## 🚦 Status do Projeto

- ✅ API REST completa
- ✅ Interface mobile
- ✅ Sistema de votação
- ✅ Validação de associados
- ✅ Mensageria assíncrona
- ✅ Testes automatizados
- ✅ Documentação Swagger
- ✅ Qualidade de código

---

💡 **Dica**: Para mais informações sobre linting e qualidade de código, consulte o arquivo `LINTING.md`.
