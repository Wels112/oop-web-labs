package edu.java.issues.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.exception.EmptyQueryException;
import edu.java.issues.exception.ShortNameException;

/**
 * Общее хранилище записей о выдачах. Один и тот же экземпляр использует
 * окно Swing (ООП) и веб-приложение (Web), поэтому правка в одном
 * интерфейсе сразу видна в другом. Методы синхронизированы, так как
 * к хранилищу обращаются несколько потоков: поток интерфейса, потоки Tomcat
 * и потоки лабораторной работы № 8.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueRepository {

	/** Значение условия поиска «читатель не выбран». */
	public static final String ALL_READERS = "Все читатели";

	/** Журнал работы хранилища (ООП, лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(IssueRepository.class);

	/** Единственный экземпляр хранилища. */
	private static final IssueRepository INSTANCE = new IssueRepository();

	/** Читатели, из которых выбирают в выпадающих списках. */
	private static final List<String> READERS = List.of("Иванов Пётр",
			"Смирнова Анна", "Кузнецов Илья");

	/** Записи о выдачах. */
	private final List<Issue> issues = new ArrayList<>();

	/** Слушатели изменений данных: окно Swing подписывается на них. */
	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

	/** Создаёт пустое хранилище; публичный конструктор нужен для тестов. */
	public IssueRepository() {
	}

	/**
	 * Возвращает общий экземпляр хранилища.
	 *
	 * @return общее хранилище приложения
	 */
	public static IssueRepository getInstance() {
		return INSTANCE;
	}

	/**
	 * Возвращает список читателей картотеки.
	 *
	 * @return неизменяемый список имён читателей
	 */
	public static List<String> readers() {
		return READERS;
	}

	/**
	 * Подписывает слушателя на любые изменения данных.
	 *
	 * @param listener действие, которое вызывается после изменения
	 */
	public void addChangeListener(Runnable listener) {
		listeners.add(listener);
	}

	/** Сообщает слушателям об изменении данных. */
	private void fireChanged() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

	/**
	 * Возвращает копию всех записей.
	 *
	 * @return неизменяемая копия списка выдач
	 */
	public synchronized List<Issue> all() {
		return Collections.unmodifiableList(new ArrayList<>(issues));
	}

	/**
	 * Возвращает число записей.
	 *
	 * @return число выдач в картотеке
	 */
	public synchronized int size() {
		return issues.size();
	}

	/**
	 * Заменяет все записи переданными (загрузка из файла).
	 *
	 * @param newIssues новые записи
	 */
	public void replaceAll(List<Issue> newIssues) {
		synchronized (this) {
			issues.clear();
			issues.addAll(newIssues);
		}
		log.info("Данные заменены, записей: {}", newIssues.size());
		fireChanged();
	}

	/**
	 * Добавляет запись.
	 *
	 * @param issue новая запись
	 */
	public void add(Issue issue) {
		synchronized (this) {
			issues.add(issue);
		}
		log.info("Добавлена выдача: {}", issue);
		fireChanged();
	}

	/**
	 * Заменяет запись с указанным номером.
	 *
	 * @param index номер записи, начиная с нуля
	 * @param issue новое значение записи
	 * @throws IndexOutOfBoundsException если записи с таким номером нет
	 */
	public void update(int index, Issue issue) {
		synchronized (this) {
			issues.set(index, issue);
		}
		log.info("Изменена выдача № {}: {}", index, issue);
		fireChanged();
	}

	/**
	 * Удаляет запись с указанным номером.
	 *
	 * @param index номер записи, начиная с нуля
	 * @throws IndexOutOfBoundsException если записи с таким номером нет
	 */
	public void remove(int index) {
		Issue removed;
		synchronized (this) {
			removed = issues.remove(index);
		}
		log.info("Удалена выдача: {}", removed);
		fireChanged();
	}

	/**
	 * Проверяет условия поиска (ООП, лабораторная работа № 4).
	 *
	 * @param reader имя читателя или «Все читатели»
	 * @param book   часть названия книги
	 * @throws EmptyQueryException если не задано ни одно условие отбора
	 * @throws ShortNameException  если название книги состоит из одного
	 *                             символа
	 */
	public static void checkQuery(String reader, String book)
			throws EmptyQueryException, ShortNameException {
		String name = book == null ? "" : book.trim();
		boolean noReader = reader == null || reader.isBlank()
				|| ALL_READERS.equals(reader);
		if (noReader && name.isEmpty()) {
			throw new EmptyQueryException();
		}
		if (name.length() == 1) {
			throw new ShortNameException(name);
		}
	}

	/**
	 * Отбирает записи по условиям поиска без проверки самих условий.
	 *
	 * @param reader имя читателя или «Все читатели»
	 * @param book   часть названия книги, регистр не учитывается
	 * @return отобранные записи
	 */
	public synchronized List<Issue> select(String reader, String book) {
		List<Issue> found = new ArrayList<>();
		for (int index : selectIndexes(reader, book)) {
			found.add(issues.get(index));
		}
		return found;
	}

	/**
	 * Возвращает номера записей, подходящих под условия поиска. Номера нужны
	 * веб-интерфейсу, чтобы изменять и удалять записи из отфильтрованного
	 * списка.
	 *
	 * @param reader имя читателя или «Все читатели»
	 * @param book   часть названия книги, регистр не учитывается
	 * @return номера подходящих записей по возрастанию
	 */
	public synchronized List<Integer> selectIndexes(String reader,
			String book) {
		String name = book == null ? "" : book.trim().toLowerCase();
		boolean allReaders = reader == null || reader.isBlank()
				|| ALL_READERS.equals(reader);
		List<Integer> found = new ArrayList<>();
		for (int i = 0; i < issues.size(); i++) {
			Issue issue = issues.get(i);
			boolean byReader = allReaders || issue.getReader().equals(reader);
			boolean byBook = name.isEmpty()
					|| issue.getBook().toLowerCase().contains(name);
			if (byReader && byBook) {
				found.add(i);
			}
		}
		return found;
	}

	/**
	 * Проверяет условия и отбирает записи.
	 *
	 * @param reader имя читателя или «Все читатели»
	 * @param book   часть названия книги
	 * @return отобранные записи
	 * @throws EmptyQueryException если не задано ни одно условие отбора
	 * @throws ShortNameException  если название книги состоит из одного
	 *                             символа
	 */
	public List<Issue> search(String reader, String book)
			throws EmptyQueryException, ShortNameException {
		checkQuery(reader, book);
		List<Issue> found = select(reader, book);
		log.debug("Поиск «{}» / «{}»: найдено {}", reader, book, found.size());
		return found;
	}
}
