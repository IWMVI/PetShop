package br.iwmvi.petshop.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return;
        }

        ConnectionInfo connectionInfo = parse(url);
        if (connectionInfo == null || connectionInfo.database().isEmpty() || "postgres".equals(connectionInfo.database())) {
            return;
        }

        String username = environment.getProperty("spring.datasource.username", "postgres");
        String password = environment.getProperty("spring.datasource.password", "");

        String maintenanceUrl = "jdbc:postgresql://" + connectionInfo.host() + ":" + connectionInfo.port() + "/postgres";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.warn("Failed to load PostgreSQL driver, skipping automatic database creation", e);
            return;
        }

        try (Connection connection = DriverManager.getConnection(maintenanceUrl, username, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + connectionInfo.database() + "'")) {
                if (resultSet.next()) {
                    return;
                }
            }
            statement.executeUpdate("CREATE DATABASE \"" + connectionInfo.database() + "\"");
            log.info("Database '{}' created successfully", connectionInfo.database());
        } catch (SQLException e) {
            log.warn("Failed to create database '{}' automatically: {}", connectionInfo.database(), e.getMessage());
        }
    }

    private ConnectionInfo parse(String url) {
        String rest = url.substring("jdbc:postgresql://".length());
        int queryStart = rest.indexOf('?');
        if (queryStart >= 0) {
            rest = rest.substring(0, queryStart);
        }
        try {
            URI uri = new URI("postgresql://" + rest);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            String database = path == null || path.isBlank() ? "" : path.substring(1);
            return new ConnectionInfo(host, port, database);
        } catch (URISyntaxException e) {
            log.warn("Failed to parse datasource URL '{}': {}", url, e.getMessage());
            return null;
        }
    }

    private record ConnectionInfo(String host, int port, String database) {
    }
}