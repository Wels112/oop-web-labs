package edu.java.issues.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Тесты входа и ролей (Web, лабораторные работы № 6 и № 9).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
class AuthTest {

	/** Верный пароль даёт роль, неверный и неизвестный пользователь — нет. */
	@Test
	void userStoreChecksPasswords() {
		UserStore store = UserStore.getInstance();
		assertEquals(Role.LIBRARIAN,
				store.authenticate("librarian", "lib123").orElseThrow());
		assertEquals(Role.READER,
				store.authenticate("reader", "read123").orElseThrow());
		assertTrue(store.authenticate("librarian", "wrong").isEmpty());
		assertTrue(store.authenticate("nobody", "lib123").isEmpty());
		assertTrue(store.authenticate(null, null).isEmpty());
	}

	/** Адрес возврата после входа не может вести на чужой сайт. */
	@Test
	void loginRedirectStaysInsideApplication() {
		assertEquals("/issues", LoginServlet.safeNext("https://evil.example/"));
		assertEquals("/issues", LoginServlet.safeNext("//evil.example"));
		assertEquals("/issues", LoginServlet.safeNext(null));
		assertEquals("/about.jsp", LoginServlet.safeNext("/about.jsp"));
	}

	/** Страницы входа и оформления открыты, страницы данных закрыты. */
	@Test
	void publicPaths() {
		assertTrue(AuthFilter.isPublic("/login"));
		assertTrue(AuthFilter.isPublic("/css/style.css"));
		assertFalse(AuthFilter.isPublic("/issues"));
		assertFalse(AuthFilter.isPublic("/gwt-app/issuegwt/issueService"));
	}

	/** Читателю запрещено менять данные, библиотекарю разрешено. */
	@Test
	void readerCannotPostChanges() throws Exception {
		AuthFilter filter = new AuthFilter();

		FilterChain chain = mock(FilterChain.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		filter.doFilter(requestFor(Role.READER), response, chain);
		verify(response).sendError(
				org.mockito.ArgumentMatchers.eq(HttpServletResponse.SC_FORBIDDEN),
				anyString());
		verify(chain, never()).doFilter(
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any());

		FilterChain chain2 = mock(FilterChain.class);
		HttpServletRequest librarian = requestFor(Role.LIBRARIAN);
		filter.doFilter(librarian, mock(HttpServletResponse.class), chain2);
		verify(chain2).doFilter(
				org.mockito.ArgumentMatchers.eq(librarian),
				org.mockito.ArgumentMatchers.any());
	}

	/** Без входа запрос перенаправляется на страницу входа. */
	@Test
	void anonymousIsRedirectedToLogin() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/issues");
		when(request.getContextPath()).thenReturn("");
		when(request.getMethod()).thenReturn("GET");
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.encodeRedirectURL(anyString()))
				.thenAnswer(call -> call.getArgument(0));
		new AuthFilter().doFilter(request, response, mock(FilterChain.class));
		verify(response).sendRedirect("/login?next=%2Fissues");
	}

	/**
	 * Создаёт заглушку POST-запроса к /issues от пользователя с ролью.
	 *
	 * @param role роль пользователя
	 * @return заглушка запроса
	 */
	private HttpServletRequest requestFor(Role role) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpSession session = mock(HttpSession.class);
		when(request.getRequestURI()).thenReturn("/issues");
		when(request.getContextPath()).thenReturn("");
		when(request.getMethod()).thenReturn("POST");
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(WebSupport.ROLE)).thenReturn(role);
		return request;
	}
}
