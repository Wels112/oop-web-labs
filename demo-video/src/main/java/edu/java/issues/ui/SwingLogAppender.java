package edu.java.issues.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;

/**
 * Приёмник сообщений Log4j, который выводит журнал приложения в текстовую
 * область окна (ООП, лабораторная работа № 10). Благодаря ему протокол
 * работы приложения виден прямо в интерфейсе, а не только в файле
 * <code>logs/issues.log</code>.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class SwingLogAppender extends AbstractAppender {

	/** Текстовая область, в которую выводятся сообщения. */
	private final JTextArea area;

	/**
	 * Создаёт приёмник сообщений.
	 *
	 * @param area текстовая область журнала
	 */
	private SwingLogAppender(JTextArea area) {
		super("SwingLog", null, null, true, Property.EMPTY_ARRAY);
		this.area = area;
	}

	/**
	 * Подключает приёмник к корневому регистратору Log4j. В окно попадают
	 * сообщения уровня INFO и выше.
	 *
	 * @param area текстовая область журнала
	 */
	public static void attach(JTextArea area) {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		SwingLogAppender appender = new SwingLogAppender(area);
		appender.start();
		config.addAppender(appender);
		config.getRootLogger().addAppender(appender, Level.INFO, null);
		context.updateLoggers();
	}

	@Override
	public void append(LogEvent event) {
		String name = event.getLoggerName();
		String shortName = name.substring(name.lastIndexOf('.') + 1);
		String line = new SimpleDateFormat("HH:mm:ss").format(
				new Date(event.getTimeMillis())) + " " + event.getLevel()
				+ " [" + event.getThreadName() + "] " + shortName + " - "
				+ event.getMessage().getFormattedMessage() + "\n";
		SwingUtilities.invokeLater(() -> {
			area.append(line);
			area.setCaretPosition(area.getDocument().getLength());
		});
	}
}
