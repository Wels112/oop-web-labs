package edu.java.web;

/**
 * Запись о выдаче книги читателю: читатель, книга, срок возврата и отметка
 * о возврате. Класс неизменяемый, поэтому его безопасно использовать
 * из нескольких потоков сервера.
 *
 * @author Рябов И.А., группа 5315
 * @version 5.0
 */
public final class Issue {

	/** Имя читателя. */
	private final String reader;

	/** Название книги. */
	private final String book;

	/** Дата выдачи. */
	private final String issued;

	/** Срок возврата. */
	private final String due;

	/** Признак «книга возвращена». */
	private final boolean returned;

	/**
	 * Создаёт запись о выдаче.
	 *
	 * @param reader   имя читателя
	 * @param book     название книги
	 * @param issued   дата выдачи
	 * @param due      срок возврата
	 * @param returned true, если книга уже возвращена
	 */
	public Issue(String reader, String book, String issued, String due,
			boolean returned) {
		this.reader = reader;
		this.book = book;
		this.issued = issued;
		this.due = due;
		this.returned = returned;
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

	/** @return true, если книга возвращена */
	public boolean isReturned() {
		return returned;
	}
}
