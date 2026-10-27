package edu.java.issues.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Тест сервлета с mock-объектами Mockito (Web, лабораторная работа № 9):
 * запрос и ответ подменяются заглушками, поэтому Tomcat для проверки
 * не нужен.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
class IssueServletTest {

	/** Проверяемый сервлет. */
	private IssueServlet servlet;

	/** Заглушка запроса. */
	private HttpServletRequest request;

	/** Заглушка ответа. */
	private HttpServletResponse response;

	/** Буфер, в который сервлет пишет страницу. */
	private StringWriter page;

	/** Создаёт сервлет, хранилище с двумя записями и заглушки. */
	@BeforeEach
	void setUp() throws Exception {
		IssueRepository repo = new IssueRepository();
		repo.add(new Issue("Иванов Пётр", "Три мушкетёра", "05.09.2026",
				"05.10.2026", "Да"));
		repo.add(new Issue("Смирнова Анна", "Война <и> мир", "12.09.2026",
				"12.10.2026", "Нет"));

		ServletConfig config = mock(ServletConfig.class);
		when(config.getInitParameter("titleKey")).thenReturn("servlet.title");
		when(config.getInitParameter("maxRows")).thenReturn("50");
		servlet = new IssueServlet();
		servlet.init(config);
		servlet.setRepository(repo);

		request = mock(HttpServletRequest.class);
		when(request.getContextPath()).thenReturn("");
		when(request.getAttribute(WebSupport.LOCALE))
				.thenReturn(Locale.forLanguageTag("ru"));
		response = mock(HttpServletResponse.class);
		page = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(page, true));
	}

	/** Без условий сервлет выводит все записи. */
	@Test
	void showsAllIssues() throws Exception {
		servlet.doGet(request, response);
		verify(response).setContentType("text/html; charset=UTF-8");
		assertTrue(page.toString().contains("Три мушкетёра"));
		assertTrue(page.toString().contains("Смирнова Анна"));
	}

	/** Динамический параметр reader отбирает записи читателя. */
	@Test
	void filtersByReaderParameter() throws Exception {
		when(request.getParameter("reader")).thenReturn("Иванов Пётр");
		servlet.doGet(request, response);
		assertTrue(page.toString().contains("Три мушкетёра"));
		assertFalse(page.toString().contains("Война"));
	}

	/** Текст из базы экранируется: разметка из названия не попадает в HTML. */
	@Test
	void escapesHtml() throws Exception {
		servlet.doGet(request, response);
		assertTrue(page.toString().contains("Война &lt;и&gt; мир"));
		assertFalse(page.toString().contains("Война <и> мир"));
	}

	/** Английская локаль переключает заголовок страницы. */
	@Test
	void usesEnglishBundle() throws Exception {
		when(request.getAttribute(WebSupport.LOCALE))
				.thenReturn(Locale.forLanguageTag("en"));
		servlet.doGet(request, response);
		assertTrue(page.toString().contains("Issue list (servlet)"));
	}

	/** Если ничего не найдено, выводится сообщение, а не пустая таблица. */
	@Test
	void showsEmptyMessage() throws Exception {
		when(request.getParameter("book")).thenReturn("несуществующая");
		servlet.doGet(request, response);
		assertTrue(page.toString().contains("class=\"empty\""));
	}
}
