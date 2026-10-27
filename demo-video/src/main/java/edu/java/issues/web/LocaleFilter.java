package edu.java.issues.web;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.jstl.core.Config;

/**
 * Фильтр выбора языка интерфейса (Web, лабораторная работа № 4).
 * Язык берётся из параметра <code>lang</code> (ru или en), иначе из сессии,
 * иначе из заголовка Accept-Language браузера. Выбор запоминается в сессии.
 * Фильтр также задаёт кодировку UTF-8 для параметров запроса: без этого
 * русские буквы из форм приходят искажёнными.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class LocaleFilter implements Filter {

	/** Поддерживаемые языки. */
	private static final Set<String> SUPPORTED = Set.of("ru", "en");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		HttpServletRequest http = (HttpServletRequest) request;
		String lang = http.getParameter("lang");
		HttpSession session = http.getSession(false);
		if (lang != null && SUPPORTED.contains(lang)) {
			http.getSession(true).setAttribute(WebSupport.LANG, lang);
		} else {
			lang = session == null ? null
					: (String) session.getAttribute(WebSupport.LANG);
		}
		if (lang == null) {
			Locale preferred = http.getLocale();
			lang = preferred != null && "en".equals(preferred.getLanguage())
					? "en" : "ru";
		}
		Locale locale = Locale.forLanguageTag(lang);
		request.setAttribute(WebSupport.LOCALE, locale);
		// Те же настройки использует библиотека тегов JSTL fmt
		Config.set(request, Config.FMT_LOCALE, locale);
		chain.doFilter(request, response);
	}
}
