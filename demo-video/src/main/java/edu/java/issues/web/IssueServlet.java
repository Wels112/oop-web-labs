package edu.java.issues.web;

import static edu.java.issues.web.WebSupport.esc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ResourceBundle;

import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Сервлет «Список выдач» (Web, лабораторная работа № 3). Страницу целиком
 * формирует код сервлета. Параметры инициализации <code>title</code>
 * и <code>maxRows</code> задаются в дескрипторе web.xml, динамические
 * параметры <code>reader</code> и <code>book</code> приходят в запросе.
 * Тексты берутся из файлов ресурсов (лабораторная работа № 4).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Хранилище выдач; в тестах подменяется. */
	private transient IssueRepository repository = IssueRepository
			.getInstance();

	/** Параметр инициализации: ключ заголовка страницы в файле ресурсов. */
	private String titleKey = "servlet.title";

	/** Параметр инициализации: наибольшее число строк на странице. */
	private int maxRows = 50;

	/**
	 * Читает параметры инициализации сервлета из web.xml.
	 *
	 * @param config конфигурация сервлета
	 * @throws ServletException если базовая инициализация не удалась
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String key = config.getInitParameter("titleKey");
		if (key != null) {
			titleKey = key;
		}
		String rows = config.getInitParameter("maxRows");
		if (rows != null) {
			try {
				maxRows = Integer.parseInt(rows.trim());
			} catch (NumberFormatException e) {
				throw new ServletException(
						"Параметр maxRows должен быть числом: " + rows, e);
			}
		}
	}

	/**
	 * Подменяет хранилище (для модульных тестов с mock-объектами).
	 *
	 * @param repository хранилище выдач
	 */
	void setRepository(IssueRepository repository) {
		this.repository = repository;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ResourceBundle res = WebSupport.bundleOf(request);
		String reader = request.getParameter("reader");
		String book = request.getParameter("book");
		List<Issue> found = repository.select(reader, book);
		boolean cut = found.size() > maxRows;
		if (cut) {
			found = found.subList(0, maxRows);
		}

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		String ctx = request.getContextPath();
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"" + esc(WebSupport.localeOf(request)
				.getLanguage()) + "\"><head><meta charset=\"UTF-8\">");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		out.println("<title>" + esc(res.getString(titleKey)) + "</title>");
		out.println("<link rel=\"stylesheet\" href=\"" + ctx
				+ "/css/style.css\"></head><body><main class=\"page narrow\">");
		out.println("<p><a href=\"" + ctx + "/\">" + esc(res.getString("nav.home"))
				+ "</a></p>");
		out.println("<h1>" + esc(res.getString(titleKey)) + "</h1>");
		out.println("<form method=\"get\" class=\"row\">");
		out.println("<label>" + esc(res.getString("filter.reader"))
				+ "<input name=\"reader\" value=\"" + esc(reader) + "\"></label>");
		out.println("<label>" + esc(res.getString("filter.book"))
				+ "<input name=\"book\" value=\"" + esc(book) + "\"></label>");
		out.println("<button class=\"btn\">" + esc(res.getString("filter.search"))
				+ "</button></form>");
		if (found.isEmpty()) {
			out.println("<p class=\"empty\">" + esc(res.getString("list.empty"))
					+ "</p>");
		} else {
			out.println("<div class=\"table-wrap\"><table><thead><tr>");
			for (String col : new String[] { "col.reader", "col.book",
					"col.issued", "col.due", "col.returned" }) {
				out.println("<th>" + esc(res.getString(col)) + "</th>");
			}
			out.println("</tr></thead><tbody>");
			for (Issue issue : found) {
				out.println("<tr><td>" + esc(issue.getReader()) + "</td><td>"
						+ esc(issue.getBook()) + "</td><td>"
						+ esc(issue.getIssued()) + "</td><td>"
						+ esc(issue.getDue()) + "</td><td>"
						+ esc(Issue.YES.equals(issue.getReturned())
								? res.getString("yes") : res.getString("no"))
						+ "</td></tr>");
			}
			out.println("</tbody></table></div>");
		}
		if (cut) {
			out.println("<p class=\"hint\">" + esc(res.getString("list.cut"))
					+ " " + maxRows + "</p>");
		}
		out.println("</main></body></html>");
	}
}
