package edu.java.issues.gwt.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Точка входа GWT-приложения «Книги читателя» (Web, лабораторная работа
 * № 8). Функциональность повторяет сервлет из лабораторной работы № 3:
 * пользователь выбирает читателя в выпадающем списке, нажимает кнопку,
 * и в диалоговом окне появляется список его книг, полученный от сервера
 * вызовом GWT-RPC.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueEntryPoint implements EntryPoint {

	/** Прокси службы на сервере. */
	private final IssueServiceAsync service = GWT.create(IssueService.class);

	/** Выпадающий список читателей. */
	private final ListBox readers = new ListBox();

	/** Кнопка запроса списка книг. */
	private final Button show = new Button("Получить список книг");

	/** Строка состояния для сообщений об ошибках и загрузке. */
	private final Label status = new Label();

	/**
	 * Строит страницу и запрашивает у сервера имена читателей.
	 */
	@Override
	public void onModuleLoad() {
		show.setEnabled(false);
		show.addClickHandler(event -> loadIssues());
		status.setText("Загрузка списка читателей…");

		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(8);
		panel.add(new Label("Выберите читателя:"));
		panel.add(readers);
		panel.add(show);
		panel.add(status);
		RootPanel.get("gwtContainer").add(panel);

		service.getReaders(new AsyncCallback<ArrayList<String>>() {
			@Override
			public void onSuccess(ArrayList<String> names) {
				for (String name : names) {
					readers.addItem(name);
				}
				show.setEnabled(!names.isEmpty());
				status.setText("");
			}

			@Override
			public void onFailure(Throwable caught) {
				status.setText("Не удалось получить читателей: "
						+ caught.getMessage());
			}
		});
	}

	/** Запрашивает у сервера книги выбранного читателя. */
	private void loadIssues() {
		final String reader = readers.getSelectedValue();
		show.setEnabled(false);
		status.setText("Загрузка…");
		service.getIssues(reader, new AsyncCallback<ArrayList<IssueDto>>() {
			@Override
			public void onSuccess(ArrayList<IssueDto> issues) {
				show.setEnabled(true);
				status.setText("");
				showDialog(reader, issues);
			}

			@Override
			public void onFailure(Throwable caught) {
				show.setEnabled(true);
				status.setText("Ошибка: " + caught.getMessage());
			}
		});
	}

	/**
	 * Показывает диалоговое окно со списком книг.
	 *
	 * @param reader имя читателя
	 * @param issues книги читателя
	 */
	private void showDialog(String reader, ArrayList<IssueDto> issues) {
		final DialogBox dialog = new DialogBox(true, true);
		dialog.setText("Список книг читателя " + reader);
		VerticalPanel content = new VerticalPanel();
		content.setSpacing(8);
		if (issues.isEmpty()) {
			content.add(new Label("За этим читателем книг нет"));
		} else {
			FlexTable table = new FlexTable();
			table.setText(0, 0, "Название книги");
			table.setText(0, 1, "Выдана");
			table.setText(0, 2, "Вернуть до");
			table.setText(0, 3, "Возвращена");
			int row = 1;
			for (IssueDto issue : issues) {
				table.setText(row, 0, issue.getBook());
				table.setText(row, 1, issue.getIssued());
				table.setText(row, 2, issue.getDue());
				table.setText(row, 3, issue.getReturned());
				row++;
			}
			table.getRowFormatter().getElement(0).getStyle()
					.setProperty("fontWeight", "600");
			table.getElement().getStyle().setProperty("borderSpacing", "12px 4px");
			content.add(table);
		}
		Button close = new Button("Закрыть");
		close.addClickHandler(event -> dialog.hide());
		content.add(close);
		dialog.setWidget(content);
		dialog.center();
		close.setFocus(true);
	}
}
