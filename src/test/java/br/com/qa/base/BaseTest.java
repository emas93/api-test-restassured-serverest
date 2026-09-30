package br.com.qa.base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class BaseTest {

    protected static RequestSpecification spec;

    @BeforeAll
    static void configurar() {
        RestAssured.baseURI = System.getProperty("base.url", "https://serverest.dev");
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }


    protected static String emailUnico() {
        return "qa_" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    protected static Map<String, Object> bodyUsuario(String email, boolean admin) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", "Usuario QA");
        body.put("email", email);
        body.put("password", "senha123");
        body.put("administrador", String.valueOf(admin));
        return body;
    }

    protected static Map<String, Object> bodyProduto(String nome) {
        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("preco", 470);
        body.put("descricao", "Produto de teste");
        body.put("quantidade", 381);
        return body;
    }


    protected static String criarUsuario(String email, boolean admin) {
        return given().spec(spec).body(bodyUsuario(email, admin))
                .when().post("/usuarios")
                .then().statusCode(201)
                .extract().path("_id");
    }

    protected static String login(String email, String senha) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", senha);
        return given().spec(spec).body(body)
                .when().post("/login")
                .then().statusCode(200)
                .extract().path("authorization");
    }


    protected static String tokenAdmin() {
        String email = emailUnico();
        criarUsuario(email, true);
        return login(email, "senha123");
    }


    protected static String criarProduto(String token, String nome) {
        return given().spec(spec).header("Authorization", token).body(bodyProduto(nome))
                .when().post("/produtos")
                .then().statusCode(201)
                .extract().path("_id");
    }
}
