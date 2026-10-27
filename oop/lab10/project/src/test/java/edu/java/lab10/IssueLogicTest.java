package edu.java.lab10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Модульные тесты (лабораторная работа № 9): поиск и контроль условий поиска,
 * сохранение и загрузка XML, отчёты, конвейер потоков.
 *
 * @author Рябов И.А., группа 5315
 * @version 10.0
 */
class IssueLogicTest {

	/** Временный каталог, который JUnit удаляет после теста. */
	@TempDir
	Path dir;

	/** Данные для проверок. */
	private List<String[]> sample;

	/** Создаёт три записи перед каждым тестом. */
	@BeforeEach
	void setUp() {
		sample = new ArrayList<String[]>();
		sample.add(new String[] { "Иванов Пётр", "Три мушкетёра", "05.09.2026",
				"05.10.2026", "Да" });
		sample.add(new String[] { "Смирнова Анна", "Война и мир",
				"12.09.2026", "12.10.2026", "Нет" });
		sample.add(new String[] { "Иванов Пётр", "Анна Каренина",
				"20.09.2026", "20.10.2026", "Нет" });
	}

	/** Поиск по читателю возвращает все его книги. */
	@Test
	void searchByReader() {
		assertEquals(2, IssueLogic.selectRows(sample, "Иванов Пётр", "").size());
	}

	/** Поиск по части названия не зависит от регистра. */
	@Test
	void searchByBookIgnoresCase() {
		List<String[]> found = IssueLogic.selectRows(sample,
				IssueLogic.ALL_READERS, "ВОЙНА");
		assertEquals(1, found.size());
		assertEquals("Смирнова Анна", found.get(0)[0]);
	}

	/** Пустой запрос приводит к EmptyQueryException. */
	@Test
	void emptyQueryIsRejected() {
		assertThrows(EmptyQueryException.class,
				() -> IssueLogic.checkQuery(IssueLogic.ALL_READERS, ""));
	}

	/** Название из одного символа приводит к ShortNameException. */
	@Test
	void oneLetterBookIsRejected() {
		ShortNameException e = assertThrows(ShortNameException.class,
				() -> IssueLogic.checkQuery(IssueLogic.ALL_READERS, "А"));
		assertTrue(e.getMessage().contains("«А»"));
	}

	/** XML-файл читается так же, как был записан. */
	@Test
	void xmlRoundTrip() throws Exception {
		String file = dir.resolve("issues.xml").toString();
		XmlIssues.save(file, sample);
		List<String[]> loaded = XmlIssues.load(file);
		assertEquals(sample.size(), loaded.size());
		for (int i = 0; i < sample.size(); i++) {
			assertEquals(String.join("|", sample.get(i)),
					String.join("|", loaded.get(i)));
		}
	}

	/** Отсутствующий XML-файл приводит к исключению. */
	@Test
	void missingXmlFails() {
		assertThrows(Exception.class,
				() -> XmlIssues.load(dir.resolve("none.xml").toString()));
	}

	/** Оба отчёта строятся и содержат данные. */
	@Test
	void reportsAreBuilt() throws Exception {
		Path xml = dir.resolve("issues.xml");
		XmlIssues.save(xml.toString(), sample);
		Path pdf = ReportBuilder.buildPdf(xml, dir.resolve("out/r.pdf"));
		Path html = ReportBuilder.buildHtml(xml, dir.resolve("out/r.html"));
		assertEquals("%PDF", new String(Files.readAllBytes(pdf), 0, 4));
		assertTrue(Files.readString(html).contains("Смирнова Анна"));
	}

	/** Потоки конвейера выполняются в нужном порядке. */
	@Test
	void pipelineRunsThreadsInOrder() throws Exception {
		Path source = dir.resolve("in.xml");
		XmlIssues.save(source.toString(), sample);
		List<String> events = Collections.synchronizedList(new ArrayList<String>());
		ReportPipeline pipeline = new ReportPipeline(source,
				dir.resolve("report.xml"), dir.resolve("out.html"), 0,
				events::add, null);
		pipeline.run();
		// В отчёт попали только невозвращённые книги
		assertEquals(2, XmlIssues.load(dir.resolve("report.xml").toString()).size());
		int loaded = indexOf(events, "Загрузчик: загружено");
		int edited = indexOf(events, "Редактор: записан");
		int built = indexOf(events, "Построитель: отчёт готов");
		assertTrue(loaded < edited && edited < built, events.toString());
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
}
