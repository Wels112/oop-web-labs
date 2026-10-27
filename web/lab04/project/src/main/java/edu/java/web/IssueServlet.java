package edu.java.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Сервлет «Список выдач» с переключением языка интерфейса (лабораторная
 * работа № 4). Язык выбирается параметром запроса <code>lang</code> (ru или
 * en) и запоминается в сессии. Тексты берутся из файлов ресурсов
 * <code>Issues_ru.properties</code> и <code>Issues_en.properties</code>
 * (кодировка UTF-8), поэтому перекодировка строк не нужна.
 *
 * @author Рябов И.А., группа 5315
 * @version 4.0
 */
public class IssueServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

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
		if (config.getInitParameter("maxRows") != null) {
			maxRows = Integer.parseInt(config.getInitParameter("maxRows"));
		}
	}

	/**
	 * Экранирует спецсимволы HTML.
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

	/**
	 * Выбирает язык интерфейса: параметр запроса, затем сессия, затем
	 * русский язык по умолчанию.
	 *
	 * @param request запрос браузера
	 * @return двухбуквенный код языка
	 */
	static String chooseLanguage(HttpServletRequest request) {
		String lang = request.getParameter("lang");
		HttpSession session = request.getSession();
		if ("ru".equals(lang) || "en".equals(lang)) {
			session.setAttribute("lang", lang);
			return lang;
		}
		Object saved = session.getAttribute("lang");
		return saved == null ? "ru" : (String) saved;
	}

	/**
	 * Обрабатывает запрос GET: выводит таблицу выдач на выбранном языке.
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
		String lang = chooseLanguage(request);
		ResourceBundle res = ResourceBundle.getBundle("Issues",
				Locale.forLanguageTag(lang));
		String reader = request.getParameter("reader");
		List<Issue> found = IssueStore.byReader(reader);

		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"" + lang + "\"><head><meta charset=\"UTF-8\">");
		out.println("<title>" + res.getString("title") + "</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
		out.println("</head><body>");
		out.println("<p class=\"lang\"><a href=\"issues?lang=ru\">RU</a> "
				+ "<a href=\"issues?lang=en\">EN</a></p>");
		out.println("<h1>" + res.getString("title") + "</h1>");
		if (reader != null && !reader.isBlank()) {
			out.println("<p>" + res.getString("reader") + ": <b>"
					+ esc(reader) + "</b></p>");
		}
		if (found.isEmpty()) {
			out.println("<p class=\"empty\">" + res.getString("empty") + "</p>");
		} else {
			out.println("<table><tr><th>" + res.getString("reader") + "</th><th>"
					+ res.getString("book") + "</th><th>"
					+ res.getString("issued") + "</th><th>"
					+ res.getString("due") + "</th><th>"
					+ res.getString("returned") + "</th></tr>");
			int shown = 0;
			for (Issue issue : found) {
				if (shown++ >= maxRows) {
					break;
				}
				out.println("<tr><td>" + esc(issue.getReader())
						+ "</td><td>" + esc(issue.getBook())
						+ "</td><td>" + issue.getIssued() + "</td><td>"
						+ issue.getDue() + "</td><td>"
						+ res.getString(issue.isReturned() ? "yes" : "no")
						+ "</td></tr>");
			}
			out.println("</table>");
		}
		out.println("<p><a href=\"index.html\">" + res.getString("back")
				+ "</a></p>");
		out.println("</body></html>");
	}
}
