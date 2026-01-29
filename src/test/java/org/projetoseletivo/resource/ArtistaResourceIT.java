package org.projetoseletivo.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.ArtistaRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@QuarkusTest
class ArtistaResourceIT {

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveListarArtistas() {
        given()
                .when()
                .get("/v1/artistas")
                .then()
                .statusCode(200)
                .body("conteudo", notNullValue())
                .body("totalElementos", greaterThanOrEqualTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveListarArtistasComPaginacao() {
        given()
                .queryParam("pagina", 0)
                .queryParam("tamanho", 5)
                .queryParam("ordem", "ASC")
                .when()
                .get("/v1/artistas")
                .then()
                .statusCode(200)
                .body("tamanhoPagina", equalTo(5))
                .body("pagina", equalTo(0));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveFiltrarPorTipo() {
        given()
                .queryParam("tipo", "BANDA")
                .when()
                .get("/v1/artistas")
                .then()
                .statusCode(200);
    }

    @Test
    void deveRetornar401SemAutenticacao() {
        given()
                .when()
                .get("/v1/artistas")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveCriarArtista() {
        ArtistaRequest request = ArtistaRequest.builder()
                .nome("Novo Artista Test")
                .tipo(TipoArtista.SOLO)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/artistas")
                .then()
                .statusCode(201)
                .body("nome", equalTo("Novo Artista Test"))
                .body("tipo", equalTo("SOLO"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveRetornar404ParaArtistaInexistente() {
        given()
                .when()
                .get("/v1/artistas/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void deveListarArtistasComOrdenacaoDesc() {
        given()
                .queryParam("ordem", "DESC")
                .when()
                .get("/v1/artistas")
                .then()
                .statusCode(200);
    }
}
