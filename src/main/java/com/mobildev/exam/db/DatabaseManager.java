package com.mobildev.exam.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());


    // The single, static instance of the HikariCP data source.
    private static final HikariDataSource dataSource;

    // Static block to initialize the connection pool when the class is loaded.
    static {
        // We read configuration from a configuration file in a real-world application.
        String dbUrl = "jdbc:mysql://localhost:3306/online_exam_db";
        String user = "root";
        String pass = "root";

        // Create the configuration object for HikariCP.
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(user);
        config.setPassword(pass);

        // Configure the pool size. A good starting point is usually
        // based on the number of CPU cores and expected concurrency.
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setPoolName("ExamApp-Pool");

        // Set up other useful properties for performance and reliability.
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP connection pool has been shut down gracefully.");
        }
    }
}