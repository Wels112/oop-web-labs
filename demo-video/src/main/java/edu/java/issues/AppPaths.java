package edu.java.issues;

import java.nio.file.Path;

/**
 * Пути к файлам приложения относительно рабочего каталога (корня проекта).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public final class AppPaths {

	/** Каталог исходных данных. */
	public static final Path DATA_DIR = Path.of("data");

	/** Текстовый файл со списком выдач (ООП, лабораторная работа № 5). */
	public static final Path TEXT_FILE = DATA_DIR.resolve("issues.txt");

	/** XML-файл со списком выдач (ООП, лабораторная работа № 6). */
	public static final Path XML_FILE = DATA_DIR.resolve("issues.xml");

	/** XML-файл, который строит второй поток для отчёта (лабораторная № 8). */
	public static final Path REPORT_XML = Path.of("reports", "out",
			"report-data.xml");

	/** Каталог созданных отчётов. */
	public static final Path OUT_DIR = Path.of("reports", "out");

	/** Файл журнала Log4j (ООП, лабораторная работа № 10). */
	public static final Path LOG_FILE = Path.of("logs", "issues.log");

	/** Служебный класс: экземпляры не создаются. */
	private AppPaths() {
	}
}
