package edu.java.issues.web;

import edu.java.issues.model.Issue;

/**
 * Строка таблицы на веб-странице: запись и её номер в хранилище.
 * Номер нужен формам изменения и удаления, когда таблица отфильтрована.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class IssueRow {

	/** Номер записи в хранилище. */
	private final int index;

	/** Запись о выдаче. */
	private final Issue issue;

	/**
	 * Создаёт строку.
	 *
	 * @param index номер записи в хранилище
	 * @param issue запись о выдаче
	 */
	public IssueRow(int index, Issue issue) {
		this.index = index;
		this.issue = issue;
	}

	/** @return номер записи в хранилище */
	public int getIndex() {
		return index;
	}

	/** @return запись о выдаче */
	public Issue getIssue() {
		return issue;
	}
}
