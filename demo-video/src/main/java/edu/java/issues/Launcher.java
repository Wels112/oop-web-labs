package edu.java.issues;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import edu.java.issues.exception.StorageException;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.ui.IssueListForm;

/**
 * Точка входа всего проекта: один запуск открывает окно Swing (ООП,
 * лабораторные работы № 1-10) и веб-приложение на встроенном Tomcat
 * (Web, лабораторные работы № 1-9). Оба интерфейса работают с одним
 * хранилищем {@link IssueRepository}.
 * Параметры: <code>--no-gui</code> — только сайт, <code>-Dhttp.port=8080</code>
 * и <code>-Dhttps.port=8443</code> — порты (0 отключает HTTPS).
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class Launcher {

	/** Журнал запуска (ООП, лабораторная работа № 10). */
	private static final Logger log = LogManager.getLogger(Launcher.class);

	/** Каталог веб-приложения: страницы JSP, стили, дескриптор web.xml. */
	private static final String WEBAPP = "src/main/webapp";

	/** Файл хранилища ключей с самоподписанным сертификатом для HTTPS. */
	private static final String KEYSTORE = "conf/keystore.p12";

	/**
	 * Запускает приложение.
	 *
	 * @param args <code>--no-gui</code> отключает окно Swing
	 * @throws Exception если Tomcat не удалось запустить
	 */
	public static void main(String[] args) throws Exception {
		boolean gui = !GraphicsEnvironment.isHeadless();
		for (String arg : args) {
			if ("--no-gui".equals(arg)) {
				gui = false;
			}
		}
		int httpPort = Integer.getInteger("http.port", 8080);
		int httpsPort = Integer.getInteger("https.port", 8443);

		loadInitialData();
		Tomcat tomcat = startTomcat(httpPort, httpsPort);
		String url = "http://localhost:" + httpPort + "/";
		log.info("Сайт запущен: {}", url);
		if (httpsPort > 0 && new File(KEYSTORE).isFile()) {
			log.info("HTTPS (самоподписанный сертификат): https://localhost:{}/",
					httpsPort);
		}
		if (gui) {
			SwingUtilities.invokeLater(() -> {
				try {
					UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					log.warn("Системный внешний вид недоступен: {}",
							e.getMessage());
				}
				new IssueListForm(url).show();
			});
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				tomcat.stop();
				tomcat.destroy();
			} catch (LifecycleException e) {
				log.warn("Ошибка остановки Tomcat: {}", e.getMessage());
			}
		}));
		tomcat.getServer().await();
	}

	/** Загружает начальные данные из XML-файла в общее хранилище. */
	private static void loadInitialData() {
		Path xml = AppPaths.XML_FILE;
		if (!Files.isRegularFile(xml)) {
			log.warn("Файл данных {} не найден, картотека пуста", xml);
			return;
		}
		try {
			List<Issue> issues = new XmlIssueStorage().load(xml.toString());
			IssueRepository.getInstance().replaceAll(issues);
		} catch (StorageException e) {
			log.error("Не удалось загрузить {}: {}", xml, e.getMessage());
		}
	}

	/**
	 * Настраивает и запускает встроенный Tomcat (Web, лабораторные работы
	 * № 1 и № 6).
	 *
	 * @param httpPort  порт HTTP
	 * @param httpsPort порт HTTPS или 0, чтобы не открывать его
	 * @return запущенный сервер
	 * @throws LifecycleException если сервер не запустился
	 */
	private static Tomcat startTomcat(int httpPort, int httpsPort)
			throws LifecycleException {
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
		tomcat.setPort(httpPort);
		tomcat.getConnector().setURIEncoding("UTF-8");

		File keystore = new File(KEYSTORE);
		if (httpsPort > 0 && keystore.isFile()) {
			Connector https = new Connector(
					"org.apache.coyote.http11.Http11NioProtocol");
			https.setPort(httpsPort);
			https.setScheme("https");
			https.setSecure(true);
			https.setProperty("SSLEnabled", "true");
			https.setURIEncoding("UTF-8");
			SSLHostConfig ssl = new SSLHostConfig();
			SSLHostConfigCertificate cert = new SSLHostConfigCertificate(ssl,
					SSLHostConfigCertificate.Type.RSA);
			cert.setCertificateKeystoreFile(keystore.getAbsolutePath());
			cert.setCertificateKeystoreType("PKCS12");
			cert.setCertificateKeystorePassword(
					System.getProperty("keystore.password", "changeit"));
			ssl.addCertificate(cert);
			https.addSslHostConfig(ssl);
			tomcat.getService().addConnector(https);
		}

		Context context = tomcat.addWebapp("",
				new File(WEBAPP).getAbsolutePath());
		// Классы приложения берутся из общего загрузчика классов, чтобы окно
		// Swing и сайт использовали один экземпляр хранилища данных
		context.setParentClassLoader(Launcher.class.getClassLoader());
		tomcat.start();
		return tomcat;
	}
}
