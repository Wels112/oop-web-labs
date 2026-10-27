package edu.java.issues.report;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.exception.StorageException;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.model.Issue;
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
 * (ООП, лабораторная работа № 7). Шаблоны отчётов <code>issues_pdf.jrxml</code>
 * и <code>issues_html.jrxml</code> описывают структуру XML-файла, созданного
 * в лабораторной работе № 6, и отличаются оформлением: первый шаблон — плоская
 * таблица на странице A4, второй — альбомная страница с группировкой записей
 * по читателям.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueReportBuilder {

	/** Журнал работы построителя отчётов (лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(IssueReportBuilder.class);

	/** Выражение выборки записей из XML-файла. */
	private static final String SELECT = "/issues/issue";

	/**
	 * Строит отчёт в формате PDF по шаблону плоской таблицы.
	 *
	 * @param xmlFile XML-файл с данными, созданный {@link XmlIssueStorage}
	 * @param out     путь к создаваемому PDF-файлу
	 * @return путь к созданному файлу
	 * @throws StorageException если шаблон или данные не удалось обработать
	 */
	public Path buildPdf(Path xmlFile, Path out) throws StorageException {
		try {
			JasperPrint print = fill("/reports/issues_pdf.jrxml", xmlFile);
			prepare(out);
			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setExporterInput(new SimpleExporterInput(print));
			exporter.setExporterOutput(
					new SimpleOutputStreamExporterOutput(out.toFile()));
			exporter.exportReport();
			log.info("Построен PDF-отчёт {}", out);
			return out;
		} catch (JRException | IOException e) {
			log.error("Ошибка построения PDF-отчёта", e);
			throw new StorageException(
					"Ошибка построения PDF-отчёта: " + e.getMessage(), e);
		}
	}

	/**
	 * Строит отчёт в формате HTML по шаблону с группировкой по читателям.
	 * Шаблон группирует соседние строки с одним читателем, поэтому данные
	 * предварительно упорядочиваются по имени читателя во временной копии
	 * XML-файла.
	 *
	 * @param xmlFile XML-файл с данными, созданный {@link XmlIssueStorage}
	 * @param out     путь к создаваемому HTML-файлу
	 * @return путь к созданному файлу
	 * @throws StorageException если шаблон или данные не удалось обработать
	 */
	public Path buildHtml(Path xmlFile, Path out) throws StorageException {
		Path sorted = null;
		try {
			XmlIssueStorage xml = new XmlIssueStorage();
			List<Issue> issues = new ArrayList<>(
					xml.load(xmlFile.toString()));
			issues.sort(Comparator.comparing(Issue::getReader)
					.thenComparing(Issue::getIssued));
			sorted = Files.createTempFile("issues-sorted", ".xml");
			xml.save(sorted.toString(), issues);
			JasperPrint print = fill("/reports/issues_html.jrxml", sorted);
			prepare(out);
			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterInput(new SimpleExporterInput(print));
			SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(
					out.toFile(), "UTF-8");
			exporter.setExporterOutput(output);
			exporter.exportReport();
			log.info("Построен HTML-отчёт {}", out);
			return out;
		} catch (JRException | IOException e) {
			log.error("Ошибка построения HTML-отчёта", e);
			throw new StorageException(
					"Ошибка построения HTML-отчёта: " + e.getMessage(), e);
		} finally {
			if (sorted != null) {
				try {
					Files.deleteIfExists(sorted);
				} catch (IOException ignored) {
					// временный файл удалит система
				}
			}
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
	private JasperPrint fill(String template, Path xmlFile)
			throws JRException, IOException {
		try (InputStream stream = IssueReportBuilder.class
				.getResourceAsStream(template)) {
			if (stream == null) {
				throw new IOException("Шаблон не найден: " + template);
			}
			JasperReport report = JasperCompileManager.compileReport(stream);
			JRXmlDataSource source = new JRXmlDataSource(
					xmlFile.toFile().getPath(), SELECT);
			return JasperFillManager.fillReport(report, new java.util.HashMap<>(),
					source);
		}
	}

	/**
	 * Создаёт каталог для выходного файла.
	 *
	 * @param out путь к выходному файлу
	 * @throws IOException если каталог не удалось создать
	 */
	private void prepare(Path out) throws IOException {
		Path parent = out.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
	}
}
