package edu.java.web;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

/**
 * Запуск приложения во встроенном Tomcat прямо из среды разработки
 * (Run As, Java Application). Тот же web-проект можно собрать командой
 * Maven install и развернуть в отдельном Tomcat 10.1: файл war появится
 * в каталоге target. Адрес приложения:
 * <code>http://localhost:8080/web09/</code>.
 *
 * @author Рябов И.А., группа 5315
 * @version 9.0
 */
public class DevServer {

	/**
	 * Запускает Tomcat и ждёт остановки.
	 *
	 * @param args аргументы командной строки, не используются
	 * @throws Exception если сервер не удалось запустить
	 */
	public static void main(String[] args) throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
		tomcat.setPort(8080);
		Connector http = tomcat.getConnector();
		http.setURIEncoding("UTF-8");
		// Защищённый порт 8443 (лабораторная работа № 6)
		http.setRedirectPort(8443);
		File keystore = new File("conf/keystore.p12");
		if (keystore.isFile()) {
			Connector https = new Connector(
					"org.apache.coyote.http11.Http11NioProtocol");
			https.setPort(8443);
			https.setScheme("https");
			https.setSecure(true);
			https.setProperty("SSLEnabled", "true");
			https.setURIEncoding("UTF-8");
			SSLHostConfig ssl = new SSLHostConfig();
			SSLHostConfigCertificate cert = new SSLHostConfigCertificate(ssl,
					SSLHostConfigCertificate.Type.RSA);
			cert.setCertificateKeystoreFile(keystore.getAbsolutePath());
			cert.setCertificateKeystoreType("PKCS12");
			cert.setCertificateKeystorePassword("changeit");
			ssl.addCertificate(cert);
			https.addSslHostConfig(ssl);
			tomcat.getService().addConnector(https);
		}
		// Пользователи и роли: то же, что в conf/tomcat-users.xml для отдельного Tomcat
		tomcat.addUser("librarian", "lib123");
		tomcat.addRole("librarian", "library");
		Context context = tomcat.addWebapp("/web09",
				new File("src/main/webapp").getAbsolutePath());
		context.setParentClassLoader(DevServer.class.getClassLoader());
		tomcat.start();
		System.out.println("Приложение запущено: http://localhost:8080/web09/");
		System.out.println("Защищённый адрес: https://localhost:8443/web09/");
		System.out.println("Вход: librarian / lib123");
		tomcat.getServer().await();
	}
}
