package edu.java.issues.web;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Хранилище учётных записей веб-приложения (Web, лабораторная работа № 6).
 * Пароли не хранятся открытым текстом: для каждого пользователя считается
 * хеш PBKDF2 со случайной солью. Две демонстрационные учётные записи
 * создаются при запуске, их пароли указаны в README.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class UserStore {

	/** Число итераций PBKDF2. */
	private static final int ITERATIONS = 120_000;

	/** Общий экземпляр хранилища. */
	private static final UserStore INSTANCE = new UserStore();

	/** Запись о пользователе: соль, хеш пароля и роль. */
	private static final class Account {
		/** Случайная соль. */
		private final byte[] salt;
		/** Хеш пароля. */
		private final byte[] hash;
		/** Роль пользователя. */
		private final Role role;

		/**
		 * Создаёт запись.
		 *
		 * @param salt соль
		 * @param hash хеш пароля
		 * @param role роль пользователя
		 */
		private Account(byte[] salt, byte[] hash, Role role) {
			this.salt = salt;
			this.hash = hash;
			this.role = role;
		}
	}

	/** Учётные записи по имени пользователя. */
	private final Map<String, Account> accounts = new HashMap<>();

	/** Создаёт хранилище с демонстрационными пользователями. */
	private UserStore() {
		register("librarian", "lib123", Role.LIBRARIAN);
		register("reader", "read123", Role.READER);
	}

	/**
	 * Возвращает общий экземпляр хранилища.
	 *
	 * @return хранилище учётных записей
	 */
	public static UserStore getInstance() {
		return INSTANCE;
	}

	/**
	 * Добавляет пользователя.
	 *
	 * @param name     имя пользователя
	 * @param password пароль открытым текстом, в хранилище не попадает
	 * @param role     роль пользователя
	 */
	private void register(String name, String password, Role role) {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		accounts.put(name, new Account(salt, hash(password, salt), role));
	}

	/**
	 * Проверяет имя и пароль.
	 *
	 * @param name     имя пользователя
	 * @param password введённый пароль
	 * @return роль пользователя, если имя и пароль верны, иначе пустое значение
	 */
	public Optional<Role> authenticate(String name, String password) {
		Account account = name == null ? null : accounts.get(name);
		// Хеш считается и для неизвестного имени, чтобы время ответа
		// не выдавало, существует ли пользователь
		byte[] salt = account == null ? new byte[16] : account.salt;
		byte[] candidate = hash(password == null ? "" : password, salt);
		if (account != null && MessageDigest.isEqual(candidate, account.hash)) {
			return Optional.of(account.role);
		}
		return Optional.empty();
	}

	/**
	 * Считает хеш пароля алгоритмом PBKDF2.
	 *
	 * @param password пароль
	 * @param salt     соль
	 * @return хеш пароля
	 */
	private static byte[] hash(String password, byte[] salt) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
					ITERATIONS, 256);
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
					.generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(
					"Алгоритм PBKDF2 недоступен: "
							+ new String(salt, StandardCharsets.ISO_8859_1),
					e);
		}
	}
}
