package edu.java.issues.gwt.server;

import java.util.ArrayList;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import edu.java.issues.gwt.client.IssueDto;
import edu.java.issues.gwt.client.IssueService;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;

/**
 * Серверная часть службы GWT-RPC (Web, лабораторная работа № 8): отдаёт
 * браузеру данные из общего хранилища выдач. Обращения к службе проходят
 * через тот же фильтр входа, что и остальные страницы приложения.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueServiceImpl extends RemoteServiceServlet
		implements IssueService {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	@Override
	public ArrayList<String> getReaders() {
		return new ArrayList<>(IssueRepository.readers());
	}

	@Override
	public ArrayList<IssueDto> getIssues(String reader) {
		ArrayList<IssueDto> result = new ArrayList<>();
		for (Issue issue : IssueRepository.getInstance().select(reader, "")) {
			result.add(new IssueDto(issue.getBook(), issue.getIssued(),
					issue.getDue(), issue.getReturned()));
		}
		return result;
	}
}
