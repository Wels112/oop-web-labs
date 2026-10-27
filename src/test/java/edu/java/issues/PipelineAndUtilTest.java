package edu.java.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.java.issues.concurrent.ReportPipeline;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.lab1.ArraySort;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.model.IssueValidator;

/**
 * Тесты конвейера потоков, проверки данных и сортировки массива
 * (ООП, лабораторные работы № 1, № 8, № 9).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
class PipelineAndUtilTest {

	/** Временный каталог для файлов теста. */
	@TempDir
	Path dir;

	/** Потоки выполняются в нужном порядке независимо от порядка запуска. */
	@Test
	void pipelineRunsThreadsInOrder() throws Exception {
		Path source = dir.resolve("in.xml");
		new XmlIssueStorage().save(source.toString(), List.of(
				new Issue("Иванов Пётр", "Три мушкетёра", "05.09.2026",
						"05.10.2026", "Да"),
				new Issue("Смирнова Анна", "Война и мир", "12.09.2026",
						"12.10.2026", "Нет")));
		IssueRepository repo = new IssueRepository();
		List<String> events = Collections.synchronizedList(new ArrayList<>());
		ReportPipeline pipeline = new ReportPipeline(repo, source,
				dir.resolve("report.xml"), dir.resolve("out.html"), 0,
				events::add);
		Path html = pipeline.run();

		assertEquals(2, repo.size());
		assertTrue(Files.readString(html).contains("Смирнова Анна"));
		// В отчёт попала только невозвращённая книга
		assertEquals(1,
				new XmlIssueStorage().load(dir.resolve("report.xml").toString())
						.size());
		int loaded = indexOf(events, "Загрузчик: загружено");
		int edited = indexOf(events, "Редактор: записан");
		int built = indexOf(events, "Построитель: отчёт готов");
		assertTrue(loaded < edited && edited < built, events.toString());
	}

	/** Ошибка загрузчика не оставляет остальные потоки ждать вечно. */
	@Test
	void pipelineFailsCleanlyWhenSourceMissing() {
		ReportPipeline pipeline = new ReportPipeline(new IssueRepository(),
				dir.resolve("none.xml"), dir.resolve("r.xml"),
				dir.resolve("o.html"), 0, null);
		org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
				java.time.Duration.ofSeconds(10),
				() -> org.junit.jupiter.api.Assertions.assertThrows(
						edu.java.issues.exception.StorageException.class,
						pipeline::run));
	}

	/**
	 * Ищет первое сообщение, начинающееся с заданного текста.
	 *
	 * @param events сообщения конвейера
	 * @param prefix начало искомого сообщения
	 * @return номер сообщения
	 */
	private int indexOf(List<String> events, String prefix) {
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i).startsWith(prefix)) {
				return i;
			}
		}
		throw new AssertionError("Нет сообщения «" + prefix + "» в " + events);
	}

	/** Проверка дат: формат, несуществующие дни, порядок дат. */
	@Test
	void validatorChecksDates() {
		assertNull(IssueValidator.check("Иванов Пётр", "Книга", "01.10.2026",
				"01.11.2026"));
		assertNotNull(IssueValidator.check("", "Книга", "01.10.2026",
				"01.11.2026"));
		assertNotNull(IssueValidator.check("Иванов Пётр", "Книга", "31.02.2026",
				"01.11.2026"));
		assertNotNull(IssueValidator.check("Иванов Пётр", "Книга", "2026-10-01",
				"01.11.2026"));
		assertNotNull(IssueValidator.check("Иванов Пётр", "Книга", "01.11.2026",
				"01.10.2026"));
	}

	/** Сортировка по возрастанию и убыванию (лабораторная работа № 1). */
	@Test
	void arraySortOrdersNumbers() {
		int[] a = { 5, 3, 9, 1 };
		ArraySort.sort(a, true);
		assertEquals("[1, 3, 5, 9]", java.util.Arrays.toString(a));
		ArraySort.sort(a, false);
		assertEquals("[9, 5, 3, 1]", java.util.Arrays.toString(a));
	}
}
