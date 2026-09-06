package vn.edu.eaut.library.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/cnj25_library?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Doanmanh";

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("CNJ25LibraryPool");
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo connection pool: " + e.getMessage(), e);
        }
    }

    private DBConnection() {
        // ngăn khởi tạo instance
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                    // bỏ qua lỗi khi đóng tài nguyên
                }
            }
        }
    }
}
