package edu.java.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Обработчик формы со страницы <code>ask.jsp</code> (лабораторная работа
 * № 5). Обработка выполняется на Java: сервлет проверяет имя читателя,
 * отбирает его выдачи и передаёт результат странице <code>issues.jsp</code>
 * через атрибуты запроса. Об ошибках пользователю сообщает страница
 * <code>error.jsp</code>.
 *
 * @author Рябов И.А., группа 5315
 * @version 5.0
 */
public class IssueProcessor extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/**
	 * Обрабатывает отправку формы: параметр <code>reader</code>.
	 *
	 * @param request  запрос браузера
	 * @param response ответ сервера
	 * @throws ServletException при ошибке сервлета
	 * @throws IOException      при ошибке записи ответа
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String reader = request.getParameter("reader");
		if (reader == null || reader.isBlank()) {
			fail(request, response,
					"Необходимо ввести имя читателя в форму.");
			return;
		}
		if (!IssueStore.readers().contains(reader)) {
			fail(request, response, "Читатель «" + reader + "» не найден.");
			return;
		}
		List<Issue> found = IssueStore.byReader(reader);
		request.setAttribute("reader", reader);
		request.setAttribute("issues", found);
		request.getRequestDispatcher("/issues.jsp").forward(request, response);
	}

	/**
	 * Передаёт сообщение об ошибке странице error.jsp.
	 *
	 * @param request  запрос браузера
	 * @param response ответ сервера
	 * @param message  текст сообщения
	 * @throws ServletException при ошибке сервлета
	 * @throws IOException      при ошибке записи ответа
	 */
	private void fail(HttpServletRequest request, HttpServletResponse response,
			String message) throws ServletException, IOException {
		request.setAttribute("message", message);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		request.getRequestDispatcher("/error.jsp").forward(request, response);
	}
}
