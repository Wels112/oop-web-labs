package edu.java.issues.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.exception.StorageException;
import edu.java.issues.model.Issue;

/**
 * Сохранение и загрузка списка выдач из текстового файла (ООП,
 * лабораторная работа № 5). Каждая запись занимает пять подряд идущих строк
 * файла: читатель, книга, дата выдачи, срок возврата, отметка о возврате.
 * Используются буферизованные символьные потоки в кодировке UTF-8.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class TextIssueStorage {

	/** Журнал работы с файлами (лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(TextIssueStorage.class);

	/**
	 * Записывает записи в текстовый файл.
	 *
	 * @param fileName полное имя файла
	 * @param issues   записи, которые нужно сохранить
	 * @throws StorageException если файл не удалось записать
	 */
	public void save(String fileName, List<Issue> issues)
			throws StorageException {
		try (BufferedWriter writer = new BufferedWriter(
				new FileWriter(fileName, StandardCharsets.UTF_8))) {
			for (Issue issue : issues) {
				for (String cell : issue.toRow()) {
					writer.write(cell);
					writer.write("\n");
				}
			}
			log.info("Записан текстовый файл {}: {} записей", fileName,
					issues.size());
		} catch (IOException e) {
			log.error("Ошибка записи файла {}", fileName, e);
			throw new StorageException("Ошибка записи в файл: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Читает записи из текстового файла.
	 *
	 * @param fileName полное имя файла
	 * @return прочитанные записи
	 * @throws StorageException если файл не найден или не читается
	 */
	public List<Issue> load(String fileName) throws StorageException {
		List<Issue> result = new ArrayList<>();
		try (BufferedReader input = new BufferedReader(
				new FileReader(fileName, StandardCharsets.UTF_8))) {
			String reader;
			while ((reader = input.readLine()) != null) {
				if (reader.isBlank()) {
					continue;
				}
				String book = input.readLine();
				String issued = input.readLine();
				String due = input.readLine();
				String returned = input.readLine();
				result.add(new Issue(reader, book, issued, due, returned));
			}
			log.info("Прочитан текстовый файл {}: {} записей", fileName,
					result.size());
			return result;
		} catch (FileNotFoundException e) {
			log.error("Файл не найден: {}", fileName, e);
			throw new StorageException("Файл не найден: " + fileName, e);
		} catch (IOException e) {
			log.error("Ошибка чтения файла {}", fileName, e);
			throw new StorageException("Ошибка чтения файла: " + e.getMessage(),
					e);
		}
	}
}
