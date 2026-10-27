package edu.java.issues.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Проверка данных новой или изменённой записи о выдаче. Одну и ту же проверку
 * используют окно Swing и веб-форма, поэтому правила не расходятся.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public final class IssueValidator {

	/** Строгий формат даты дд.мм.гггг. */
	private static final DateTimeFormatter DATE = DateTimeFormatter
			.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

	/** Служебный класс: экземпляры не создаются. */
	private IssueValidator() {
	}

	/**
	 * Разбирает дату в формате дд.мм.гггг.
	 *
	 * @param text текст даты
	 * @return дата или null, если текст не является корректной датой
	 */
	public static LocalDate parseDate(String text) {
		try {
			return LocalDate.parse(text == null ? "" : text.trim(), DATE);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/**
	 * Проверяет поля записи.
	 *
	 * @param reader имя читателя
	 * @param book   название книги
	 * @param issued дата выдачи
	 * @param due    срок возврата
	 * @return текст ошибки для пользователя или null, если данные верны
	 */
	public static String check(String reader, String book, String issued,
			String due) {
		if (reader == null || reader.isBlank()) {
			return "Укажите читателя";
		}
		if (book == null || book.isBlank()) {
			return "Укажите название книги";
		}
		LocalDate from = parseDate(issued);
		if (from == null) {
			return "Дата выдачи должна быть в формате дд.мм.гггг";
		}
		LocalDate to = parseDate(due);
		if (to == null) {
			return "Срок возврата должен быть в формате дд.мм.гггг";
		}
		if (to.isBefore(from)) {
			return "Срок возврата раньше даты выдачи";
		}
		return null;
	}
}
