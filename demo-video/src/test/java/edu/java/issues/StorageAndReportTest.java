package edu.java.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.java.issues.exception.StorageException;
import edu.java.issues.io.TextIssueStorage;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.model.Issue;
import edu.java.issues.report.IssueReportBuilder;

/**
 * Модульные тесты работы с файлами и отчётами (ООП, лабораторные работы
 * № 5, № 6, № 7, № 9).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
class StorageAndReportTest {

	/** Временный каталог, который JUnit удаляет после теста. */
	@TempDir
	Path dir;

	/** Данные для проверок. */
	private final List<Issue> sample = List.of(
			new Issue("Иванов Пётр", "Три мушкетёра", "05.09.2026",
					"05.10.2026", "Да"),
			new Issue("Смирнова Анна", "Война и мир", "12.09.2026",
					"12.10.2026", "Нет"));

	/** Текстовый файл читается так же, как был записан. */
	@Test
	void textRoundTrip() throws Exception {
		TextIssueStorage storage = new TextIssueStorage();
		String file = dir.resolve("a.txt").toString();
		storage.save(file, sample);
		assertEquals(sample, storage.load(file));
	}

	/** XML-файл читается так же, как был записан, включая кириллицу. */
	@Test
	void xmlRoundTrip() throws Exception {
		XmlIssueStorage storage = new XmlIssueStorage();
		String file = dir.resolve("a.xml").toString();
		storage.save(file, sample);
		assertEquals(sample, storage.load(file));
	}

	/** Отсутствующий файл приводит к StorageException. */
	@Test
	void missingFileFails() {
		assertThrows(StorageException.class,
				() -> new TextIssueStorage().load(dir.resolve("none.txt").toString()));
		assertThrows(StorageException.class,
				() -> new XmlIssueStorage().load(dir.resolve("none.xml").toString()));
	}

	/** XML с объявлением DTD отвергается (защита от внешних сущностей). */
	@Test
	void xmlWithDoctypeIsRejected() throws Exception {
		Path evil = dir.resolve("evil.xml");
		Files.writeString(evil,
				"<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><issues>&e;</issues>");
		assertThrows(StorageException.class,
				() -> new XmlIssueStorage().load(evil.toString()));
	}

	/** Оба отчёта строятся и содержат данные. */
	@Test
	void reportsAreBuilt() throws Exception {
		Path xml = dir.resolve("issues.xml");
		new XmlIssueStorage().save(xml.toString(), sample);
		IssueReportBuilder builder = new IssueReportBuilder();
		Path pdf = builder.buildPdf(xml, dir.resolve("out/r.pdf"));
		Path html = builder.buildHtml(xml, dir.resolve("out/r.html"));
		byte[] head = Files.readAllBytes(pdf);
		assertEquals("%PDF", new String(head, 0, 4));
		assertTrue(Files.readString(html).contains("Смирнова Анна"));
	}
}
