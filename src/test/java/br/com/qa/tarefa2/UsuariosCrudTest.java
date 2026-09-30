package br.com.qa.tarefa2;

import br.com.qa.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@Epic("ServeRest API")
@Feature("Tarefa 2 - Usuários CRUD - Cenários positivos e negativos")
@Tag("tarefa2")
class UsuariosCrudTest extends BaseTest {

    @Test
    @Tag("bug")
    @Story("GET")
    @DisplayName("[BUG] GET /usuarios/{id} - 200, header e corpo")
    void get() {
        String email = emailUnico();
        String id = criarUsuario(email, false);

        given().spec(spec)
                .when().get("/usuarios/{id}", id)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("_id", equalTo(id))
                .body("email", equalTo(email))
                .body(not(hasKey("password")))
                .body(not(containsString("senha123")));
    }


    @Test
    @Story("POST")
    @DisplayName("POST /usuarios - 201, header e corpo")
    void post() {
        given().spec(spec).body(bodyUsuario(emailUnico(), false))
                .when().post("/usuarios")
                .then()
                .statusCode(201)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    @Test
    @Story("PUT")
    @DisplayName("PUT /usuarios/{id} - altera usuário (200) e confirma via GET")
    void putAlteraUsuario() {
        String id = criarUsuario(emailUnico(), false);
        String novoEmail = emailUnico();
        var corpo = bodyUsuario(novoEmail, false);
        corpo.put("nome", "Nome Alterado");

        given().spec(spec).body(corpo)
                .when().put("/usuarios/{id}", id)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Registro alterado com sucesso"));

        given().spec(spec)
                .when().get("/usuarios/{id}", id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Nome Alterado"))
                .body("email", equalTo(novoEmail));
    }

    @Test
    @Story("PUT")
    @DisplayName("[-] PUT /usuarios/{id} com e-mail já usado por outro usuário - 400")
    void putEmailDuplicado() {
        String emailOutro = emailUnico();
        criarUsuario(emailOutro, false);
        String id = criarUsuario(emailUnico(), false);

        given().spec(spec).body(bodyUsuario(emailOutro, false))
                .when().put("/usuarios/{id}", id)
                .then()
                .statusCode(400)
                .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @Tag("bug")
    @Story("PUT")
    @DisplayName("[BUG] PUT /usuarios/{id} com ID inexistente- 404")
    void putIdInexistenteDeveRetornar404() {
        given().spec(spec).body(bodyUsuario(emailUnico(), false))
                .when().put("/usuarios/{id}", "idNovoDoTeste0001")
                .then()
                .statusCode(404)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @Story("DELETE")
    @DisplayName("DELETE /usuarios/{id} - 200 e usuário deixa de existir")
    void deleteUsuario() {
        String id = criarUsuario(emailUnico(), false);

        given().spec(spec)
                .when().delete("/usuarios/{id}", id)
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Registro excluído com sucesso"));

        given().spec(spec)
                .when().get("/usuarios/{id}", id)
                .then()
                .statusCode(400)
                .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @Story("DELETE")
    @Tag("bug")
    @DisplayName("[BUG] DELETE /usuarios/{id} inexistente - 404 ")
    void deleteInexistente() {
        given().spec(spec)
                .when().delete("/usuarios/{id}", "idQueNaoExiste999")
                .then()
                .statusCode(404);
    }
}
