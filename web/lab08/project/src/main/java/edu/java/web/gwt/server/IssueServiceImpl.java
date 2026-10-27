package edu.java.web.gwt.server;

import java.util.ArrayList;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import edu.java.web.gwt.client.IssueDto;
import edu.java.web.gwt.client.IssueService;
import edu.java.web.Issue;
import edu.java.web.IssueStore;

/**
 * Серверная часть службы GWT-RPC (Web, лабораторная работа № 8): отдаёт
 * браузеру данные из общего хранилища выдач. Обращения к службе проходят
 * через тот же фильтр входа, что и остальные страницы приложения.
 *
 * @author Рябов И.А., группа 5315
 * @version 8.0
 */
public class IssueServiceImpl extends RemoteServiceServlet
		implements IssueService {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	@Override
	public ArrayList<String> getReaders() {
		return new ArrayList<>(IssueStore.readers());
	}

	@Override
	public ArrayList<IssueDto> getIssues(String reader) {
		ArrayList<IssueDto> result = new ArrayList<>();
		for (Issue issue : IssueStore.byReader(reader)) {
			result.add(new IssueDto(issue.getBook(), issue.getIssued(),
					issue.getDue(), (issue.isReturned() ? "Да" : "Нет")));
		}
		return result;
	}
}
