package br.com.qa.tarefa1;

import br.com.qa.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@Epic("ServeRest API")
@Feature("Usuários Básico")
@Tag("tarefa1")
class UsuariosTest extends BaseTest {

    @Test
    @Story("Listar usuários")
    @DisplayName("[+] GET /usuarios retorna 200, JSON e lista consistente")
    void listarUsuarios() {
        given().spec(spec)
                .when().get("/usuarios")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .time(lessThan(5000L))
                .body("quantidade", greaterThanOrEqualTo(0))
                .body("usuarios", instanceOf(List.class))
                .body("usuarios[0]", allOf(hasKey("nome"), hasKey("email"), hasKey("_id")));
    }
    @Test
    @Story("Usuário inexistente")
    @Tag("bug")
    @Severity(SeverityLevel.NORMAL)
    @Description("Recurso inexistente deveria retornar 404, mas a API retorna 400.")
    @DisplayName("[BUG] GET /usuarios/{id} inexistente deveria retornar 404")
    void usuarioInexistente() {
        given().spec(spec)
                .when().get("/usuarios/{id}", "idInexistente123")
                .then()
                .statusCode(404);
    }

    @Test
    @Story("Listar usuários")
    @DisplayName("[+] GET /usuarios?email= filtra pelo e-mail informado")
    void filtrarUsuarioPorEmail() {
        String email = emailUnico();
        criarUsuario(email, false);

        given().spec(spec).queryParam("email", email)
                .when().get("/usuarios")
                .then()
                .statusCode(200)
                .body("quantidade", equalTo(1))
                .body("usuarios[0].email", equalTo(email))
                .body("usuarios[0]", hasKey("_id"));
    }


    @Test
    @Story("Cadastrar usuário")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[+] POST /usuarios cria usuário e retorna 201 com _id")
    void cadastrarUsuario() {
        given().spec(spec).body(bodyUsuario(emailUnico(), false))
                .when().post("/usuarios")
                .then()
                .statusCode(201)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", allOf(notNullValue(), not(emptyString())));
    }

    @Test
    @Story("Cadastrar usuário")
    @DisplayName("[-] POST /usuarios com e-mail duplicado retorna 400")
    void cadastrarEmailDuplicado() {
        String email = emailUnico();
        criarUsuario(email, false);

        given().spec(spec).body(bodyUsuario(email, false))
                .when().post("/usuarios")
                .then()
                .statusCode(400)
                .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @Story("Cadastrar usuário")
    @DisplayName("[-] POST /usuarios com corpo vazio retorna 400 e lista campos obrigatórios")
    void cadastrarCorpoVazio() {
        given().spec(spec).body(new HashMap<>())
                .when().post("/usuarios")
                .then()
                .statusCode(400)
                .body("nome", equalTo("nome é obrigatório"))
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"))
                .body("administrador", equalTo("administrador é obrigatório"));
    }

    @Test
    @Story("Cadastrar usuário")
    @DisplayName("[-] POST /usuarios com e-mail inválido retorna 400")
    void cadastrarEmailInvalido() {
        given().spec(spec).body(bodyUsuario("email-sem-arroba", false))
                .when().post("/usuarios")
                .then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }


    @Test
    @Story("Buscar usuário por ID")
    @DisplayName("[+] GET /usuarios/{id} retorna 200 e dados do usuário")
    void buscarUsuarioPorId() {
        String email = emailUnico();
        String id = criarUsuario(email, true);

        given().spec(spec)
                .when().get("/usuarios/{id}", id)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("_id", equalTo(id))
                .body("email", equalTo(email))
                .body("nome", equalTo("Usuario QA"))
                .body("administrador", equalTo("true"));
    }


}
