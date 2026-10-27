package edu.java.issues.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;
import edu.java.issues.model.IssueValidator;

/**
 * Диалог ввода и редактирования записи о выдаче (ООП, лабораторные работы
 * № 2 и № 3). Поля проверяются классом {@link IssueValidator}; пока данные
 * неверны, диалог показывается снова с сообщением об ошибке.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public final class IssueDialog {

	/** Формат даты для начальных значений полей. */
	private static final DateTimeFormatter DATE = DateTimeFormatter
			.ofPattern("dd.MM.yyyy");

	/** Служебный класс: экземпляры не создаются. */
	private IssueDialog() {
	}

	/**
	 * Показывает диалог и возвращает введённую запись.
	 *
	 * @param parent  окно-владелец диалога
	 * @param title   заголовок диалога
	 * @param initial начальные значения полей или null для новой записи
	 * @return введённая запись или null, если пользователь отказался
	 */
	public static Issue ask(Component parent, String title, Issue initial) {
		JComboBox<String> reader = new JComboBox<>(
				IssueRepository.readers().toArray(new String[0]));
		reader.setEditable(true);
		JTextField book = new JTextField(20);
		JTextField issued = new JTextField(LocalDate.now().format(DATE));
		JTextField due = new JTextField(
				LocalDate.now().plusDays(30).format(DATE));
		JCheckBox returned = new JCheckBox("Книга возвращена");
		if (initial != null) {
			reader.setSelectedItem(initial.getReader());
			book.setText(initial.getBook());
			issued.setText(initial.getIssued());
			due.setText(initial.getDue());
			returned.setSelected(Issue.YES.equals(initial.getReturned()));
		}
		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
		panel.add(new JLabel("Читатель:"));
		panel.add(reader);
		panel.add(new JLabel("Книга:"));
		panel.add(book);
		panel.add(new JLabel("Дата выдачи:"));
		panel.add(issued);
		panel.add(new JLabel("Срок возврата:"));
		panel.add(due);
		panel.add(new JLabel(""));
		panel.add(returned);

		while (true) {
			int answer = JOptionPane.showConfirmDialog(parent, panel, title,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (answer != JOptionPane.OK_OPTION) {
				return null;
			}
			String readerName = String.valueOf(reader.getSelectedItem());
			String error = IssueValidator.check(readerName, book.getText(),
					issued.getText(), due.getText());
			if (error == null) {
				return new Issue(readerName, book.getText(), issued.getText(),
						due.getText(), returned.isSelected() ? Issue.YES
								: Issue.NO);
			}
			JOptionPane.showMessageDialog(parent, error, "Неверные данные",
					JOptionPane.WARNING_MESSAGE);
		}
	}
}
