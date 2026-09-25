# Javalin REST API E2E test med Testcontainers

Et lille undervisningseksempel på en REST API end-to-end-test med:

- Java 21
- Maven
- Javalin
- PostgreSQL
- JDBC
- HikariCP
- JUnit 5
- Rest Assured
- Testcontainers

## Hvad tester vi?

Testen går gennem hele backend-kæden:

```text
Rest Assured
    |
    | HTTP GET /hello/Pan
    v
Javalin
    |
PersonController
    |
PersonMapper
    |
PostgreSQL (Testcontainer)
```

PostgreSQL bliver altså **ikke mocked**.

## Forudsætninger

Du skal have:

1. Java 21
2. Maven (eller bruge Maven fra IntelliJ)
3. Docker Desktop / en fungerende Docker-installation

Du behøver ikke selv oprette eller starte en PostgreSQL-database.

## Kør i IntelliJ

Åbn projektets `pom.xml` som Maven-projekt.

Sørg for, at Docker kører.

Åbn:

`src/test/java/dk/ek/HelloE2ERestTest.java`

og kør testklassen.

Første gang henter Testcontainers PostgreSQL Docker-imaget.

## Hvad sker der?

JUnit starter testen.

Testcontainers starter automatisk en PostgreSQL-container og giver Java-koden JDBC URL, brugernavn og password.

Testens `setUp()`:

1. opretter en connection pool
2. opretter tabellen `person`
3. opretter `PersonMapper`
4. starter Javalin på en ledig port
5. fortæller Rest Assured, hvilken port Javalin bruger

Testen indsætter derefter Peter Pan direkte gennem `PersonMapper`.

Rest Assured kalder derefter REST-endpointet:

`GET /hello/Pan`

Requesten går gennem Javalin og `PersonController`, som slår Peter Pan op i den rigtige PostgreSQL-database.

Efter hver test slettes testdata.

Når testsuiten er færdig, stopper Javalin, og Testcontainers fjerner PostgreSQL-containeren automatisk.

## Kør fra terminal

```bash
mvn test
```

## Centrale test

```java
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
```

Bemærk forskellen mellem **setup** og det, der faktisk testes:

- Testdata oprettes direkte via mapperen.
- Selve systemet kaldes gennem HTTP.
- Controller og persistence-lag bruges af applikationen.
- PostgreSQL er en rigtig database i en midlertidig container.

Det gør eksemplet velegnet til at diskutere integrationstest kontra REST API end-to-end-test.
