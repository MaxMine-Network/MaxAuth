package ru.themrliamt.auth.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.maxmind.geoip2.WebServiceClient;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import lombok.Builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class MySQL {

    private final ExecutorService QUERY_EXECUTOR = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("database-thread #%s")
                    .setDaemon(true)
                    .build());

    private Connection connection;
    private final MysqlDataSource dataSource = new MysqlDataSource();

    @Builder(builderMethodName = "newBuilder", builderClassName = "MySqlBuilder", buildMethodName = "create")
    public MySQL(String host, String user, String password, String database) {
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(host);
        dataSource.setDatabaseName(database);
        dataSource.setPort(3306);
        try {
            dataSource.setAutoReconnect(true);
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void refreshConnection() {
        try {
            if (connection != null && !connection.isClosed() && connection.isValid(1000)) {
                return;
            }

            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Все нахуй обосралось с MySql");
        }
    }

    public <T> T executeQuery(String sql, ResponseHandler<ResultSet, T> handler, Object... objects) {
        Callable<T> callable = () -> {
            try (PreparedStatement preparedStatement = createStatement(sql, PreparedStatement.NO_GENERATED_KEYS, objects)) {
                refreshConnection();
                ResultSet rs = preparedStatement.executeQuery();
                return handler.handleResponse(rs);
            } catch (Exception e) {
                return null;
            }
        };

        Future<T> future = asyncTask(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int execute(String sql, Object... objects) {
        Callable<Integer> callable = () -> {
            try (PreparedStatement ps = createStatement(sql, PreparedStatement.NO_GENERATED_KEYS, objects)) {
                refreshConnection();
                ps.execute();
                return 1;
            } catch (Exception e) {
                return null;
            }
        };

        Future<Integer> future = asyncTask(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private PreparedStatement createStatement(String query, int generatedKeys, Object... objects) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(query, generatedKeys);

        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                ps.setObject(i + 1, objects[i]);
            }
        }
        return ps;
    }

    private <T> Future<T> asyncTask(Callable<T> callable) {
        return QUERY_EXECUTOR.submit(callable);
    }

}
