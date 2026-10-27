package edu.java.web;

import java.util.ArrayList;
import java.util.List;

/**
 * Набор данных приложения «Картотека выдач»: читатели и записи о выдачах.
 * Данные хранятся в памяти и одинаковы для всех посетителей.
 *
 * @author Рябов И.А., группа 5315
 * @version 5.0
 */
public final class IssueStore {

	/** Читатели библиотеки. */
	private static final List<String> READERS = List.of("Иванов Пётр",
			"Смирнова Анна", "Кузнецов Илья");

	/** Все записи о выдачах. */
	private static final List<Issue> ISSUES = List.of(
			new Issue("Иванов Пётр", "Три мушкетёра", "05.09.2026",
					"05.10.2026", true),
			new Issue("Смирнова Анна", "Война и мир", "12.09.2026",
					"12.10.2026", false),
			new Issue("Иванов Пётр", "Анна Каренина", "20.09.2026",
					"20.10.2026", false),
			new Issue("Кузнецов Илья", "Три мушкетёра", "25.09.2026",
					"25.10.2026", false));

	/** Служебный класс: экземпляры не создаются. */
	private IssueStore() {
	}

	/**
	 * Возвращает имена читателей.
	 *
	 * @return неизменяемый список имён
	 */
	public static List<String> readers() {
		return READERS;
	}

	/**
	 * Возвращает выдачи читателя.
	 *
	 * @param reader имя читателя; пустое значение означает всех читателей
	 * @return записи о выдачах этого читателя
	 */
	public static List<Issue> byReader(String reader) {
		List<Issue> found = new ArrayList<Issue>();
		for (Issue issue : ISSUES) {
			if (reader == null || reader.isBlank()
					|| issue.getReader().equals(reader)) {
				found.add(issue);
			}
		}
		return found;
	}
}
