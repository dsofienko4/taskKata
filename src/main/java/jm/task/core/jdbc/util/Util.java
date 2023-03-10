package jm.task.core.jdbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Util {
    private static Connection conn = null;
    private static Util instance = null;

    /**
     * Приватный конструктор, который устанавливает соединение с базой данных,
     * используя настройки, указанные в конфигурационном файле database.properties.
     * Если соединение уже установлено, конструктор не выполняет никаких действий.
     * В случае возникновения исключения во время установки соединения, стек
     * исключения будет напечатан в стандартный вывод ошибок.
     */
    private Util() {
        try {
            if (null == conn || conn.isClosed()) {
                Properties props = getProps();
                conn = DriverManager.getConnection(
                        props.getProperty("db.url"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password")
                );
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает единственный экземпляр класса Util, используя ленивую
     * инициализацию, что означает, что экземпляр класса будет создан только
     * при первом вызове этого метода. Последующие вызовы будут возвращать
     * существующий экземпляр.
     */
    public static Util getInstance() {
        if (null == instance) {
            instance = new Util();
        }
        return instance;
    }

    /**
     * Возвращает текущее соединение с базой данных. Если соединение еще не
     * установлено, вызывается приватный конструктор для установки соединения.
     * @return текущее соединение с базой данных
     */
    public Connection getConnection() {
        if (null == conn) {
            instance = new Util();
        }
        return conn;
    }

    /**
     * Читает конфигурационный файл database.properties из ресурсов приложения
     * и возвращает его в виде объекта Properties.
     * @return объект Properties, содержащий настройки базы данных
     * @throws IOException если конфигурационный файл не найден
     */
    private static Properties getProps() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(
                Paths.get(Util.class.getResource("/database.properties").toURI()))) {
            props.load(in);
            return props;
        } catch (IOException | URISyntaxException e) {
            throw new IOException("Database config file not found", e);
        }
    }
}