package edu.java.issues.model;

import java.util.Objects;

/**
 * Запись картотеки выдач: какой читатель, какую книгу и на какой срок взял.
 * Объект не изменяется после создания: для редактирования создаётся новая
 * запись, поэтому его безопасно передавать между потоками.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public final class Issue {

	/** Отметка о возврате для вернувшей книгу записи. */
	public static final String YES = "Да";

	/** Отметка о возврате для записи, где книга ещё на руках. */
	public static final String NO = "Нет";

	/** Имя читателя. */
	private final String reader;

	/** Название книги. */
	private final String book;

	/** Дата выдачи в формате дд.мм.гггг. */
	private final String issued;

	/** Срок возврата в формате дд.мм.гггг. */
	private final String due;

	/** Отметка о возврате: «Да» или «Нет». */
	private final String returned;

	/**
	 * Создаёт запись о выдаче.
	 *
	 * @param reader   имя читателя
	 * @param book     название книги
	 * @param issued   дата выдачи
	 * @param due      срок возврата
	 * @param returned отметка о возврате; любое значение, кроме «Да»,
	 *                 считается значением «Нет»
	 */
	public Issue(String reader, String book, String issued, String due,
			String returned) {
		this.reader = clean(reader);
		this.book = clean(book);
		this.issued = clean(issued);
		this.due = clean(due);
		this.returned = YES.equalsIgnoreCase(clean(returned)) ? YES : NO;
	}

	/**
	 * Убирает пробелы по краям и заменяет null пустой строкой.
	 *
	 * @param value исходное значение
	 * @return очищенная строка
	 */
	private static String clean(String value) {
		return value == null ? "" : value.trim();
	}

	/** @return имя читателя */
	public String getReader() {
		return reader;
	}

	/** @return название книги */
	public String getBook() {
		return book;
	}

	/** @return дата выдачи */
	public String getIssued() {
		return issued;
	}

	/** @return срок возврата */
	public String getDue() {
		return due;
	}

	/** @return отметка о возврате: «Да» или «Нет» */
	public String getReturned() {
		return returned;
	}

	/**
	 * Возвращает копию записи с новой отметкой о возврате.
	 *
	 * @param newReturned новая отметка о возврате
	 * @return новая запись
	 */
	public Issue withReturned(String newReturned) {
		return new Issue(reader, book, issued, due, newReturned);
	}

	/**
	 * Представляет запись массивом строк в порядке столбцов таблицы.
	 *
	 * @return массив из пяти строк
	 */
	public String[] toRow() {
		return new String[] { reader, book, issued, due, returned };
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Issue)) {
			return false;
		}
		Issue o = (Issue) other;
		return reader.equals(o.reader) && book.equals(o.book)
				&& issued.equals(o.issued) && due.equals(o.due)
				&& returned.equals(o.returned);
	}

	@Override
	public int hashCode() {
		return Objects.hash(reader, book, issued, due, returned);
	}

	@Override
	public String toString() {
		return reader + ": " + book + " (" + issued + " - " + due
				+ ", возвращена: " + returned + ")";
	}
}
