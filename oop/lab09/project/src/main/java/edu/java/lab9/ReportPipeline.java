package edu.java.lab9;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Конвейер из трёх параллельных потоков (лабораторная работа № 8):
 * <ol>
 * <li>поток «Загрузчик» читает данные из XML-файла;</li>
 * <li>поток «Редактор» не начинает работу, пока загрузчик не закончил,
 * затем отбирает невозвращённые книги, упорядочивает их по сроку возврата
 * и записывает XML-файл для отчёта;</li>
 * <li>поток «Построитель» не начинает работу, пока редактор не записал
 * файл, затем строит отчёт в формате HTML.</li>
 * </ol>
 * Потоки синхронизируются монитором и методами wait() и notifyAll():
 * ожидающий поток не тратит время процессора на опрос условия. Потоки
 * запускаются в обратном порядке, чтобы ожидание было видно.
 *
 * @author Рябов И.А., группа 5315
 * @version 9.0
 */
public class ReportPipeline {

	/** Монитор, на котором потоки ждут друг друга. */
	private final Object monitor = new Object();

	/** Файл с исходными данными. */
	private final Path source;

	/** Файл с данными для отчёта, который строит второй поток. */
	private final Path reportXml;

	/** Итоговый HTML-отчёт. */
	private final Path html;

	/** Пауза каждого этапа в миллисекундах. */
	private final long pause;

	/** Получатель сообщений о ходе работы. */
	private final Consumer<String> progress;

	/** Получатель загруженных данных. */
	private final Consumer<List<String[]>> onLoaded;

	/** Данные, загруженные первым потоком. */
	private List<String[]> data;

	/** Признак «данные загружены»; изменяется под монитором. */
	private boolean loaded;

	/** Признак «XML для отчёта записан»; изменяется под монитором. */
	private boolean edited;

	/** Ошибка любого из потоков, если она была. */
	private volatile Exception failure;

	/**
	 * Создаёт конвейер.
	 *
	 * @param source    XML-файл с исходными данными
	 * @param reportXml XML-файл, который строит редактор
	 * @param html      итоговый HTML-отчёт
	 * @param pause     пауза каждого этапа, мс (в тестах может быть нулевой)
	 * @param progress  получатель сообщений о ходе работы
	 * @param onLoaded  получатель данных после загрузки
	 */
	public ReportPipeline(Path source, Path reportXml, Path html, long pause,
			Consumer<String> progress, Consumer<List<String[]>> onLoaded) {
		this.source = source;
		this.reportXml = reportXml;
		this.html = html;
		this.pause = pause;
		this.progress = progress;
		this.onLoaded = onLoaded;
	}

	/**
	 * Запускает три потока и ждёт их завершения методом join.
	 *
	 * @return путь к построенному HTML-отчёту
	 * @throws Exception если любой из потоков завершился ошибкой
	 */
	public Path run() throws Exception {
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
			throw new Exception("Конвейер остановлен: " + failure.getMessage(),
					failure);
		}
		say("Основной поток: все три потока завершены");
		return html;
	}

	/** Тело первого потока: загрузка данных из XML-файла. */
	private void loadData() {
		try {
			say("Загрузчик: читаю " + source);
			sleep();
			List<String[]> loadedRows = XmlIssues.load(source.toString());
			if (onLoaded != null) {
				onLoaded.accept(loadedRows);
			}
			say("Загрузчик: загружено записей " + loadedRows.size());
			synchronized (monitor) {
				data = loadedRows;
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
			List<String[]> rows;
			synchronized (monitor) {
				while (!loaded && failure == null) {
					say("Редактор: жду, пока загрузчик закончит");
					monitor.wait();
				}
				rows = data;
			}
			if (failure != null) {
				return;
			}
			sleep();
			List<String[]> debtors = new ArrayList<String[]>();
			for (String[] row : rows) {
				if (!"Да".equals(row[4])) {
					debtors.add(row);
				}
			}
			debtors.sort(Comparator.comparing((String[] r) -> r[3]));
			XmlIssues.save(reportXml.toString(), debtors);
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
			ReportBuilder.buildHtml(reportXml, html);
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
		failure = e;
		say(Thread.currentThread().getName() + ": ошибка: " + e.getMessage());
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}

	/**
	 * Передаёт сообщение о ходе работы получателю.
	 *
	 * @param message текст сообщения
	 */
	private void say(String message) {
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
