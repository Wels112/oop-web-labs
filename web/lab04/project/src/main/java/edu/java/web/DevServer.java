package edu.java.web;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

/**
 * Запуск приложения во встроенном Tomcat прямо из среды разработки
 * (Run As, Java Application). Тот же web-проект можно собрать командой
 * Maven install и развернуть в отдельном Tomcat 10.1: файл war появится
 * в каталоге target. Адрес приложения:
 * <code>http://localhost:8080/web04/</code>.
 *
 * @author Рябов И.А., группа 5315
 * @version 4.0
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
		Context context = tomcat.addWebapp("/web04",
				new File("src/main/webapp").getAbsolutePath());
		context.setParentClassLoader(DevServer.class.getClassLoader());
		tomcat.start();
		System.out.println("Приложение запущено: http://localhost:8080/web04/");
		tomcat.getServer().await();
	}
}
