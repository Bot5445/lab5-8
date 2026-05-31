package org.example.server.ioStorage;

import org.example.network.data.Person;
import org.example.network.data.PersonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Реализация интерфейса {@link IStorage} для работы с CSV-файлами.
 * Обеспечивает чтение и запись коллекции с использованием классов Scanner и BufferedOutputStream.
 * Корректно обрабатывает ошибки отсутствия файла и прав доступа.
 */
public class FileStorage implements IStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    private String fileName;

    /**
     * Устанавливает имя файла для хранилища.
     * Если имя не содержит расширения, автоматически добавляет ".csv".
     *
     * @param fileName имя файла (с путем или без)
     */
    public void setFileName(String fileName) {
        if (fileName != null && !fileName.contains(".")) {
            fileName = fileName + ".csv";
        }
        this.fileName = fileName;
    }

    public FileStorage() {
        this.fileName = "file.csv";
    }

    public FileStorage(String fileCSV) {
        this.fileName = fileCSV;
    }

    /**
     * Загружает коллекцию объектов Person из CSV-файла.
     * Парсит файл построчно, игнорируя пустые строки.
     * Ошибки парсинга логируются через Logback.
     *
     * @return список загруженных объектов Person
     * @throws IOException если файл не найден, нет прав доступа или произошла ошибка чтения
     */
    @Override
    public List<Person> load() throws IOException {
        File file = new File(fileName);

        if (!file.exists()) {
            throw new FileNotFoundException("Файл \"" + fileName + "\" не найден.");
        }
        if (!file.canRead()) {
            throw new AccessDeniedException("Нет прав на чтение файла \"" + fileName + "\".");
        }

        List<Person> people = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // ИСПРАВЛЕНО: используем стандартный split вместо OpenCSV
                // Аргумент -1 критически важен, чтобы не терять пустые ячейки (null) в конце строки!
                String[] parts = line.split(",", -1);

                try {
                    Person person = PersonFactory.createFromStringArray(parts);
                    people.add(person);
                } catch (IllegalArgumentException e) {
                    // ИСПРАВЛЕНО: логируем ошибку один раз через логгер
                    logger.warn("Пропущена невалидная строка в CSV: '{}'. Причина: {}", line, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода при чтении файла: {}", e.getMessage());
            throw e;
        }

        logger.info("Загружено {} элементов из файла '{}'.", people.size(), fileName);
        return people;
    }

    /**
     * Сохраняет коллекцию объектов Person в CSV-файл.
     * Запись производится с использованием буферизации (BufferedOutputStream).
     *
     * @param persons коллекция объектов для сохранения
     * @throws IOException если файл не найден, нет прав на запись или произошла ошибка ввода-вывода
     */
    @Override
    public void save(Collection<Person> persons) throws IOException {
        try (BufferedOutputStream bufferOut = new BufferedOutputStream(new FileOutputStream(fileName))) {
            for (Person entry : persons) {
                String row = entry.toString() + "\n";
                bufferOut.write(row.getBytes());
            }
        } catch (AccessDeniedException e) {
            logger.error("Ошибка доступа при сохранении: {}", e.getMessage());
            throw new IOException("Ошибка доступа: " + e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("Файл не найден для записи: {}", fileName);
            throw new IOException("Файл не найден или нет прав для записи: " + fileName);
        } catch (IOException e) {
            logger.error("Ошибка сохранения в файл '{}': {}", fileName, e.getMessage());
            throw new IOException("Ошибка сохранения в " + fileName + ": " + e.getMessage());
        }

        logger.info("Коллекция ({} элементов) успешно сохранена в файл '{}'.", persons.size(), fileName);
    }
}