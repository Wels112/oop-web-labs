package edu.java.issues.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.exception.EmptyQueryException;
import edu.java.issues.exception.ShortNameException;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.model.IssueValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Контроллер страницы «Выдачи» (Web, лабораторные работы № 5 и № 7).
 * GET готовит данные и передаёт их странице issues.jsp через атрибуты
 * запроса (forward). POST изменяет данные и после успеха перенаправляет
 * браузер обратно (redirect), передавая сообщение об итоге через сессию:
 * так обновление страницы не повторяет операцию. Условие отбора по читателю
 * запоминается в сессии (страница выбора читателя).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssuesController extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Журнал работы контроллера. */
	private static final Logger log = LogManager
			.getLogger(IssuesController.class);

	/** Хранилище выдач. */
	private final transient IssueRepository repository = IssueRepository
			.getInstance();

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		ResourceBundle res = WebSupport.bundleOf(request);
		String reader = request.getParameter("reader");
		if (reader == null) {
			// Данные между запросами: читатель, выбранный на странице вопроса
			reader = (String) session.getAttribute(WebSupport.READER_FILTER);
		}
		String book = request.getParameter("book");
		if (reader == null) {
			reader = IssueRepository.ALL_READERS;
		}
		if (book == null) {
			book = "";
		}
		if (request.getParameter("search") != null) {
			// Проверка условий поиска теми же исключениями, что в окне Swing
			try {
				IssueRepository.checkQuery(reader, book);
			} catch (EmptyQueryException e) {
				request.setAttribute("error", res.getString("error.empty"));
			} catch (ShortNameException e) {
				request.setAttribute("error", res.getString("error.short"));
			}
		}
		List<IssueRow> rows = new ArrayList<>();
		List<Issue> all = repository.all();
		for (int index : repository.selectIndexes(reader, book.trim())) {
			rows.add(new IssueRow(index, all.get(index)));
		}
		request.setAttribute("rows", rows);
		request.setAttribute("readers", IssueRepository.readers());
		request.setAttribute("selectedReader", reader);
		request.setAttribute("book", book);
		request.setAttribute("csrf", WebSupport.csrfToken(session));
		Object flash = session.getAttribute(WebSupport.FLASH);
		if (flash != null) {
			request.setAttribute("flash", flash);
			session.removeAttribute(WebSupport.FLASH);
		}
		request.getRequestDispatcher("/WEB-INF/views/issues.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!WebSupport.csrfValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Неверный защитный токен формы");
			return;
		}
		HttpSession session = request.getSession();
		ResourceBundle res = WebSupport.bundleOf(request);
		String action = request.getParameter("action");
		String message;
		try {
			if ("delete".equals(action)) {
				repository.remove(index(request));
				message = res.getString("flash.deleted");
			} else if ("add".equals(action) || "update".equals(action)) {
				String reader = request.getParameter("reader");
				String book = request.getParameter("book");
				String issued = request.getParameter("issued");
				String due = request.getParameter("due");
				String error = IssueValidator.check(reader, book, issued, due);
				if (error != null) {
					session.setAttribute(WebSupport.FLASH,
							res.getString("flash.invalid") + ": " + error);
					response.sendRedirect(response.encodeRedirectURL(
							request.getContextPath() + "/issues"));
					return;
				}
				Issue issue = new Issue(reader, book, issued, due,
						request.getParameter("returned") == null ? Issue.NO
								: Issue.YES);
				if ("add".equals(action)) {
					repository.add(issue);
					message = res.getString("flash.added");
				} else {
					repository.update(index(request), issue);
					message = res.getString("flash.updated");
				}
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Неизвестное действие");
				return;
			}
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			log.warn("Неверный номер записи: {}", request.getParameter("index"));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Неверный номер записи");
			return;
		}
		session.setAttribute(WebSupport.FLASH, message);
		response.sendRedirect(response.encodeRedirectURL(
				request.getContextPath() + "/issues"));
	}

	/**
	 * Читает номер записи из параметра запроса.
	 *
	 * @param request запрос
	 * @return номер записи
	 * @throws NumberFormatException если параметр не является числом
	 */
	private int index(HttpServletRequest request) {
		return Integer.parseInt(request.getParameter("index"));
	}
}
