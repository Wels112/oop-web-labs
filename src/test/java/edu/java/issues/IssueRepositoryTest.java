package edu.java.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.java.issues.exception.EmptyQueryException;
import edu.java.issues.exception.ShortNameException;
import edu.java.issues.model.Issue;
import edu.java.issues.model.IssueRepository;

/**
 * Модульные тесты хранилища выдач (ООП, лабораторная работа № 9):
 * поиск, контроль условий поиска, изменение записей и уведомление слушателей.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
class IssueRepositoryTest {

	/** Хранилище, которое создаётся заново перед каждым тестом. */
	private IssueRepository repo;

	/** Заполняет хранилище тремя записями. */
	@BeforeEach
	void setUp() {
		repo = new IssueRepository();
		repo.add(new Issue("Иванов Пётр", "Три мушкетёра", "05.09.2026",
				"05.10.2026", "Да"));
		repo.add(new Issue("Смирнова Анна", "Война и мир", "12.09.2026",
				"12.10.2026", "Нет"));
		repo.add(new Issue("Иванов Пётр", "Анна Каренина", "20.09.2026",
				"20.10.2026", "Нет"));
	}

	/** Поиск только по читателю возвращает все его книги. */
	@Test
	void searchByReader() throws Exception {
		List<Issue> found = repo.search("Иванов Пётр", "");
		assertEquals(2, found.size());
	}

	/** Поиск по части названия не зависит от регистра. */
	@Test
	void searchByBookIgnoresCase() throws Exception {
		List<Issue> found = repo.search(IssueRepository.ALL_READERS, "ВОЙНА");
		assertEquals(1, found.size());
		assertEquals("Смирнова Анна", found.get(0).getReader());
	}

	/** Пустой запрос приводит к EmptyQueryException. */
	@Test
	void emptyQueryIsRejected() {
		assertThrows(EmptyQueryException.class,
				() -> repo.search(IssueRepository.ALL_READERS, "  "));
	}

	/** Название из одного символа приводит к ShortNameException. */
	@Test
	void oneLetterBookIsRejected() {
		ShortNameException e = assertThrows(ShortNameException.class,
				() -> repo.search(IssueRepository.ALL_READERS, "А"));
		assertTrue(e.getMessage().contains("«А»"));
	}

	/** Правка отметки о возврате создаёт новую запись и не портит старую. */
	@Test
	void updateReplacesRecord() {
		Issue old = repo.all().get(1);
		repo.update(1, old.withReturned("Да"));
		assertEquals("Да", repo.all().get(1).getReturned());
		assertEquals("Нет", old.getReturned());
	}

	/** Удаление уменьшает число записей. */
	@Test
	void removeDecreasesSize() {
		repo.remove(0);
		assertEquals(2, repo.size());
	}

	/** Обращение к несуществующей записи — стандартное исключение. */
	@Test
	void badIndexThrows() {
		assertThrows(IndexOutOfBoundsException.class, () -> repo.remove(10));
	}

	/** Слушатель вызывается после каждого изменения. */
	@Test
	void listenersAreNotified() {
		AtomicInteger calls = new AtomicInteger();
		repo.addChangeListener(calls::incrementAndGet);
		repo.add(new Issue("Кузнецов Илья", "Три мушкетёра", "25.09.2026",
				"25.10.2026", "Нет"));
		repo.remove(0);
		assertEquals(2, calls.get());
	}

	/** Любая отметка, кроме «Да», превращается в «Нет». */
	@Test
	void returnedFlagIsNormalized() {
		assertEquals("Нет", new Issue("a", "b", "c", "d", "возможно").getReturned());
		assertEquals("Да", new Issue("a", "b", "c", "d", " да ").getReturned());
	}
}
