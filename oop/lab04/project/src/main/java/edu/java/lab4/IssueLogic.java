package edu.java.lab4;

import java.util.ArrayList;
import java.util.List;

/**
 * Логика поиска по списку выдач, не зависящая от графического интерфейса.
 *
 * @author Рябов И.А., группа 5315
 * @version 4.0
 */
public final class IssueLogic {

	/** Значение условия поиска «читатель не выбран». */
	public static final String ALL_READERS = "Все читатели";

	/** Служебный класс: экземпляры не создаются. */
	private IssueLogic() {
	}

	/**
	 * Проверяет условия поиска и генерирует исключение, если условия
	 * заданы неверно.
	 *
	 * @param selectedReader имя читателя или «Все читатели»
	 * @param book           название книги, введённое пользователем
	 * @throws EmptyQueryException если не задано ни одно условие отбора
	 * @throws ShortNameException  если название книги состоит из одного
	 *                             символа
	 */
	public static void checkQuery(String selectedReader, String book)
			throws EmptyQueryException, ShortNameException {
		if (ALL_READERS.equals(selectedReader) && book.isEmpty()) {
			throw new EmptyQueryException();
		}
		if (book.length() == 1) {
			throw new ShortNameException(book);
		}
	}

	/**
	 * Отбирает из полного списка выдач строки, удовлетворяющие условиям
	 * поиска.
	 *
	 * @param all            полный список выдач
	 * @param selectedReader имя читателя или «Все читатели»
	 * @param book           часть названия книги; пустая строка означает,
	 *                       что название не задано
	 * @return отобранные строки
	 */
	public static List<String[]> selectRows(List<String[]> all,
			String selectedReader, String book) {
		List<String[]> found = new ArrayList<String[]>();
		for (String[] row : all) {
			if (rowMatches(row, selectedReader, book)) {
				found.add(row);
			}
		}
		return found;
	}

	/**
	 * Проверяет, удовлетворяет ли строка условиям поиска.
	 *
	 * @param row            проверяемая строка
	 * @param selectedReader имя читателя или «Все читатели»
	 * @param book           часть названия книги
	 * @return true, если строка удовлетворяет обоим условиям
	 */
	public static boolean rowMatches(String[] row, String selectedReader,
			String book) {
		boolean byReader = ALL_READERS.equals(selectedReader)
				|| row[0].equals(selectedReader);
		boolean byBook = book.isEmpty()
				|| row[1].toLowerCase().contains(book.toLowerCase());
		return byReader && byBook;
	}
}
