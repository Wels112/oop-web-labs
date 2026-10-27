package edu.java.issues.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Общие вспомогательные методы веб-приложения: имена атрибутов сессии,
 * экранирование HTML, защитный токен форм и выбор набора сообщений.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public final class WebSupport {

	/** Атрибут сессии: имя вошедшего пользователя. */
	public static final String USER = "user";

	/** Атрибут сессии: роль вошедшего пользователя. */
	public static final String ROLE = "role";

	/** Атрибут сессии: выбранный язык (Web, лабораторная работа № 4). */
	public static final String LANG = "lang";

	/** Атрибут сессии: читатель, выбранный на странице вопроса
	 * (Web, лабораторная работа № 7). */
	public static final String READER_FILTER = "readerFilter";

	/** Атрибут сессии: одноразовое сообщение после перенаправления. */
	public static final String FLASH = "flash";

	/** Атрибут сессии: защитный токен форм. */
	public static final String CSRF = "csrf";

	/** Атрибут запроса: текущая локаль. */
	public static final String LOCALE = "locale";

	/** Базовое имя файлов сообщений. */
	public static final String BUNDLE = "messages";

	/** Служебный класс: экземпляры не создаются. */
	private WebSupport() {
	}

	/**
	 * Экранирует спецсимволы HTML, чтобы введённый текст нельзя было
	 * использовать для внедрения разметки.
	 *
	 * @param text исходный текст
	 * @return безопасный для вставки в HTML текст
	 */
	public static String esc(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(text.length() + 16);
		for (char c : text.toCharArray()) {
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&#39;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Возвращает защитный токен форм текущей сессии, создавая его при первом
	 * обращении.
	 *
	 * @param session сессия пользователя
	 * @return токен
	 */
	public static String csrfToken(HttpSession session) {
		Object token = session.getAttribute(CSRF);
		if (token == null) {
			byte[] bytes = new byte[24];
			new SecureRandom().nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(bytes);
			session.setAttribute(CSRF, token);
		}
		return (String) token;
	}

	/**
	 * Проверяет токен, присланный формой.
	 *
	 * @param request запрос с формой
	 * @return true, если токен совпадает с токеном сессии
	 */
	public static boolean csrfValid(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		String sent = request.getParameter("csrf");
		if (session == null || sent == null
				|| session.getAttribute(CSRF) == null) {
			return false;
		}
		return MessageDigest.isEqual(
				sent.getBytes(StandardCharsets.UTF_8),
				((String) session.getAttribute(CSRF))
						.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Возвращает локаль запроса, которую выбрал фильтр языка.
	 *
	 * @param request запрос
	 * @return локаль; русская, если фильтр не сработал
	 */
	public static Locale localeOf(HttpServletRequest request) {
		Object locale = request.getAttribute(LOCALE);
		return locale instanceof Locale ? (Locale) locale
				: Locale.forLanguageTag("ru");
	}

	/**
	 * Загружает набор сообщений для локали запроса (Web, лабораторная
	 * работа № 4). Файлы ресурсов читаются в кодировке UTF-8, поэтому
	 * перекодировка строк не нужна.
	 *
	 * @param request запрос
	 * @return набор сообщений
	 */
	public static ResourceBundle bundleOf(HttpServletRequest request) {
		return ResourceBundle.getBundle(BUNDLE, localeOf(request));
	}
}
