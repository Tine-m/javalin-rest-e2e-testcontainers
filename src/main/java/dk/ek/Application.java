package dk.ek;

import dk.ek.controllers.PersonController;
import dk.ek.persistence.PersonMapper;
import io.javalin.Javalin;

public class Application {

    public static Javalin create(PersonMapper personMapper) {
        Javalin app = Javalin.create();
        PersonController.addRoutes(app, personMapper);
        return app;
    }
}
