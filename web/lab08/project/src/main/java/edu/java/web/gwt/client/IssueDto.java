package edu.java.web.gwt.client;

import java.io.Serializable;

/**
 * Данные одной выдачи, передаваемые из серверной части GWT в браузер
 * (Web, лабораторная работа № 8). Класс должен иметь конструктор без
 * параметров и сериализуемые поля: это требование механизма GWT-RPC.
 *
 * @author Рябов И.А., группа 5315
 * @version 8.0
 */
public class IssueDto implements Serializable {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Название книги. */
	private String book;

	/** Дата выдачи. */
	private String issued;

	/** Срок возврата. */
	private String due;

	/** Отметка о возврате: «Да» или «Нет». */
	private String returned;

	/** Конструктор без параметров нужен для сериализации GWT-RPC. */
	public IssueDto() {
	}

	/**
	 * Создаёт объект передачи данных.
	 *
	 * @param book     название книги
	 * @param issued   дата выдачи
	 * @param due      срок возврата
	 * @param returned отметка о возврате
	 */
	public IssueDto(String book, String issued, String due, String returned) {
		this.book = book;
		this.issued = issued;
		this.due = due;
		this.returned = returned;
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

	/** @return отметка о возврате */
	public String getReturned() {
		return returned;
	}
}
