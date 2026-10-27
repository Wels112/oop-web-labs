package edu.java.issues.ui;

// Подключение графических библиотек
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.java.issues.AppPaths;
import edu.java.issues.concurrent.ReportPipeline;
import edu.java.issues.exception.EmptyQueryException;
import edu.java.issues.exception.ShortNameException;
import edu.java.issues.exception.StorageException;
import edu.java.issues.io.TextIssueStorage;
import edu.java.issues.io.XmlIssueStorage;
import edu.java.issues.lab1.ArraySort;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.report.IssueReportBuilder;

/**
 * Экранная форма «Картотека выдач» библиотеки. Это итог цепочки лабораторных
 * работ по объектно-ориентированному программированию: форма (№ 2), события
 * (№ 3), исключения (№ 4), файлы (№ 5), XML (№ 6), отчёты PDF и HTML (№ 7),
 * потоки (№ 8) и журнал Log4j (№ 10). Данные берутся из общего хранилища
 * {@link IssueRepository}, поэтому изменения, сделанные на сайте, сразу
 * появляются в таблице.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueListForm {

	/** Журнал работы окна (лабораторная работа № 10). */
	private static final Logger log = LogManager
			.getLogger(IssueListForm.class);

	/** Адрес веб-приложения (Web, лабораторные работы № 1-9). */
	private final String siteUrl;

	/** Общее хранилище данных. */
	private final IssueRepository repository = IssueRepository.getInstance();

	/** Главное окно экранной формы. */
	private JFrame issueList;

	/** Модель данных таблицы выдач. */
	private DefaultTableModel model;

	/** Таблица вывода данных о выдачах. */
	private JTable issues;

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

	/** Кнопка построения отчётов PDF и HTML. */
	private JButton print;

	/** Выпадающий список для выбора читателя. */
	private JComboBox<String> reader;

	/** Поле ввода названия книги. */
	private JTextField bookName;

	/** Кнопка поиска записей о выдачах. */
	private JButton filter;

	/** Кнопка сброса условий поиска. */
	private JButton showAll;

	/** Строка состояния. */
	private JLabel status;

	/** Область журнала Log4j. */
	private JTextArea logArea;

	/** Названия столбцов таблицы выдач. */
	private final String[] columns = { "Читатель", "Книга", "Дата выдачи",
			"Срок возврата", "Возвращена" };

	/**
	 * Создаёт форму.
	 *
	 * @param siteUrl адрес веб-приложения, который открывает пункт меню
	 */
	public IssueListForm(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	/**
	 * Строит и визуализирует экранную форму, после чего подключает
	 * к её компонентам слушателей событий.
	 */
	public void show() {

		// Создание окна
		issueList = new JFrame("Картотека выдач");
		issueList.setSize(900, 620);
		issueList.setLocation(80, 60);
		issueList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Создание кнопок и прикрепление иконок
		save = new JButton(new ImageIcon("./img/save.png"));
		open = new JButton(new ImageIcon("./img/open.png"));
		add = new JButton(new ImageIcon("./img/add.png"));
		edit = new JButton(new ImageIcon("./img/edit.png"));
		delete = new JButton(new ImageIcon("./img/delete.png"));
		print = new JButton(new ImageIcon("./img/print.png"));

		// Настройка подсказок для кнопок
		save.setToolTipText("Сохранить список выдач (TXT или XML)");
		open.setToolTipText("Открыть список выдач (TXT или XML)");
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

		// Таблица выдач: ячейки не редактируются, правка идёт через диалог
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
		issues.setRowHeight(24);
		issues.setFillsViewportHeight(true);
		JScrollPane scroll = new JScrollPane(issues);

		// Панель поиска
		reader = new JComboBox<String>();
		reader.addItem(IssueRepository.ALL_READERS);
		for (String name : IssueRepository.readers()) {
			reader.addItem(name);
		}
		bookName = new JTextField("", 15);
		filter = new JButton("Поиск");
		showAll = new JButton("Показать все");
		JPanel filterPanel = new JPanel();
		filterPanel.add(new JLabel("Читатель:"));
		filterPanel.add(reader);
		filterPanel.add(new JLabel("Книга:"));
		filterPanel.add(bookName);
		filterPanel.add(filter);
		filterPanel.add(showAll);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(scroll, BorderLayout.CENTER);
		tablePanel.add(filterPanel, BorderLayout.SOUTH);

		// Журнал Log4j в нижней части окна
		logArea = new JTextArea(8, 60);
		logArea.setEditable(false);
		logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Журнал работы (Log4j)"));
		SwingLogAppender.attach(logArea);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				tablePanel, logScroll);
		split.setResizeWeight(0.72);

		status = new JLabel(" Готово");

		// Размещение компонентов
		issueList.setLayout(new BorderLayout());
		issueList.setJMenuBar(createMenuBar());
		issueList.add(toolBar, BorderLayout.NORTH);
		issueList.add(split, BorderLayout.CENTER);
		issueList.add(status, BorderLayout.SOUTH);

		// Подключение слушателей событий к компонентам формы
		addListeners();

		// Общее хранилище сообщает об изменениях, в том числе сделанных
		// на сайте или потоками, поэтому таблица обновляется в потоке Swing
		repository.addChangeListener(
				() -> SwingUtilities.invokeLater(this::showAllRows));
		showAllRows();

		// Визуализация экранной формы
		issueList.setVisible(true);
		log.info("Открыта экранная форма «Картотека выдач»");
	}

	/**
	 * Создаёт главное меню: по пунктам видно, какая лабораторная работа
	 * за какую возможность отвечает.
	 *
	 * @return строка меню
	 */
	private JMenuBar createMenuBar() {
		JMenuBar bar = new JMenuBar();

		JMenu file = new JMenu("Файл");
		file.add(menuItem("Открыть текстовый файл (ООП 5)…",
				e -> openFile(".txt")));
		file.add(menuItem("Сохранить в текстовый файл…",
				e -> saveFile(".txt")));
		file.addSeparator();
		file.add(menuItem("Открыть XML-файл (ООП 6)…", e -> openFile(".xml")));
		file.add(menuItem("Сохранить в XML-файл…", e -> saveFile(".xml")));
		file.addSeparator();
		file.add(menuItem("Выход", e -> issueList.dispose()));

		JMenu reports = new JMenu("Отчёты (ООП 7)");
		reports.add(menuItem("Построить PDF и HTML", e -> buildReports()));

		JMenu threads = new JMenu("Потоки (ООП 8)");
		threads.add(menuItem("Запустить конвейер из 3 потоков",
				e -> runPipeline()));

		JMenu service = new JMenu("Сервис");
		service.add(menuItem("Сортировка массива (ООП 1)", e -> showSort()));
		service.add(menuItem("Открыть сайт (Web 1-9)", e -> openSite()));
		service.add(menuItem("Файл журнала logs/issues.log (ООП 10)",
				e -> openPath(AppPaths.LOG_FILE)));

		bar.add(file);
		bar.add(reports);
		bar.add(threads);
		bar.add(service);
		return bar;
	}

	/**
	 * Создаёт пункт меню с обработчиком.
	 *
	 * @param title    текст пункта
	 * @param listener обработчик выбора пункта
	 * @return пункт меню
	 */
	private JMenuItem menuItem(String title, ActionListener listener) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(listener);
		return item;
	}

	/**
	 * Подключает слушателей событий к компонентам формы (ООП, лабораторная
	 * работа № 3): к кнопкам панели инструментов, к кнопкам поиска, к выпадающему
	 * списку читателей, к таблице выдач и к окну.
	 */
	private void addListeners() {

		/*
		 * Слушатель кнопки «Открыть»: запрашивает имя файла и загружает его
		 * содержимое в хранилище; формат определяется расширением файла.
		 */
		open.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку загрузки.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				openFile(null);
			}
		});

		/*
		 * Слушатель кнопки «Сохранить»: запрашивает имя файла и записывает
		 * в него содержимое хранилища.
		 */
		save.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку сохранения.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				saveFile(null);
			}
		});

		/* Слушатель кнопки «Добавить»: открывает диалог новой записи. */
		add.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку добавления.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				Issue created = IssueDialog.ask(issueList,
						"Новая запись о выдаче", null);
				if (created != null) {
					repository.add(created);
					status.setText(" Добавлена запись: " + created.getBook());
				}
			}
		});

		/* Слушатель кнопки «Редактировать»: правка выбранной записи. */
		edit.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку редактирования. Номер строки
			 * в таблице совпадает с номером записи в хранилище, пока таблица
			 * показывает полный список.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				int row = selectedRow();
				if (row < 0) {
					return;
				}
				Issue changed = IssueDialog.ask(issueList,
						"Редактирование записи", repository.all().get(row));
				if (changed != null) {
					repository.update(row, changed);
					status.setText(" Запись изменена");
				}
			}
		});

		/* Слушатель кнопки «Удалить»: удаляет выбранную запись. */
		delete.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку удаления после подтверждения.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				int row = selectedRow();
				if (row < 0) {
					return;
				}
				int answer = JOptionPane.showConfirmDialog(issueList,
						"Удалить выбранную запись о выдаче?", "Удаление",
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {
					repository.remove(row);
					status.setText(" Запись удалена");
				}
			}
		});

		/* Слушатель кнопки «Отчёты»: строит PDF и HTML. */
		print.addActionListener(new ActionListener() {
			/**
			 * Вызывается при нажатии на кнопку печати.
			 *
			 * @param event событие нажатия на кнопку
			 */
			public void actionPerformed(ActionEvent event) {
				buildReports();
			}
		});

		/*
		 * Слушатель события ActionEvent для кнопки «Поиск». Контролирует
		 * корректность условий поиска (ООП, лабораторная работа № 4): вызов
		 * поиска помещён в блок try, а блоки catch обрабатывают собственные
		 * исключения EmptyQueryException и ShortNameException.
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
					List<Issue> found = repository.search(selectedReader, book);
					fillTable(found);
					status.setText(" Найдено записей: " + found.size());
				} catch (EmptyQueryException e) {
					log.warn("Пустой поисковый запрос");
					JOptionPane.showMessageDialog(issueList, e.getMessage(),
							"Пустой запрос", JOptionPane.WARNING_MESSAGE);
				} catch (ShortNameException e) {
					log.warn("Слишком короткое название: {}", book);
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
				showAllRows();
			}
		});

		/* Слушатель события ItemEvent для выпадающего списка читателей. */
		reader.addItemListener(new ItemListener() {
			/**
			 * Вызывается при изменении выбора в списке: сообщает в строке
			 * состояния, сколько книг числится за выбранным читателем.
			 *
			 * @param event событие изменения выбора в списке
			 */
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					String selected = (String) event.getItem();
					int count = repository.select(selected, "").size();
					status.setText(" Выбран читатель: " + selected
							+ ", записей о выдаче: " + count);
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

		/* Слушатель события WindowEvent: записывает закрытие окна в журнал. */
		issueList.addWindowListener(new WindowAdapter() {
			/**
			 * Вызывается при закрытии окна.
			 *
			 * @param event событие окна
			 */
			public void windowClosing(WindowEvent event) {
				log.info("Окно «Картотека выдач» закрыто");
			}
		});
	}

	/**
	 * Возвращает номер выбранной строки или сообщает, что строка не выбрана.
	 *
	 * @return номер строки или -1
	 */
	private int selectedRow() {
		int row = issues.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(issueList, "Выберите строку в таблице");
		}
		return row;
	}

	/**
	 * Составляет текст карточки выдачи по номеру строки таблицы.
	 *
	 * @param row номер строки таблицы
	 * @return текст карточки — названия полей и их значения
	 * @throws ArrayIndexOutOfBoundsException если строки с таким номером нет
	 */
	private String makeCard(int row) throws ArrayIndexOutOfBoundsException {
		String card = "";
		for (int i = 0; i < columns.length; i++) {
			card = card + columns[i] + ": " + model.getValueAt(row, i) + "\n";
		}
		return card;
	}

	/** Показывает в таблице все записи хранилища. */
	private void showAllRows() {
		fillTable(repository.all());
		status.setText(" Записей в картотеке: " + repository.size());
	}

	/**
	 * Заменяет содержимое таблицы переданными записями.
	 *
	 * @param rows записи, которые требуется вывести в таблицу
	 */
	private void fillTable(List<Issue> rows) {
		model.setRowCount(0);
		for (Issue issue : rows) {
			model.addRow(issue.toRow());
		}
	}

	/**
	 * Показывает стандартное окно выбора файла и возвращает полное имя
	 * выбранного файла.
	 *
	 * @param title     заголовок окна выбора файла
	 * @param mode      режим окна: FileDialog.LOAD или FileDialog.SAVE
	 * @param extension расширение файла по умолчанию («.txt» или «.xml»)
	 * @return полное имя файла или null, если пользователь отказался
	 */
	private String chooseFile(String title, int mode, String extension) {
		FileDialog dialog = new FileDialog(issueList, title, mode);
		dialog.setDirectory(new File(AppPaths.DATA_DIR.toString())
				.getAbsolutePath());
		dialog.setFile("*" + extension);
		dialog.setVisible(true);
		if (dialog.getFile() == null) {
			return null;
		}
		return dialog.getDirectory() + dialog.getFile();
	}

	/**
	 * Загружает список выдач из файла: формат определяется по расширению.
	 *
	 * @param extension предлагаемое расширение или null для текстового файла
	 */
	private void openFile(String extension) {
		String fileName = chooseFile("Загрузка списка выдач", FileDialog.LOAD,
				extension == null ? ".txt" : extension);
		if (fileName == null) {
			return;
		}
		try {
			List<Issue> loaded = fileName.toLowerCase().endsWith(".xml")
					? new XmlIssueStorage().load(fileName)
					: new TextIssueStorage().load(fileName);
			repository.replaceAll(loaded);
			JOptionPane.showMessageDialog(issueList,
					"Загружено записей: " + loaded.size() + "\nФайл: "
							+ fileName);
		} catch (StorageException e) {
			JOptionPane.showMessageDialog(issueList, e.getMessage(), "Ошибка",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Сохраняет список выдач в файл: формат определяется по расширению.
	 *
	 * @param extension предлагаемое расширение или null для текстового файла
	 */
	private void saveFile(String extension) {
		String fileName = chooseFile("Сохранение списка выдач", FileDialog.SAVE,
				extension == null ? ".txt" : extension);
		if (fileName == null) {
			return;
		}
		try {
			List<Issue> all = repository.all();
			if (fileName.toLowerCase().endsWith(".xml")) {
				new XmlIssueStorage().save(fileName, all);
			} else {
				new TextIssueStorage().save(fileName, all);
			}
			JOptionPane.showMessageDialog(issueList,
					"Сохранено записей: " + all.size() + "\nФайл: " + fileName);
		} catch (StorageException e) {
			JOptionPane.showMessageDialog(issueList, e.getMessage(), "Ошибка",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Строит отчёты PDF и HTML по текущим данным в фоновом потоке, чтобы окно
	 * не замирало, и открывает их в программах просмотра.
	 */
	private void buildReports() {
		status.setText(" Строю отчёты…");
		new SwingWorker<Path[], Void>() {
			@Override
			protected Path[] doInBackground() throws Exception {
				Path xml = AppPaths.OUT_DIR.resolve("issues-snapshot.xml");
				new XmlIssueStorage().save(xml.toString(), repository.all());
				IssueReportBuilder builder = new IssueReportBuilder();
				return new Path[] {
						builder.buildPdf(xml, AppPaths.OUT_DIR.resolve("issues.pdf")),
						builder.buildHtml(xml,
								AppPaths.OUT_DIR.resolve("issues.html")) };
			}

			@Override
			protected void done() {
				try {
					Path[] files = get();
					status.setText(" Отчёты готовы: " + files[0] + ", "
							+ files[1]);
					openPath(files[1]);
					openPath(files[0]);
				} catch (Exception e) {
					Throwable cause = e.getCause() == null ? e : e.getCause();
					JOptionPane.showMessageDialog(issueList, cause.getMessage(),
							"Ошибка отчёта", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	/**
	 * Запускает конвейер из трёх потоков в отдельном потоке, чтобы окно
	 * не замирало, пока потоки ждут друг друга.
	 */
	private void runPipeline() {
		status.setText(" Конвейер запущен, ход работы виден в журнале");
		new SwingWorker<Path, Void>() {
			@Override
			protected Path doInBackground() throws Exception {
				ReportPipeline pipeline = new ReportPipeline(repository,
						AppPaths.XML_FILE, AppPaths.REPORT_XML,
						AppPaths.OUT_DIR.resolve("pipeline.html"), 900, null);
				return pipeline.run();
			}

			@Override
			protected void done() {
				try {
					Path html = get();
					status.setText(" Конвейер завершён, отчёт: " + html);
					openPath(html);
				} catch (Exception e) {
					Throwable cause = e.getCause() == null ? e : e.getCause();
					JOptionPane.showMessageDialog(issueList, cause.getMessage(),
							"Ошибка конвейера", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	/** Показывает ход сортировки массива из лабораторной работы № 1. */
	private void showSort() {
		JTextArea text = new JTextArea(ArraySort.demo(true) + "\n\n"
				+ ArraySort.demo(false));
		text.setEditable(false);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		JOptionPane.showMessageDialog(issueList, text,
				"Сортировка массива (ООП, лабораторная работа № 1)",
				JOptionPane.PLAIN_MESSAGE);
	}

	/** Открывает веб-приложение в браузере по умолчанию. */
	private void openSite() {
		try {
			Desktop.getDesktop().browse(new URI(siteUrl));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(issueList,
					"Откройте в браузере адрес " + siteUrl);
		}
	}

	/**
	 * Открывает файл программой по умолчанию.
	 *
	 * @param path путь к файлу
	 */
	private void openPath(Path path) {
		try {
			Desktop.getDesktop().open(path.toAbsolutePath().toFile());
		} catch (Exception e) {
			log.warn("Не удалось открыть {}: {}", path, e.getMessage());
		}
	}
}
