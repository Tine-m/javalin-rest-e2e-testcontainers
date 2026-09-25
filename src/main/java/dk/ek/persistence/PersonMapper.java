package dk.ek.persistence;

import dk.ek.entities.Person;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class PersonMapper {

    private final DataSource dataSource;

    public PersonMapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Person create(Person person) {
        String sql = """
                INSERT INTO person (first_name, last_name)
                VALUES (?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Person(
                            rs.getInt(1),
                            person.getFirstName(),
                            person.getLastName()
                    );
                }
            }

            throw new RuntimeException("Person was inserted, but no generated id was returned.");

        } catch (SQLException e) {
            throw new RuntimeException("Could not create person", e);
        }
    }

    public Optional<Person> findByLastName(String lastName) {
        String sql = """
                SELECT id, first_name, last_name
                FROM person
                WHERE last_name = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, lastName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Person(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    ));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Could not find person", e);
        }
    }

    public void deleteAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM person")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete persons", e);
        }
    }
}
