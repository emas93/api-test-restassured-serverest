# API Test - Rest Assured + ServeRest

Automação de testes de API REST com **Rest Assured**, **JUnit 5** e **Allure Report**, usando a API pública [ServeRest](https://serverest.dev) como alvo.

Os testes validam **status code, headers e corpo** das respostas em cenários positivos e negativos, cobrindo os métodos **GET, POST, PUT e DELETE** nos recursos `/login`, `/usuarios` e `/produtos`.

## Tecnologias

| Ferramenta | Versão | Uso |
|---|---|---|
| Java | 17+ | Linguagem |
| Maven | 3.8+ | Build e gerenciamento de dependências |
| Rest Assured | 5.5.0 | Requisições e validações da API |
| JUnit 5 | 5.10.2 | Execução dos testes e organização por tags |
| Jackson Databind | 2.17.2 | Serialização do corpo das requisições em JSON |
| Allure | 2.27.0 | Relatório detalhado (request e response de cada teste) |
| Surefire Report | 3.2.5 | Relatório HTML simples |

## Pré-requisitos

- [Java JDK 17](https://adoptium.net/) ou superior
- [Apache Maven](https://maven.apache.org/download.cgi) 3.8 ou superior
- Acesso à internet (a API é pública e o Maven baixa as dependências na primeira execução)

Para conferir se estão instalados:

```bash
java -version
mvn -version
```

## Como rodar

```bash
# 1. Clonar o repositório
git clone https://github.com/emas93/api-test-restassured-serverest.git
cd api-test-restassured-serverest

# 2. Executar todos os testes
mvn clean test
```

Na primeira execução o Maven baixa as dependências, então pode demorar um pouco mais.

### Executar apenas uma parte

Os testes são organizados em tags:

| Tag | Conteúdo |
|---|---|
| `tarefa1` | Login e usuários (validação de endpoints, cenários positivos e negativos) |
| `tarefa2` | CRUD completo de usuários e produtos (GET, POST, PUT e DELETE) |
| `bug` | Testes que documentam desvios da API e **devem falhar** (veja [Achados na API](#achados-na-api)) |

Um teste pode ter mais de uma tag. Os testes `bug` também pertencem a `tarefa1` ou `tarefa2`, então `-Dgroups=tarefa1` inclui os `bug` dessa tarefa.

```bash
mvn clean test -Dgroups=tarefa1
mvn clean test -Dgroups=tarefa2

# só os testes que devem passar (exclui os [BUG])
mvn clean test -DexcludedGroups=bug

# só os testes que documentam desvios da API
mvn clean test -Dgroups=bug

# uma única classe
mvn clean test -Dtest=LoginTest

# um único método
mvn clean test -Dtest=ProdutosCrudTest#excluirProduto
```

### Apontar para outra instância da API

Por padrão os testes usam `https://serverest.dev`. Para usar uma instância local da ServeRest:

```bash
npx serverest
mvn clean test -Dbase.url=http://localhost:3000
```

## Relatórios

### Allure (detalhado)

```bash
mvn clean test -Dmaven.test.failure.ignore=true
mvn allure:serve
```

O primeiro comando executa os testes sem interromper o build quando há falhas. O segundo gera o relatório e o abre no navegador. Na primeira vez o plugin baixa o Allure, então precisa de internet. O relatório fica disponível enquanto o terminal estiver aberto.

Para gerar os arquivos do relatório sem abrir o navegador:

```bash
mvn allure:report
```

O resultado fica em `target/site/allure-maven-plugin/`. Não abra o `index.html` com duplo clique, porque o navegador bloqueia o carregamento dos dados em arquivos locais e a página aparece em branco. Sirva a pasta por um servidor local:

```bash
cd target/site/allure-maven-plugin
python -m http.server 8000
# acesse http://localhost:8000
```

No relatório:

- **Overview**: totais de testes que passaram e falharam, com gráficos.
- **Behaviors**: testes agrupados por Epic, Feature e Story.
- **Suites**: testes agrupados por classe.
- Ao abrir um teste, aparecem a **requisição e a resposta completas** (URL, headers e body).

### Surefire (simples)

```bash
mvn surefire-report:report
```

Gera `target/site/surefire-report.html`. Os resultados brutos ficam em `target/surefire-reports/`.

## Estrutura do projeto

```
.
├── pom.xml
└── src/test/java/br/com/qa
    ├── base
    │   └── BaseTest.java              # configuração, massa de dados e métodos auxiliares
    ├── tarefa1
    │   ├── LoginTest.java             # POST /login
    │   └── UsuariosTest.java          # GET e POST em /usuarios
    └── tarefa2
        ├── UsuariosCrudTest.java      # GET, POST, PUT e DELETE em /usuarios
        └── ProdutosCrudTest.java      # GET, POST, PUT e DELETE em /produtos (com token)
```

## Cenários cobertos

São **29 testes**: 24 validam o comportamento atual da API e 5, marcados com **[BUG]** e com a tag `bug`, afirmam o comportamento esperado pelas boas práticas REST e devem falhar (veja a seção [Achados na API](#achados-na-api)).

### Login (`POST /login`)

| Cenário | Resultado esperado |
|---|---|
| Credenciais válidas | 200 e token `Bearer` |
| Senha errada | 401 e mensagem de erro, sem token |
| Corpo sem campos obrigatórios | 400 e mensagem por campo |

### Usuários

| Método | Cenário | Resultado esperado |
|---|---|---|
| GET `/usuarios` | Listagem | 200, JSON e lista consistente |
| GET `/usuarios?email=` | Filtro por e-mail | 200 e apenas o usuário filtrado |
| GET `/usuarios/{id}` | ID existente | 200 e dados do usuário |
| GET `/usuarios/{id}` | ID inexistente **[BUG]** | 404 |
| GET `/usuarios/{id}` | Não expor a senha **[BUG]** | Resposta sem o campo `password` |
| POST `/usuarios` | Cadastro válido | 201 e `_id` |
| POST `/usuarios` | E-mail duplicado | 400 |
| POST `/usuarios` | Corpo vazio | 400 com os campos obrigatórios |
| POST `/usuarios` | E-mail inválido | 400 |
| PUT `/usuarios/{id}` | Alteração | 200 e dados confirmados por um GET |
| PUT `/usuarios/{id}` | E-mail já usado por outro usuário | 400 |
| PUT `/usuarios/{id}` | ID inexistente **[BUG]** | 404 |
| DELETE `/usuarios/{id}` | Exclusão | 200 e usuário deixa de existir |
| DELETE `/usuarios/{id}` | ID inexistente **[BUG]** | 404 |

### Produtos (rotas protegidas por token de administrador)

| Método | Cenário | Resultado esperado |
|---|---|---|
| GET `/produtos` | Listagem | 200, JSON e lista |
| GET `/produtos/{id}` | ID existente | 200 e dados do produto |
| GET `/produtos/{id}` | ID inexistente **[BUG]** | 404 |
| POST `/produtos` | Com token de administrador | 201 e `_id` |
| POST `/produtos` | Sem token | 401 |
| POST `/produtos` | Nome duplicado | 400 |
| POST `/produtos` | Usuário não administrador | 403 |
| PUT `/produtos/{id}` | Alteração | 200 e dados confirmados por um GET |
| PUT `/produtos/{id}` | Sem token | 401 |
| DELETE `/produtos/{id}` | Exclusão | 200 e produto deixa de existir |
| DELETE `/produtos/{id}` | Sem token | 401 |

## Achados na API

A ServeRest é uma API feita para prática de testes, e alguns comportamentos fogem das convenções REST e de segurança. Os testes marcados com **[BUG]** documentam esses desvios: eles verificam o comportamento **esperado**, então falham enquanto a API se comportar de outra forma. Essas falhas são intencionais.

| Achado | Esperado | Comportamento observado |
|---|---|---|
| GET de usuário ou produto inexistente | 404 | 400 |
| DELETE de usuário inexistente | 404 | 200 com a mensagem "Nenhum registro excluído" |
| PUT de usuário com ID inexistente | 404 | Cria um novo usuário (201) |
| Senha do usuário nas respostas | Campo `password` não exposto | `GET /usuarios/{id}` devolve a senha em **texto puro** (severidade mais alta dos achados) |

Todos os testes desta seção têm a tag `bug`. Por causa das falhas, `mvn clean test` termina com `BUILD FAILURE`. Existem duas formas de contornar isso:

```bash
# roda tudo e gera o relatório completo, sem interromper o build nas falhas
mvn clean test -Dmaven.test.failure.ignore=true

# roda só o que deve passar (build verde)
mvn clean test -DexcludedGroups=bug
```

## Boas práticas aplicadas

- **Testes independentes**: cada teste cria os próprios dados, com e-mails e nomes únicos (UUID). Eles rodam em qualquer ordem e podem ser repetidos sem conflito na API pública.
- **Configuração centralizada**: a `BaseTest` concentra a URL base, a especificação da requisição e os métodos auxiliares (criar usuário, fazer login, obter token de administrador e criar produto).
- **Validação completa**: os testes verificam status code, header `Content-Type` e corpo. Nos fluxos de PUT e DELETE, um GET posterior confirma o efeito da operação.
- **Rastreabilidade**: as anotações `@Epic`, `@Feature`, `@Story`, `@Severity` e `@DisplayName` organizam o relatório Allure.

## Observações

- O `-javaagent` do AspectJ, configurado no `pom.xml`, é necessário para o Allure registrar os passos dos testes.
- A pasta `.allure/`, criada pelo plugin, e a pasta `target/` estão no `.gitignore`.
- A ServeRest é uma API pública compartilhada, então o tempo de resposta pode variar.
