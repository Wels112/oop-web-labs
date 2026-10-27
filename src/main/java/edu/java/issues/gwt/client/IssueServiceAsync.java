package edu.java.issues.gwt.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Асинхронный интерфейс службы {@link IssueService}: результат вызова
 * приходит в обработчик AsyncCallback, не блокируя страницу.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public interface IssueServiceAsync {

	/**
	 * Запрашивает имена читателей.
	 *
	 * @param callback обработчик результата
	 */
	void getReaders(AsyncCallback<ArrayList<String>> callback);

	/**
	 * Запрашивает книги читателя.
	 *
	 * @param reader   имя читателя
	 * @param callback обработчик результата
	 */
	void getIssues(String reader, AsyncCallback<ArrayList<IssueDto>> callback);
}
