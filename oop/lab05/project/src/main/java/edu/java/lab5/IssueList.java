package edu.java.lab5;

// Подключение графических библиотек
import java.awt.BorderLayout;
import java.awt.FileDialog;
// Подключение библиотеки обработки событий
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
 * Лабораторная работа № 5: Сохранение и загрузка данных из файла.
 *
 * @author Рябов И.А., группа 5315
 * @version 5.0
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
	 * и подключает к её компонентам слушателей событий
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

		// Подключение слушателей событий к компонентам формы
		addListeners();

		// Визуализация экранной формы
		issueList.setVisible(true);
	}

	/**
	 * Подключает слушателей событий к компонентам формы (лабораторная
	 * работа № 3).
	 */
	private void addListeners() {

		/* Слушатель кнопки «Добавить запись»: запрашивает данные новой выдачи. */
		add.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку добавления.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				addIssue();
			}
		});

		/*
		 * Слушатель кнопки «Редактировать запись»: изменяет отметку
		 * о возврате книги в выбранной строке таблицы.
		 */
		edit.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку редактирования.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				int row = issues.getSelectedRow();
				if (row < 0) {
					JOptionPane.showMessageDialog(issueList,
							"Выберите строку в таблице");
					return;
				}
				String[] issue = shown.get(row);
				String value = JOptionPane.showInputDialog(issueList,
						"Книга возвращена (Да или Нет):", issue[4]);
				if (value != null) {
					issue[4] = value.trim().equalsIgnoreCase("Да") ? "Да" : "Нет";
					fillTable(shown);
				}
			}
		});

		/* Слушатель кнопки «Удалить запись»: удаляет выбранную строку. */
		delete.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку удаления.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				int row = issues.getSelectedRow();
				if (row < 0) {
					JOptionPane.showMessageDialog(issueList,
							"Выберите строку в таблице");
					return;
				}
				String[] issue = shown.get(row);
				int answer = JOptionPane.showConfirmDialog(issueList,
						"Удалить запись «" + issue[1] + "»?", "Удаление",
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {
					all.remove(issue);
					shown.remove(issue);
					fillTable(shown);
				}
			}
		});

		/*
		 * Слушатель кнопки «Открыть список выдач»: запрашивает имя файла
		 * и загружает его содержимое в таблицу.
		 */
		open.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку загрузки.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				String fileName = chooseFile("Загрузка списка выдач",
						FileDialog.LOAD);
				if (fileName != null) {
					loadFromFile(fileName);
				}
			}
		});

		/*
		 * Слушатель кнопки «Сохранить список выдач»: запрашивает имя файла
		 * и записывает в него содержимое таблицы.
		 */
		save.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку сохранения.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				String fileName = chooseFile("Сохранение списка выдач",
						FileDialog.SAVE);
				if (fileName != null) {
					saveToFile(fileName);
				}
			}
		});

		/*
		 * Слушатель события ActionEvent для кнопки «Поиск».
		 * Контролирует корректность условий поиска: вызов checkQuery
		 * помещён в блок try, а блоки catch обрабатывают собственные
		 * исключения EmptyQueryException и ShortNameException
		 * (лабораторная работа № 4).
		 */
		filter.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку «Поиск».
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				String selectedReader = (String) reader.getSelectedItem();
				String book = bookName.getText().trim();
				try {
					IssueLogic.checkQuery(selectedReader, book);
					List<String[]> found = IssueLogic.selectRows(all,
							selectedReader, book);
					fillTable(found);
					JOptionPane.showMessageDialog(issueList,
							"Найдено записей: " + found.size());
				} catch (EmptyQueryException e) {
					JOptionPane.showMessageDialog(issueList, e.getMessage(),
							"Пустой запрос", JOptionPane.WARNING_MESSAGE);
				} catch (ShortNameException e) {
					JOptionPane.showMessageDialog(issueList, e.getMessage(),
							"Короткое название", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		/* Слушатель кнопки «Показать все»: сбрасывает условия поиска. */
		showAll.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку «Показать все».
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				reader.setSelectedIndex(0);
				bookName.setText("");
				fillTable(all);
			}
		});

		/* Слушатель события ItemEvent для выпадающего списка читателей. */
		reader.addItemListener(new ItemListener() {
			/**
			 * Вызывается при изменении выбора в выпадающем списке. Реагирует
			 * только на выбор нового элемента и сообщает, сколько книг
			 * числится за выбранным читателем.
			 *
			 * @param event событие изменения выбора в списке
			 */
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					String selectedReader = (String) event.getItem();
					int count = IssueLogic.selectRows(all, selectedReader,
							"").size();
					JOptionPane.showMessageDialog(issueList,
							"Выбран читатель: " + selectedReader
									+ "\nЗаписей о выдаче: " + count);
				}
			}
		});

		/*
		 * Слушатель события MouseEvent для таблицы выдач. Контролирует
		 * стандартное исключение ArrayIndexOutOfBoundsException, которое
		 * возникает при обращении к строке с номером -1.
		 */
		issues.addMouseListener(new MouseAdapter() {
			/**
			 * Вызывается при щелчке мышью по таблице. При двойном щелчке
			 * выводит карточку выдачи — все поля выбранной строки.
			 *
			 * @param event событие щелчка мышью
			 */
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					try {
						JOptionPane.showMessageDialog(issueList,
								makeCard(issues.getSelectedRow()),
								"Карточка выдачи",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (ArrayIndexOutOfBoundsException e) {
						JOptionPane.showMessageDialog(issueList,
								"Строка не выбрана.\n" + e.toString(),
								"Ошибка", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
	}

	/**
	 * Запрашивает данные новой выдачи и добавляет запись в список.
	 */
	private void addIssue() {
		JComboBox<String> who = new JComboBox<String>(new String[] {
				"Иванов Пётр", "Смирнова Анна", "Кузнецов Илья" });
		JTextField book = new JTextField(18);
		JTextField issued = new JTextField("01.11.2026");
		JTextField due = new JTextField("01.12.2026");
		JPanel panel = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
		panel.add(new JLabel("Читатель:"));
		panel.add(who);
		panel.add(new JLabel("Книга:"));
		panel.add(book);
		panel.add(new JLabel("Дата выдачи:"));
		panel.add(issued);
		panel.add(new JLabel("Срок возврата:"));
		panel.add(due);
		int answer = JOptionPane.showConfirmDialog(issueList, panel,
				"Новая запись о выдаче", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION) {
			return;
		}
		if (book.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(issueList, "Укажите название книги",
					"Неверные данные", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String[] issue = { (String) who.getSelectedItem(),
				book.getText().trim(), issued.getText().trim(),
				due.getText().trim(), "Нет" };
		all.add(issue);
		fillTable(all);
	}

	/**
	 * Составляет текст карточки выдачи по номеру строки таблицы.
	 *
	 * @param row номер строки таблицы
	 * @return текст карточки — названия полей и их значения
	 * @throws ArrayIndexOutOfBoundsException если строки с таким номером
	 *                                        в таблице нет
	 */
	private String makeCard(int row) throws ArrayIndexOutOfBoundsException {
		String card = "";
		for (int i = 0; i < columns.length; i++) {
			card = card + columns[i] + ": " + model.getValueAt(row, i) + "\n";
		}
		return card;
	}

	/**
	 * Показывает стандартное окно выбора файла и возвращает полное имя
	 * выбранного файла.
	 *
	 * @param title заголовок окна выбора файла
	 * @param mode  режим окна: FileDialog.LOAD или FileDialog.SAVE
	 * @return полное имя файла или null, если пользователь отказался
	 */
	private String chooseFile(String title, int mode) {
		FileDialog dialog = new FileDialog(issueList, title, mode);
		dialog.setDirectory(new File("data").getAbsolutePath());
		dialog.setFile("*.txt");
		dialog.setVisible(true);
		if (dialog.getFile() == null) {
			return null;
		}
		return dialog.getDirectory() + dialog.getFile();
	}

	/**
	 * Сохраняет содержимое таблицы в текстовом файле. Значение каждой ячейки
	 * записывается в отдельной строке файла. Запись выполняется
	 * буферизованным символьным потоком в кодировке UTF-8.
	 *
	 * @param fileName полное имя файла, в который сохраняются данные
	 */
	void saveToFile(String fileName) {
		try {
			java.io.BufferedWriter writer = new java.io.BufferedWriter(
					new java.io.FileWriter(fileName,
							java.nio.charset.StandardCharsets.UTF_8));
			for (String[] row : all) {
				for (String cell : row) {
					writer.write(cell);
					writer.write("\n");
				}
			}
			writer.close();
			JOptionPane.showMessageDialog(issueList,
					"Сохранено записей: " + all.size() + "\nФайл: " + fileName);
		} catch (java.io.IOException e) {
			JOptionPane.showMessageDialog(issueList,
					"Ошибка записи в файл:\n" + e.getMessage(), "Ошибка",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Загружает список выдач из текстового файла в таблицу: пять подряд
	 * идущих строк файла образуют одну строку таблицы.
	 *
	 * @param fileName полное имя файла, из которого загружаются данные
	 */
	void loadFromFile(String fileName) {
		try {
			java.io.BufferedReader input = new java.io.BufferedReader(
					new java.io.FileReader(fileName,
							java.nio.charset.StandardCharsets.UTF_8));
			List<String[]> loaded = new ArrayList<String[]>();
			String readerName;
			while ((readerName = input.readLine()) != null) {
				String book = input.readLine();
				String issued = input.readLine();
				String due = input.readLine();
				String returned = input.readLine();
				loaded.add(new String[] { readerName, book, issued, due,
						returned });
			}
			input.close();
			all = loaded;
			fillTable(all);
			JOptionPane.showMessageDialog(issueList,
					"Загружено записей: " + all.size() + "\nФайл: " + fileName);
		} catch (java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(issueList,
					"Файл не найден:\n" + e.getMessage(), "Ошибка",
					JOptionPane.ERROR_MESSAGE);
		} catch (java.io.IOException e) {
			JOptionPane.showMessageDialog(issueList,
					"Ошибка чтения файла:\n" + e.getMessage(), "Ошибка",
					JOptionPane.ERROR_MESSAGE);
		}
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
