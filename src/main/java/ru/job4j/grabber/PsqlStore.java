package ru.job4j.grabber;

import ru.job4j.model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     connection.prepareStatement("insert into post (title, description, link, created) "
                             + "values (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            Timestamp timestamp = Timestamp.valueOf(post.getCreated());
            statement.setTimestamp(4, timestamp);
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        return find("select * from post;");
    }

    @Override
    public Post findById(int id) {
        List<Post> posts = find(String.format("select * from post where id = %s", id));
        return !posts.isEmpty() ? posts.get(0) : null;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private List<Post> find(String string) {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(string)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getString("link"),
                            resultSet.getInt("id"),
                            resultSet.getTimestamp("created").toLocalDateTime()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}