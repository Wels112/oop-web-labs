package edu.java.issues.exception;

/**
 * Исключение «ошибка хранилища» (ООП, лабораторные работы № 5 и № 6).
 * Оборачивает ошибки чтения и записи текстового и XML-файлов, чтобы
 * интерфейс получал одно понятное сообщение независимо от формата файла.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class StorageException extends Exception {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/**
	 * Создаёт исключение с сообщением и причиной.
	 *
	 * @param message сообщение для пользователя
	 * @param cause   исходная ошибка ввода-вывода или разбора файла
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
