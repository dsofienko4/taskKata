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



import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
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


    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        // проверяем, была ли уже создана фабрика сессий. Если нет, то создаём её с помощью конфигурации
        if (sessionFactory == null) {
            // создаём объект конфигурации
            Configuration configuration = new Configuration();
            // добавляем класс-сущность для маппинга
            configuration.addAnnotatedClass(User.class);
            // задаём настройки соединения с БД
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/taskKata");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "root");
            // задаём диалект СУБД для Hibernate
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
            // задаём режим автоматического создания и удаления таблиц при старте и остановке приложения
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            // задаём вывод SQL-запросов в консоль
            configuration.setProperty("hibernate.show_sql", "true");
            // форматируем вывод SQL-запросов для удобства чтения
            configuration.setProperty("hibernate.format_sql", "true");
            // создаём объект для настройки реестра сервисов Hibernate
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            // создаём фабрику сессий на основе настроек конфигурации и реестра сервисов Hibernate
            sessionFactory = configuration.buildSessionFactory(builder.build());
        }
        // возвращаем фабрику сессий
        return sessionFactory;
    }
}