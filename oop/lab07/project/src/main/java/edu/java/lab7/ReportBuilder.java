package edu.java.lab7;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * Построение отчётов в форматах PDF и HTML системой JasperReports
 * (лабораторная работа № 7). Шаблоны <code>issues_pdf.jrxml</code>
 * и <code>issues_html.jrxml</code> описывают структуру XML-файла из
 * лабораторной работы № 6 и отличаются оформлением: первый — плоская таблица
 * на странице A4, второй — альбомная страница с группировкой по читателям.
 *
 * @author Рябов И.А., группа 5315
 * @version 7.0
 */
public final class ReportBuilder {

	/** Выражение выборки записей из XML-файла. */
	private static final String SELECT = "/issues/issue";

	/** Служебный класс: экземпляры не создаются. */
	private ReportBuilder() {
	}

	/**
	 * Строит отчёт в формате PDF по шаблону плоской таблицы.
	 *
	 * @param xmlFile XML-файл с данными
	 * @param out     путь к создаваемому PDF-файлу
	 * @return путь к созданному файлу
	 * @throws Exception если шаблон или данные не удалось обработать
	 */
	public static Path buildPdf(Path xmlFile, Path out) throws Exception {
		JasperPrint print = fill("/reports/issues_pdf.jrxml", xmlFile);
		prepare(out);
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setExporterInput(new SimpleExporterInput(print));
		exporter.setExporterOutput(
				new SimpleOutputStreamExporterOutput(out.toFile()));
		exporter.exportReport();
		return out;
	}

	/**
	 * Строит отчёт в формате HTML по шаблону с группировкой по читателям.
	 * Шаблон группирует соседние строки с одним читателем, поэтому данные
	 * предварительно упорядочиваются по имени читателя во временной копии
	 * XML-файла.
	 *
	 * @param xmlFile XML-файл с данными
	 * @param out     путь к создаваемому HTML-файлу
	 * @return путь к созданному файлу
	 * @throws Exception если шаблон или данные не удалось обработать
	 */
	public static Path buildHtml(Path xmlFile, Path out) throws Exception {
		Path sorted = Files.createTempFile("issues-sorted", ".xml");
		try {
			List<String[]> rows = new ArrayList<String[]>(
					XmlIssues.load(xmlFile.toString()));
			rows.sort(Comparator.<String[], String>comparing(r -> r[0])
					.thenComparing(r -> r[2]));
			XmlIssues.save(sorted.toString(), rows);
			JasperPrint print = fill("/reports/issues_html.jrxml", sorted);
			prepare(out);
			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterInput(new SimpleExporterInput(print));
			exporter.setExporterOutput(
					new SimpleHtmlExporterOutput(out.toFile(), "UTF-8"));
			exporter.exportReport();
			return out;
		} finally {
			Files.deleteIfExists(sorted);
		}
	}

	/**
	 * Компилирует шаблон и заполняет его данными из XML-файла.
	 *
	 * @param template путь к шаблону внутри ресурсов приложения
	 * @param xmlFile  XML-файл с данными
	 * @return заполненный отчёт, готовый к экспорту
	 * @throws JRException если шаблон некорректен или заполнение не удалось
	 * @throws IOException если шаблон не найден
	 */
	private static JasperPrint fill(String template, Path xmlFile)
			throws JRException, IOException {
		try (InputStream stream = ReportBuilder.class
				.getResourceAsStream(template)) {
			if (stream == null) {
				throw new IOException("Шаблон не найден: " + template);
			}
			JasperReport report = JasperCompileManager.compileReport(stream);
			JRXmlDataSource source = new JRXmlDataSource(
					xmlFile.toFile().getPath(), SELECT);
			return JasperFillManager.fillReport(report,
					new HashMap<String, Object>(), source);
		}
	}

	/**
	 * Создаёт каталог для выходного файла.
	 *
	 * @param out путь к выходному файлу
	 * @throws IOException если каталог не удалось создать
	 */
	private static void prepare(Path out) throws IOException {
		Path parent = out.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
	}
}
