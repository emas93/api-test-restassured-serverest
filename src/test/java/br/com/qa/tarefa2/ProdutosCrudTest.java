package br.com.qa.tarefa2;

import br.com.qa.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@Epic("ServeRest API")
@Feature("Tarefa 2 - Produtos CRUD - Cenários positivos e negativos")
@Tag("tarefa2")
class ProdutosCrudTest extends BaseTest {

    private static String token;

    @BeforeEach
    void autenticar() {
        if (token == null) {
            token = tokenAdmin();
        }
    }

    private static String nomeUnico() {
        return "Produto " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @Story("GET")
    @DisplayName("GET /produtos - 200, header e lista")
    void listarProdutos() {
        given().spec(spec)
        .when().get("/produtos")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body("quantidade", greaterThanOrEqualTo(0))
            .body("produtos", instanceOf(List.class));
    }

    @Test
    @Story("GET")
    @DisplayName("GET /produtos/{id} - 200 e dados do produto")
    void buscarProduto() {
        String nome = nomeUnico();
        String id = criarProduto(token, nome);

        given().spec(spec)
        .when().get("/produtos/{id}", id)
        .then()
            .statusCode(200)
            .body("_id", equalTo(id))
            .body("nome", equalTo(nome))
            .body("preco", equalTo(470))
            .body("quantidade", equalTo(381));
    }

    @Test
    @Story("GET")
    @DisplayName("[BUG] GET /produtos/{id} inexistente - 404")
    void buscarProdutoInexistente() {
        given().spec(spec)
        .when().get("/produtos/{id}", "idInexistente123")
        .then()
            .statusCode(404)
            .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    @Story("POST")
    @DisplayName("POST /produtos com token de admin - 201")
    void criarProdutoComToken() {
        given().spec(spec).header("Authorization", token).body(bodyProduto(nomeUnico()))
        .when().post("/produtos")
        .then()
            .statusCode(201)
            .header("Content-Type", containsString("application/json"))
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .body("_id", notNullValue());
    }

    @Test
    @Story("POST")
    @DisplayName("[-] POST /produtos sem token - 401")
    void criarProdutoSemToken() {
        given().spec(spec).body(bodyProduto(nomeUnico()))
        .when().post("/produtos")
        .then()
            .statusCode(401)
            .body("message", equalTo(
                "Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Story("POST")
    @DisplayName("[-] POST /produtos com nome duplicado - 400")
    void criarProdutoDuplicado() {
        String nome = nomeUnico();
        criarProduto(token, nome);

        given().spec(spec).header("Authorization", token).body(bodyProduto(nome))
        .when().post("/produtos")
        .then()
            .statusCode(400)
            .body("message", equalTo("Já existe produto com esse nome"));
    }

    @Test
    @Story("POST")
    @DisplayName("[-] POST /produtos por usuário não administrador - 403")
    void criarProdutoNaoAdmin() {
        String email = emailUnico();
        criarUsuario(email, false);
        String tokenComum = login(email, "senha123");

        given().spec(spec).header("Authorization", tokenComum).body(bodyProduto(nomeUnico()))
        .when().post("/produtos")
        .then()
            .statusCode(403)
            .body("message", equalTo("Rota exclusiva para administradores"));
    }


    @Test
    @Story("PUT")
    @DisplayName("PUT /produtos/{id} - 200 e alteração refletida no GET")
    void alterarProduto() {
        String id = criarProduto(token, nomeUnico());
        String novoNome = nomeUnico();
        var corpo = bodyProduto(novoNome);
        corpo.put("preco", 999);

        given().spec(spec).header("Authorization", token).body(corpo)
        .when().put("/produtos/{id}", id)
        .then()
            .statusCode(200)
            .body("message", equalTo("Registro alterado com sucesso"));

        given().spec(spec)
        .when().get("/produtos/{id}", id)
        .then()
            .statusCode(200)
            .body("nome", equalTo(novoNome))
            .body("preco", equalTo(999));
    }

    @Test
    @Story("PUT")
    @DisplayName("[-] PUT /produtos/{id} sem token - 401")
    void alterarProdutoSemToken() {
        String id = criarProduto(token, nomeUnico());

        given().spec(spec).body(bodyProduto(nomeUnico()))
        .when().put("/produtos/{id}", id)
        .then()
            .statusCode(401);
    }


    @Test
    @Story("DELETE")
    @DisplayName("DELETE /produtos/{id} - 200 e produto deixa de existir")
    void excluirProduto() {
        String id = criarProduto(token, nomeUnico());

        given().spec(spec).header("Authorization", token)
        .when().delete("/produtos/{id}", id)
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body("message", equalTo("Registro excluído com sucesso"));

        given().spec(spec)
        .when().get("/produtos/{id}", id)
        .then()
            .statusCode(400)
            .body("message", equalTo("Produto não encontrado"));
    }

    @Test
    @Story("DELETE")
    @DisplayName("[-] DELETE /produtos/{id} sem token - 401")
    void excluirProdutoSemToken() {
        String id = criarProduto(token, nomeUnico());

        given().spec(spec)
        .when().delete("/produtos/{id}", id)
        .then()
            .statusCode(401);
    }
}
