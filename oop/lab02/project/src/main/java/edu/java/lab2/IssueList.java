package edu.java.lab2;

// Подключение графических библиотек
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

/**
 * Экранная форма «Картотека выдач» библиотеки.
 * Лабораторная работа № 2: Разработка интерфейса пользователя.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueList {

	/** Главное окно экранной формы. */
	private JFrame issueList;

	/** Модель данных таблицы выдач. */
	private DefaultTableModel model;

	/** Панель инструментов с кнопками управления. */
	private JToolBar toolBar;

	/** Кнопка сохранения списка выдач в файл. */
	private JButton save;

	/** Кнопка загрузки списка выдач из файла. */
	private JButton open;

	/** Кнопка добавления записи о выдаче. */
	private JButton add;

	/** Кнопка редактирования выбранной записи. */
	private JButton edit;

	/** Кнопка удаления выбранной записи. */
	private JButton delete;

	/** Кнопка вывода списка выдач на печать. */
	private JButton print;

	/** Таблица вывода данных о выдачах. */
	private JTable issues;

	/** Прокрутка таблицы выдач. */
	private JScrollPane scroll;

	/** Выпадающий список для выбора читателя. */
	private JComboBox<String> reader;

	/** Поле ввода названия книги. */
	private JTextField bookName;

	/** Кнопка поиска записи о выдаче. */
	private JButton filter;

	/** Кнопка сброса условий поиска. */
	private JButton showAll;

	/** Названия столбцов таблицы выдач. */
	private String[] columns = { "Читатель", "Книга", "Дата выдачи",
			"Срок возврата", "Возвращена" };

	/** Полный список выдач, из которого отбираются строки при поиске. */
	private List<String[]> all = new ArrayList<String[]>();

	/** Строки, которые сейчас показаны в таблице (весь список или результат поиска). */
	private List<String[]> shown = new ArrayList<String[]>();

	/**
	 * Строит и визуализирует экранную форму
	 * .
	 */
	public void show() {

		// Создание окна
		issueList = new JFrame("Картотека выдач");
		issueList.setSize(760, 380);
		issueList.setLocation(100, 100);
		issueList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Создание кнопок и прикрепление иконок
		save = new JButton(new ImageIcon("./img/save.png"));
		open = new JButton(new ImageIcon("./img/open.png"));
		add = new JButton(new ImageIcon("./img/add.png"));
		edit = new JButton(new ImageIcon("./img/edit.png"));
		delete = new JButton(new ImageIcon("./img/delete.png"));
		print = new JButton(new ImageIcon("./img/print.png"));

		// Настройка подсказок для кнопок
		save.setToolTipText("Сохранить список выдач");
		open.setToolTipText("Открыть список выдач");
		add.setToolTipText("Добавить запись о выдаче");
		edit.setToolTipText("Редактировать запись");
		delete.setToolTipText("Удалить запись");
		print.setToolTipText("Отчёты PDF и HTML");

		// Добавление кнопок на панель инструментов
		toolBar = new JToolBar("Панель инструментов");
		toolBar.add(save);
		toolBar.add(open);
		toolBar.add(add);
		toolBar.add(edit);
		toolBar.add(delete);
		toolBar.add(print);

		// Размещение панели инструментов
		issueList.setLayout(new BorderLayout());
		issueList.add(toolBar, BorderLayout.NORTH);

		// Создание таблицы с данными
		model = new DefaultTableModel(new String[0][0], columns) {
			private static final long serialVersionUID = 1L;

			/**
			 * Запрещает редактирование ячеек таблицы.
			 *
			 * @param row    номер строки
			 * @param column номер столбца
			 * @return всегда false
			 */
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		issues = new JTable(model);
		issues.setRowHeight(22);
		issues.setFillsViewportHeight(true);
		scroll = new JScrollPane(issues);
		// Начальные данные: загрузка из файла появится в лабораторной работе № 5
		all.add(new String[] { "Иванов Пётр", "Три мушкетёра", "05.09.2026",
				"05.10.2026", "Да" });
		all.add(new String[] { "Смирнова Анна", "Война и мир", "12.09.2026",
				"12.10.2026", "Нет" });
		all.add(new String[] { "Иванов Пётр", "Анна Каренина", "20.09.2026",
				"20.10.2026", "Нет" });
		all.add(new String[] { "Кузнецов Илья", "Три мушкетёра", "25.09.2026",
				"25.10.2026", "Нет" });
		fillTable(all);

		// Размещение таблицы с данными
		issueList.add(scroll, BorderLayout.CENTER);

		// Подготовка компонентов поиска
		reader = new JComboBox<String>(new String[] { "Все читатели",
				"Иванов Пётр", "Смирнова Анна", "Кузнецов Илья" });
		bookName = new JTextField("", 15);
		filter = new JButton("Поиск");
		showAll = new JButton("Показать все");

		// Добавление компонентов на панель
		JPanel filterPanel = new JPanel();
		filterPanel.add(new JLabel("Читатель:"));
		filterPanel.add(reader);
		filterPanel.add(new JLabel("Книга:"));
		filterPanel.add(bookName);
		filterPanel.add(filter);
		filterPanel.add(showAll);

		// Размещение панели поиска внизу окна
		issueList.add(filterPanel, BorderLayout.SOUTH);

		// Визуализация экранной формы
		issueList.setVisible(true);
	}

	/**
	 * Заменяет содержимое таблицы переданными строками.
	 *
	 * @param rows строки, которые требуется вывести в таблицу
	 */
	private void fillTable(List<String[]> rows) {
		shown = new ArrayList<String[]>(rows);
		model.setRowCount(0);
		for (String[] row : rows) {
			model.addRow(row);
		}
	}

	/**
	 * Точка входа в приложение.
	 *
	 * @param args аргументы командной строки, не используются
	 */
	public static void main(String[] args) {
		// Создание и отображение экранной формы
		new IssueList().show();
	}
}
