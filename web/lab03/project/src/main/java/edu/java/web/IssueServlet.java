package edu.java.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Сервлет «Список выдач» (лабораторная работа № 3). Страницу формирует
 * код сервлета. Параметры инициализации <code>title</code> и
 * <code>maxRows</code> задаются в дескрипторе web.xml, динамический параметр
 * <code>reader</code> приходит в строке запроса.
 *
 * @author Рябов И.А., группа 5315
 * @version 3.0
 */
public class IssueServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Параметр инициализации: заголовок страницы. */
	private String title = "Список выдач";

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
		if (config.getInitParameter("title") != null) {
			title = config.getInitParameter("title");
		}
		if (config.getInitParameter("maxRows") != null) {
			maxRows = Integer.parseInt(config.getInitParameter("maxRows"));
		}
	}

	/**
	 * Обрабатывает запрос GET: выводит таблицу выдач выбранного читателя
	 * или всех читателей.
	 *
	 * @param request  запрос браузера
	 * @param response ответ сервера
	 * @throws ServletException при ошибке сервлета
	 * @throws IOException      при ошибке записи ответа
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		String reader = request.getParameter("reader");
		List<Issue> found = IssueStore.byReader(reader);

		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"ru\"><head><meta charset=\"UTF-8\">");
		out.println("<title>" + esc(title) + "</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
		out.println("</head><body>");
		out.println("<h1>" + esc(title) + "</h1>");
		if (reader != null && !reader.isBlank()) {
			out.println("<p>Читатель: <b>" + esc(reader) + "</b></p>");
		}
		if (found.isEmpty()) {
			out.println("<p class=\"empty\">Выдач не найдено</p>");
		} else {
			out.println("<table><tr><th>Читатель</th><th>Книга</th>"
					+ "<th>Дата выдачи</th><th>Срок возврата</th>"
					+ "<th>Возвращена</th></tr>");
			int shown = 0;
			for (Issue issue : found) {
				if (shown++ >= maxRows) {
					break;
				}
				out.println("<tr><td>" + esc(issue.getReader()) + "</td><td>"
						+ esc(issue.getBook()) + "</td><td>"
						+ esc(issue.getIssued()) + "</td><td>"
						+ esc(issue.getDue()) + "</td><td>"
						+ (issue.isReturned() ? "Да" : "Нет") + "</td></tr>");
			}
			out.println("</table>");
		}
		out.println("<p><a href=\"index.html\">Другой читатель</a></p>");
		out.println("</body></html>");
	}

	/**
	 * Экранирует спецсимволы HTML, чтобы введённый текст нельзя было
	 * использовать для внедрения разметки.
	 *
	 * @param text исходный текст
	 * @return безопасный для вставки в HTML текст
	 */
	static String esc(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;");
	}
}
