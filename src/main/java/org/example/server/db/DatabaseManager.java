package org.example.server.db;

import org.example.network.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final String url = "jdbc:postgresql://pg:5432/studs";
    private final String host = "pg";
    private final int port = 5432;
    private final String database = "studs";

    private final String user;
    private final String password;

    public DatabaseManager() {
        // 1. Пытаемся прочитать логин и пароль из .pgpass (как сказал преподаватель)
        String[] credentials = loadCredentialsFromPgPass(host, port, database);

        if (!credentials[0].isEmpty()) {
            this.user = credentials[0];
            this.password = credentials[1];
            logger.info("Учетные данные успешно загружены из .pgpass для пользователя: {}", this.user);
        } else {
            // 2. Фоллбэк: если .pgpass не найден (например, при запуске на локальном Windows)
            this.user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "s505192";
            this.password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";
            logger.warn("⚠Файл .pgpass не найден. Используются переменные окружения DB_USER/DB_PASSWORD.");
        }

        try {
            Class.forName("org.postgresql.Driver");
            logger.info("Драйвер PostgreSQL загружен.");
        } catch (ClassNotFoundException e) {
            logger.error("Драйвер PostgreSQL не найден!", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * для чтения .pgpass
     * @param host
     * @param port
     * @param database
     * @return
     */
    private static String[] loadCredentialsFromPgPass(String host, int port, String database) {
        String pgPassPath = firstNonBlank(
                System.getenv("PGPASSFILE"),
                Path.of(System.getProperty("user.home"), ".pgpass").toString()
        );

        if (pgPassPath == null) {
            return new String[]{"", ""};
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(pgPassPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split(":", 5);
                if (parts.length != 5) {
                    continue;
                }

                if (!fieldMatches(parts[0], host)
                        || !fieldMatches(parts[1], String.valueOf(port))
                        || !fieldMatches(parts[2], database)) {
                    continue;
                }

                // parts[3] - это логин, parts[4] - это пароль
                return new String[]{parts[3], parts[4]};
            }
        } catch (IOException exception) {
            // Файл не найден или нет прав на чтение
        }

        return new String[]{"", ""};
    }

    private static String firstNonBlank(String... strings) {
        for (String s : strings) {
            if (s != null && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private static boolean fieldMatches(String pattern, String value) {
        return "*".equals(pattern) || pattern.equalsIgnoreCase(value);
    }

    /**
     * Хеширование пароля алгоритмом SHA-384
     * @param password пароль для шифрования
     * @return зашифрованный пароль
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-384 не найден", e);
        }
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            logger.info("Пользователь {} успешно зарегистрирован.", username);
            return true;
        } catch (SQLException e) {
            logger.warn("Ошибка регистрации пользователя {}: {}", username, e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash").equals(hashPassword(password));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка аутентификации: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Загружает все объекты {@link Person} из базы данных в оперативную память при старте сервера.
     * <p>Объекты извлекаются из таблицы {@code persons} и сортируются по ID.
     * Поля, которые могут быть {@code NULL} в БД (например, Enum-значения nationality и hairColor),
     * обрабатываются безопасно, чтобы избежать {@link NullPointerException}.</p>
     *
     * @return список загруженных объектов {@link Person}. Если таблица пуста или произошла ошибка БД,
     *         возвращается пустой список.
     */
    public List<Person> loadAllPersons() {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM persons ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 1. Безопасная загрузка Enum-полей (защита от NullPointerException)
                String nationalityStr = rs.getString("nationality");
                Country nationality = (nationalityStr != null && !nationalityStr.isBlank())
                        ? Country.valueOf(nationalityStr.toUpperCase())
                        : null;

                String hairColorStr = rs.getString("hair_color");
                Color hairColor = (hairColorStr != null && !hairColorStr.isBlank())
                        ? Color.valueOf(hairColorStr.toUpperCase())
                        : null;

                // 2. Создание объекта через основной конструктор (id, name, coordinates, height, location)
                Person p = new Person(
                        rs.getInt("id"),
                        rs.getString("name"),
                        new Coordinates(rs.getFloat("coord_x"), rs.getFloat("coord_y")),
                        rs.getLong("height"),
                        new Location(rs.getLong("loc_x"), rs.getDouble("loc_y"))
                );

                // 3. Выставляем остальные поля через сеттеры
                p.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());

                p.setPassportID(rs.getString("passport_id")); // rs.getString сам вернет null, если в БД NULL
                p.setNationality(nationality);
                p.setHairColor(hairColor);

                // 4. Дополнительные поля
                p.getLocation().setName(rs.getString("loc_name"));
                p.setOwner(rs.getString("owner")); // Требует наличия поля owner и @Setter в классе Person!

                persons.add(p);
            }
            logger.info("Загружено {} объектов из БД.", persons.size());
        } catch (SQLException e) {
            logger.error("Ошибка загрузки коллекции: {}", e.getMessage());
        }
        return persons;
    }

    // Вставка объекта. Возвращает сохраненный объект с присвоенным ID из sequence, или null при ошибке
    public Person insertPerson(Person person) {

        String sql = "INSERT INTO persons (name, coord_x, coord_y, creation_date, height, passport_id, hair_color, nationality, loc_x, loc_y, loc_name, owner) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::color_enum, ?::country_enum, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, person.getName());
            pstmt.setFloat(2, person.getCoordinates().getX());
            pstmt.setFloat(3, person.getCoordinates().getY());
            pstmt.setTimestamp(4, Timestamp.valueOf(person.getCreationDate()));
            pstmt.setLong(5, person.getHeight());
            pstmt.setString(6, person.getPassportID());
            pstmt.setString(7, person.getHairColor() != null ? person.getHairColor().name() : null);
            pstmt.setString(8, person.getNationality() != null ? person.getNationality().name() : null);
            pstmt.setLong(9, person.getLocation().getX());
            pstmt.setDouble(10, person.getLocation().getY());
            pstmt.setString(11, person.getLocation().getName());
            pstmt.setString(12, person.getOwner());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt("id");
                    person.setId(generatedId);
                    logger.info("Объект успешно сохранен в БД с ID: {}", generatedId);
                    return person;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка сохранения объекта в БД: {}", e.getMessage());
        }
        return null;
    }

    public boolean deletePerson(int id, String owner) {
        String sql = "DELETE FROM persons WHERE id = ? AND owner = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, owner);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Ошибка удаления объекта: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Обновляет объект Person в базе данных.
     * Обновление произойдет только если объект принадлежит указанному владельцу.
     *
     * @param person объект с обновленными данными (должен содержать валидные id и owner)
     * @return true, если обновление прошло успешно (затронуло 1 строку), false иначе
     */
    public boolean updatePerson(Person person) {
        String sql = "UPDATE persons SET name=?, coord_x=?, coord_y=?, creation_date=?, height=?, " +
                "passport_id=?, hair_color=?::color_enum, nationality=?::country_enum, loc_x=?, loc_y=?, loc_name=? " +
                "WHERE id=? AND owner=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, person.getName());
            pstmt.setFloat(2, person.getCoordinates().getX());
            pstmt.setFloat(3, person.getCoordinates().getY());
            pstmt.setTimestamp(4, Timestamp.valueOf(person.getCreationDate()));
            pstmt.setLong(5, person.getHeight());
            pstmt.setString(6, person.getPassportID());
            pstmt.setString(7, person.getHairColor() != null ? person.getHairColor().name() : null);
            pstmt.setString(8, person.getNationality() != null ? person.getNationality().name() : null);
            pstmt.setLong(9, person.getLocation().getX());
            pstmt.setDouble(10, person.getLocation().getY());
            pstmt.setString(11, person.getLocation().getName());

            pstmt.setInt(12, person.getId());
            pstmt.setString(13, person.getOwner());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Объект с ID {} успешно обновлен в БД.", person.getId());
                return true;
            } else {
                logger.warn("Не удалось обновить объект с ID {}. Возможно, нет прав (неверный owner).", person.getId());
            }
        } catch (SQLException e) {
            logger.error("Ошибка обновления объекта с ID {} в БД: {}", person.getId(), e.getMessage());
        }
        return false;
    }
}