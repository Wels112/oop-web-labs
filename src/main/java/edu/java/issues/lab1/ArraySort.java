package edu.java.issues.lab1;

import java.util.Arrays;

/**
 * Консольное приложение из лабораторной работы № 1: упорядочение
 * статического массива целых чисел методом пузырька. Тот же класс
 * использует окно Swing, чтобы показать ход сортировки в диалоге.
 * Запуск из консоли: <code>java edu.java.issues.lab1.ArraySort</code>.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class ArraySort {

	/** Статический массив чисел: число дней, на которые выданы книги. */
	private static int[] numbers = { 30, 14, 21, 7, 28, 10, 45 };

	/**
	 * Упорядочивает массив по возрастанию или убыванию методом пузырька
	 * и записывает ход работы в текстовый отчёт.
	 *
	 * @param array     массив, который будет упорядочен на месте
	 * @param ascending true — по возрастанию, false — по убыванию
	 * @return текстовый отчёт: массив после каждого прохода
	 */
	public static String sort(int[] array, boolean ascending) {
		StringBuilder report = new StringBuilder();
		report.append("До сортировки:    ").append(Arrays.toString(array))
				.append('\n');
		int pass = 0;
		for (int i = 0; i < array.length - 1; i++) {
			boolean swapped = false;
			for (int j = 0; j < array.length - 1 - i; j++) {
				boolean wrongOrder = ascending ? array[j] > array[j + 1]
						: array[j] < array[j + 1];
				if (wrongOrder) {
					int tmp = array[j];
					array[j] = array[j + 1];
					array[j + 1] = tmp;
					swapped = true;
				}
			}
			pass++;
			report.append("Проход ").append(pass).append(":        ")
					.append(Arrays.toString(array)).append('\n');
			if (!swapped) {
				break;
			}
		}
		report.append("После сортировки: ").append(Arrays.toString(array));
		return report.toString();
	}

	/**
	 * Выполняет сортировку копии демонстрационного массива.
	 *
	 * @param ascending true — по возрастанию, false — по убыванию
	 * @return текстовый отчёт о сортировке
	 */
	public static String demo(boolean ascending) {
		return sort(numbers.clone(), ascending);
	}

	/**
	 * Точка входа консольного приложения.
	 *
	 * @param args аргументы командной строки, не используются
	 */
	public static void main(String[] args) {
		System.out.println(demo(true));
		System.out.println();
		System.out.println(demo(false));
	}
}
