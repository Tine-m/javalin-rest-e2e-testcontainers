package dk.ek;

import dk.ek.entities.Person;
import dk.ek.persistence.ConnectionPool;
import dk.ek.persistence.PersonMapper;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@Testcontainers
class HelloE2ERestTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static Javalin app;
    private static PersonMapper personMapper;

    @BeforeAll
    static void setUp() throws SQLException {
        DataSource dataSource = ConnectionPool.create(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        createSchema(dataSource);

        personMapper = new PersonMapper(dataSource);

        app = Application.create(personMapper);
        app.start(0);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    private static void createSchema(DataSource dataSource) throws SQLException {
        String sql = """
                CREATE TABLE person (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL
                )
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    @AfterEach
    void cleanDatabase() {
        personMapper.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void shouldReturnGreetingWhenPersonExists() {
        Person peter = new Person("Peter", "Pan");
        personMapper.create(peter);

        given()
        .when()
            .get("/hello/Pan")
        .then()
            .statusCode(200)
            .body(containsString("Hello Peter Pan!"));
    }

    @Test
    void shouldReturnUnknownMessageWhenPersonDoesNotExist() {
        given()
        .when()
            .get("/hello/Hook")
        .then()
            .statusCode(200)
            .body(containsString("Who is this 'Hook' you're talking about?"));
    }
}
