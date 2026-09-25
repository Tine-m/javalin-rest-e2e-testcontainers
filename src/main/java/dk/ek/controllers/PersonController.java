package dk.ek.controllers;

import dk.ek.entities.Person;
import dk.ek.persistence.PersonMapper;
import io.javalin.Javalin;

import java.util.Optional;

public class PersonController {

    public static void addRoutes(Javalin app, PersonMapper personMapper) {
        app.get("/hello/{lastName}", ctx -> {
            String lastName = ctx.pathParam("lastName");

            Optional<Person> foundPerson = personMapper.findByLastName(lastName);

            String response = foundPerson
                    .map(person -> String.format(
                            "Hello %s %s!",
                            person.getFirstName(),
                            person.getLastName()))
                    .orElse(String.format(
                            "Who is this '%s' you're talking about?",
                            lastName));

            ctx.result(response);
        });
    }
}
