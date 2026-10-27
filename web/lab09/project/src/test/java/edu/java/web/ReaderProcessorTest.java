package edu.java.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Тест сервлета с mock-объектами Mockito (лабораторная работа № 9). Запрос,
 * ответ и сессия подменяются заглушками, поэтому Tomcat для проверки
 * не нужен: проверяются обращения сервлета к контексту и параметрам сеанса.
 *
 * @author Рябов И.А., группа 5315
 * @version 9.0
 */
class ReaderProcessorTest {

	/** Проверяемый сервлет. */
	private ReaderProcessor servlet;

	/** Заглушка запроса. */
	private HttpServletRequest request;

	/** Заглушка ответа. */
	private HttpServletResponse response;

	/** Заглушка сессии. */
	private HttpSession session;

	/** Создаёт сервлет и заглушки перед каждым тестом. */
	@BeforeEach
	void setUp() {
		servlet = new ReaderProcessor();
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		session = mock(HttpSession.class);
		when(request.getSession()).thenReturn(session);
		when(request.getContextPath()).thenReturn("/web09");
		when(response.encodeRedirectURL(anyString()))
				.thenAnswer(call -> call.getArgument(0));
	}

	/** Известный читатель сохраняется в сессии и в cookie, затем redirect. */
	@Test
	void savesReaderAndRedirects() throws Exception {
		when(request.getParameter("reader")).thenReturn("Иванов Пётр");
		servlet.doPost(request, response);

		verify(session).setAttribute("reader", "Иванов Пётр");
		ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
		verify(response).addCookie(cookie.capture());
		assertEquals("book.reader", cookie.getValue().getName());
		assertTrue(cookie.getValue().isHttpOnly());
		verify(response).sendRedirect("/web09/issues.jsp");
	}

	/** Без параметра сервлет возвращает ошибку 400. */
	@Test
	void emptyParameterIsBadRequest() throws Exception {
		when(request.getParameter("reader")).thenReturn("");
		servlet.doPost(request, response);

		verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
				anyString());
		verify(session, never()).setAttribute(anyString(), any());
	}

	/** Неизвестный читатель не попадает в сессию. */
	@Test
	void unknownReaderIsRejected() throws Exception {
		when(request.getParameter("reader")).thenReturn("Неизвестный");
		servlet.doPost(request, response);

		verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
				anyString());
		verify(response, never()).sendRedirect(anyString());
	}

	/** Выдачи отбираются по читателю. */
	@Test
	void storeFiltersByReader() {
		assertEquals(2, IssueStore.byReader("Иванов Пётр").size());
		assertEquals(4, IssueStore.byReader("").size());
	}
}
