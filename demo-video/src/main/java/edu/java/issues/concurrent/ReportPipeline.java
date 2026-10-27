package edu.java.issues.concurrent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.exception.StorageException;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.report.IssueReportBuilder;

/**
 * Конвейер из трёх параллельных потоков (ООП, лабораторная работа № 8):
 * <ol>
 * <li>поток «Загрузчик» читает данные из XML-файла в хранилище;</li>
 * <li>поток «Редактор» не начинает работу, пока загрузчик не закончил,
 * затем отбирает невозвращённые книги, упорядочивает их по сроку возврата
 * и записывает XML-файл для отчёта;</li>
 * <li>поток «Построитель» не начинает работу, пока редактор не записал
 * файл, затем строит отчёт в формате HTML.</li>
 * </ol>
 * Потоки синхронизируются монитором и методами wait()/notifyAll(): ожидающий
 * поток не тратит время процессора на опрос условия. Запускаются потоки
 * в обратном порядке, чтобы ожидание было видно в протоколе.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class ReportPipeline {

	/** Журнал работы потоков (лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(ReportPipeline.class);

	/** Монитор, на котором потоки ждут друг друга. */
	private final Object monitor = new Object();

	/** Хранилище, в которое загружаются данные. */
	private final IssueRepository repository;

	/** Файл с исходными данными. */
	private final Path source;

	/** Файл с данными для отчёта, который строит второй поток. */
	private final Path reportXml;

	/** Итоговый HTML-отчёт. */
	private final Path html;

	/** Получатель сообщений о ходе работы (окно Swing). */
	private final Consumer<String> progress;

	/** Пауза каждого этапа в миллисекундах: делает ход работы наглядным. */
	private final long pause;

	/** Признак «данные загружены»; изменяется под монитором. */
	private boolean loaded;

	/** Признак «XML для отчёта записан»; изменяется под монитором. */
	private boolean edited;

	/** Ошибка любого из потоков, если она была. */
	private volatile Exception failure;

	/**
	 * Создаёт конвейер.
	 *
	 * @param repository хранилище, в которое загружаются данные
	 * @param source     XML-файл с исходными данными
	 * @param reportXml  XML-файл, который строит редактор
	 * @param html       итоговый HTML-отчёт
	 * @param pause      пауза каждого этапа, мс (в тестах может быть нулевой)
	 * @param progress   получатель сообщений о ходе работы
	 */
	public ReportPipeline(IssueRepository repository, Path source,
			Path reportXml, Path html, long pause, Consumer<String> progress) {
		this.repository = repository;
		this.source = source;
		this.reportXml = reportXml;
		this.html = html;
		this.pause = pause;
		this.progress = progress;
	}

	/**
	 * Запускает три потока и ждёт их завершения методом join.
	 *
	 * @return путь к построенному HTML-отчёту
	 * @throws StorageException     если любой из потоков завершился ошибкой
	 * @throws InterruptedException если ожидание было прервано
	 */
	public Path run() throws StorageException, InterruptedException {
		loaded = false;
		edited = false;
		failure = null;
		Thread loader = new Thread(this::loadData, "Загрузчик");
		Thread editor = new Thread(this::editData, "Редактор");
		Thread builder = new Thread(this::buildReport, "Построитель");
		builder.start();
		editor.start();
		loader.start();
		loader.join();
		editor.join();
		builder.join();
		if (failure != null) {
			throw new StorageException(
					"Конвейер остановлен: " + failure.getMessage(), failure);
		}
		say("Основной поток: все три потока завершены");
		return html;
	}

	/** Тело первого потока: загрузка данных из XML-файла. */
	private void loadData() {
		try {
			say("Загрузчик: читаю " + source);
			sleep();
			List<Issue> issues = new XmlIssueStorage().load(source.toString());
			repository.replaceAll(issues);
			say("Загрузчик: загружено записей " + issues.size());
			synchronized (monitor) {
				loaded = true;
				monitor.notifyAll();
			}
		} catch (Exception e) {
			fail(e);
		}
	}

	/** Тело второго потока: подготовка XML-файла для отчёта. */
	private void editData() {
		try {
			synchronized (monitor) {
				while (!loaded && failure == null) {
					say("Редактор: жду, пока загрузчик закончит");
					monitor.wait();
				}
			}
			if (failure != null) {
				return;
			}
			sleep();
			List<Issue> debtors = new ArrayList<>();
			for (Issue issue : repository.all()) {
				if (!Issue.YES.equals(issue.getReturned())) {
					debtors.add(issue);
				}
			}
			debtors.sort(Comparator.comparing(Issue::getDue));
			new XmlIssueStorage().save(reportXml.toString(), debtors);
			say("Редактор: записан " + reportXml + ", книг на руках "
					+ debtors.size());
			synchronized (monitor) {
				edited = true;
				monitor.notifyAll();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			fail(e);
		} catch (Exception e) {
			fail(e);
		}
	}

	/** Тело третьего потока: построение отчёта в формате HTML. */
	private void buildReport() {
		try {
			synchronized (monitor) {
				while (!edited && failure == null) {
					say("Построитель: жду, пока редактор запишет XML");
					monitor.wait();
				}
			}
			if (failure != null) {
				return;
			}
			sleep();
			new IssueReportBuilder().buildHtml(reportXml, html);
			say("Построитель: отчёт готов: " + html);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			fail(e);
		} catch (Exception e) {
			fail(e);
		}
	}

	/**
	 * Запоминает ошибку и будит ожидающие потоки, чтобы они не зависли.
	 *
	 * @param e ошибка потока
	 */
	private void fail(Exception e) {
		log.error("Ошибка в потоке {}", Thread.currentThread().getName(), e);
		failure = e;
		say(Thread.currentThread().getName() + ": ошибка: " + e.getMessage());
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}

	/**
	 * Передаёт сообщение о ходе работы в журнал и получателю.
	 *
	 * @param message текст сообщения
	 */
	private void say(String message) {
		log.info(message);
		if (progress != null) {
			progress.accept(message);
		}
	}

	/**
	 * Делает паузу этапа.
	 *
	 * @throws InterruptedException если поток прерван во время паузы
	 */
	private void sleep() throws InterruptedException {
		if (pause > 0) {
			Thread.sleep(pause);
		}
	}
}
