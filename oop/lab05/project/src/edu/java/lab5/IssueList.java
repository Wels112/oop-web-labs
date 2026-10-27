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

// Подключение библиотеки файлового ввода-вывода
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

/**
 * Экранная форма «Картотека выдач» библиотеки с сохранением и загрузкой
 * данных из файла. Список выдач загружается из текстового файла кнопкой
 * «Открыть список выдач» и сохраняется в файл кнопкой «Сохранить список
 * выдач»; изменить отметку о возврате книги позволяет кнопка
 * «Редактировать запись».
 *
 * @author Рябов И.А., группа 5315
 * @version 1.0
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

	/** Названия столбцов таблицы выдач. */
	private String[] columns = { "Читатель", "Книга", "Дата выдачи",
			"Срок возврата", "Возвращена" };

	/** Полный список выдач, из которого отбираются строки при поиске. */
	private String[][] data = {};

	/**
	 * Строит и визуализирует экранную форму «Картотека выдач»,
	 * после чего подключает к её компонентам слушателей событий.
	 */
	public void show() {

		// Создание окна
		issueList = new JFrame("Картотека выдач");
		issueList.setSize(700, 360);
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
		print.setToolTipText("Печать списка выдач");

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

		// Создание пустой таблицы: данные загружаются из файла
		model = new DefaultTableModel(new String[0][0], columns) {
			/**
			 * Запрещает редактирование ячеек таблицы: отметка о возврате
			 * книги изменяется кнопкой «Редактировать запись».
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
		// Таблица занимает всю видимую область прокрутки, поэтому щелчок
		// ниже последней строки тоже попадает в таблицу
		issues.setFillsViewportHeight(true);
		scroll = new JScrollPane(issues);

		// Размещение таблицы с данными
		issueList.add(scroll, BorderLayout.CENTER);

		// Подготовка компонентов поиска
		reader = new JComboBox<String>(new String[] { "Все читатели",
				"Иванов Пётр", "Смирнова Анна", "Кузнецов Илья" });
		bookName = new JTextField("", 15);
		filter = new JButton("Поиск");

		// Добавление компонентов на панель
		JPanel filterPanel = new JPanel();
		filterPanel.add(reader);
		filterPanel.add(bookName);
		filterPanel.add(filter);

		// Размещение панели поиска внизу окна
		issueList.add(filterPanel, BorderLayout.SOUTH);

		// Подключение слушателей событий к компонентам формы
		addListeners();

		// Визуализация экранной формы
		issueList.setVisible(true);
	}

	/**
	 * Подключает слушателей событий к компонентам формы: к кнопкам
	 * загрузки, сохранения, редактирования и поиска, к выпадающему
	 * списку читателей и к таблице выдач.
	 */
	private void addListeners() {

		/*
		 * Слушатель кнопки «Открыть список выдач». Запрашивает имя файла
		 * и загружает его содержимое в таблицу.
		 */
		open.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку загрузки. Показывает
			 * стандартное окно выбора файла в режиме LOAD и, если файл
			 * выбран, загружает его в таблицу.
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
		 * Слушатель кнопки «Сохранить список выдач». Запрашивает имя
		 * файла и записывает в него содержимое таблицы.
		 */
		save.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку сохранения. Показывает
			 * стандартное окно выбора файла в режиме SAVE и, если файл
			 * выбран, записывает в него содержимое таблицы.
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
		 * Слушатель кнопки «Редактировать запись». Позволяет изменить
		 * отметку о возврате книги в выбранной строке таблицы.
		 */
		edit.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку редактирования.
			 * Запрашивает новое значение отметки о возврате книги
			 * для выбранной строки и записывает его в таблицу.
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
				String old = (String) model.getValueAt(row, 4);
				String value = JOptionPane.showInputDialog(issueList,
						"Книга возвращена (Да или Нет):", old);
				if (value != null) {
					model.setValueAt(value, row, 4);
					refreshData();
				}
			}
		});

		/*
		 * Слушатель события ActionEvent для кнопки «Поиск».
		 * Контролирует корректность условий поиска: вызов метода
		 * checkQuery помещён в блок try, а блоки catch обрабатывают
		 * собственные исключения EmptyQueryException и ShortNameException.
		 */
		filter.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку «Поиск». Проверяет условия
			 * поиска, отбирает подходящие строки и выводит их в таблицу.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				String selectedReader = (String) reader.getSelectedItem();
				String book = bookName.getText().trim();
				try {
					checkQuery(selectedReader, book);
					String[][] found = selectRows(selectedReader, book);
					fillTable(found);
					JOptionPane.showMessageDialog(issueList,
							"Найдено записей: " + found.length);
				} catch (EmptyQueryException e) {
					JOptionPane.showMessageDialog(issueList, e.getMessage(),
							"Пустой запрос", JOptionPane.WARNING_MESSAGE);
				} catch (ShortNameException e) {
					JOptionPane.showMessageDialog(issueList, e.getMessage(),
							"Короткое название",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		/*
		 * Слушатель события ItemEvent для выпадающего списка читателей.
		 */
		reader.addItemListener(new ItemListener() {
			/**
			 * Вызывается при изменении выбора в выпадающем списке.
			 * Реагирует только на выбор нового элемента и сообщает,
			 * сколько книг числится за выбранным читателем.
			 *
			 * @param event событие изменения выбора в списке
			 */
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					String selectedReader = (String) event.getItem();
					int count = selectRows(selectedReader, "").length;
					JOptionPane.showMessageDialog(issueList,
							"Выбран читатель: " + selectedReader
									+ "\nЗаписей о выдаче: " + count);
				}
			}
		});

		/*
		 * Слушатель события MouseEvent для таблицы выдач. Контролирует
		 * стандартное исключение ArrayIndexOutOfBoundsException, которое
		 * возникает при обращении к строке с номером −1.
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
	 * Показывает стандартное окно выбора файла и возвращает полное имя
	 * выбранного файла.
	 *
	 * @param title заголовок окна выбора файла
	 * @param mode  режим окна: FileDialog.LOAD или FileDialog.SAVE
	 * @return полное имя файла или null, если пользователь отказался
	 *         от выбора
	 */
	private String chooseFile(String title, int mode) {
		FileDialog dialog = new FileDialog(issueList, title, mode);
		dialog.setFile("*.txt");
		dialog.setVisible(true);
		if (dialog.getFile() == null) {
			return null;
		}
		return dialog.getDirectory() + dialog.getFile();
	}

	/**
	 * Сохраняет содержимое таблицы выдач в текстовом файле. Значение
	 * каждой ячейки записывается в отдельной строке файла, строки
	 * таблицы следуют друг за другом. Запись выполняется буферизованным
	 * символьным потоком в кодировке UTF-8.
	 *
	 * @param fileName полное имя файла, в который сохраняются данные
	 */
	private void saveToFile(String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(fileName, StandardCharsets.UTF_8));
			for (int i = 0; i < model.getRowCount(); i++) {
				for (int j = 0; j < model.getColumnCount(); j++) {
					writer.write((String) model.getValueAt(i, j));
					writer.write("\n");
				}
			}
			writer.close();
			JOptionPane.showMessageDialog(issueList,
					"Сохранено записей: " + model.getRowCount()
							+ "\nФайл: " + fileName);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(issueList,
					"Ошибка записи в файл:\n" + e.getMessage(),
					"Ошибка", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Загружает список выдач из текстового файла в таблицу. Таблица
	 * предварительно очищается, затем файл читается построчно
	 * буферизованным символьным потоком: пять подряд идущих строк файла
	 * образуют одну строку таблицы.
	 *
	 * @param fileName полное имя файла, из которого загружаются данные
	 */
	private void loadFromFile(String fileName) {
		try {
			BufferedReader input = new BufferedReader(
					new FileReader(fileName, StandardCharsets.UTF_8));
			int rows = model.getRowCount();
			for (int i = 0; i < rows; i++) {
				model.removeRow(0);
			}
			String readerName;
			do {
				readerName = input.readLine();
				if (readerName != null) {
					String book = input.readLine();
					String issued = input.readLine();
					String due = input.readLine();
					String returned = input.readLine();
					model.addRow(new String[] { readerName, book, issued,
							due, returned });
				}
			} while (readerName != null);
			input.close();
			refreshData();
			JOptionPane.showMessageDialog(issueList,
					"Загружено записей: " + model.getRowCount()
							+ "\nФайл: " + fileName);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(issueList,
					"Файл не найден:\n" + e.getMessage(),
					"Ошибка", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(issueList,
					"Ошибка чтения файла:\n" + e.getMessage(),
					"Ошибка", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Переносит содержимое таблицы в полный список выдач, по которому
	 * выполняется поиск. Вызывается после загрузки данных из файла
	 * и после изменения отметки о возврате книги.
	 */
	private void refreshData() {
		data = new String[model.getRowCount()][columns.length];
		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < columns.length; j++) {
				data[i][j] = (String) model.getValueAt(i, j);
			}
		}
	}

	/**
	 * Проверяет условия поиска и генерирует исключение, если условия
	 * заданы неверно.
	 *
	 * @param selectedReader имя читателя или «Все читатели», если читатель
	 *                       не выбран
	 * @param book           название книги, введённое пользователем
	 * @throws EmptyQueryException если не задано ни одно условие отбора
	 * @throws ShortNameException  если название книги состоит
	 *                             из одного символа
	 */
	private void checkQuery(String selectedReader, String book)
			throws EmptyQueryException, ShortNameException {
		if (selectedReader.equals("Все читатели") && book.isEmpty()) {
			throw new EmptyQueryException();
		}
		if (book.length() == 1) {
			throw new ShortNameException(book);
		}
	}

	/**
	 * Составляет текст карточки выдачи по номеру строки таблицы.
	 *
	 * @param row номер строки таблицы
	 * @return текст карточки — названия полей и их значения
	 * @throws ArrayIndexOutOfBoundsException если строки с таким
	 *                                        номером в таблице нет
	 */
	private String makeCard(int row) throws ArrayIndexOutOfBoundsException {
		String card = "";
		for (int i = 0; i < columns.length; i++) {
			card = card + columns[i] + ": "
					+ model.getValueAt(row, i) + "\n";
		}
		return card;
	}

	/**
	 * Отбирает из полного списка выдач строки, удовлетворяющие
	 * условиям поиска.
	 *
	 * @param selectedReader имя читателя или «Все читатели», если читатель
	 *                       не задан
	 * @param book           часть названия книги; пустая строка означает,
	 *                       что название не задано
	 * @return массив отобранных строк
	 */
	private String[][] selectRows(String selectedReader, String book) {
		int count = 0;
		for (int i = 0; i < data.length; i++) {
			if (rowMatches(data[i], selectedReader, book)) {
				count++;
			}
		}
		String[][] found = new String[count][columns.length];
		int k = 0;
		for (int i = 0; i < data.length; i++) {
			if (rowMatches(data[i], selectedReader, book)) {
				found[k] = data[i];
				k++;
			}
		}
		return found;
	}

	/**
	 * Проверяет, удовлетворяет ли строка таблицы условиям поиска.
	 *
	 * @param row            проверяемая строка
	 * @param selectedReader имя читателя или «Все читатели»
	 * @param book           часть названия книги
	 * @return true, если строка удовлетворяет обоим условиям
	 */
	private boolean rowMatches(String[] row, String selectedReader,
			String book) {
		boolean byReader = selectedReader.equals("Все читатели")
				|| row[0].equals(selectedReader);
		boolean byBook = book.isEmpty()
				|| row[1].toLowerCase().contains(book.toLowerCase());
		return byReader && byBook;
	}

	/**
	 * Заменяет содержимое таблицы выдач переданными строками.
	 *
	 * @param rows строки, которые требуется вывести в таблицу
	 */
	private void fillTable(String[][] rows) {
		model.setRowCount(0);
		for (int i = 0; i < rows.length; i++) {
			model.addRow(rows[i]);
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
