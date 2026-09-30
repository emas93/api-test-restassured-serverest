package br.com.qa.tarefa1;

import br.com.qa.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@Epic("ServeRest API")
@Feature("Login Básico")
@Tag("tarefa1")
public class LoginTest extends BaseTest {


    @Test
    @Story("Login")
    @Feature("Login Básico")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[+] POST /login com credenciais válidas - deve retornar 200 e token")
    void loginValido() {
        String email = emailUnico();
        criarUsuario(email, false);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", "senha123");

        given().spec(spec).body(body)
                .when().post("/login")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", startsWith("Bearer "));
    }

    @Test
    @Story("Login")
    @DisplayName("[-] POST /login com senha errada retorna 401")
    void loginSenhaInvalida() {
        String email = emailUnico();
        criarUsuario(email, false);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", "senha_errada");

        given().spec(spec).body(body)
                .when().post("/login")
                .then()
                .statusCode(401)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Email e/ou senha inválidos"))
                .body("$", not(hasKey("authorization")));
    }

    @Test
    @Story("Login")
    @DisplayName("[-] POST /login sem campos obrigatórios retorna 400")
    void loginSemCampos() {
        given().spec(spec).body(new HashMap<>())
                .when().post("/login")
                .then()
                .statusCode(400)
                .body("email", equalTo("email é obrigatório"))
                .body("password", equalTo("password é obrigatório"));
    }


}
