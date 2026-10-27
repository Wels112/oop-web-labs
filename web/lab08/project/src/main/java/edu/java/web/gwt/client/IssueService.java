package edu.java.web.gwt.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Синхронный интерфейс службы GWT-RPC «Картотека выдач» (Web, лабораторная
 * работа № 8). Его реализует сервлет на сервере, а браузер вызывает методы
 * через асинхронный интерфейс {@link IssueServiceAsync}.
 *
 * @author Рябов И.А., группа 5315
 * @version 8.0
 */
@RemoteServiceRelativePath("issueService")
public interface IssueService extends RemoteService {

	/**
	 * Возвращает имена читателей.
	 *
	 * @return список имён читателей
	 */
	ArrayList<String> getReaders();

	/**
	 * Возвращает книги, выданные читателю.
	 *
	 * @param reader имя читателя
	 * @return список выдач этого читателя
	 */
	ArrayList<IssueDto> getIssues(String reader);
}
