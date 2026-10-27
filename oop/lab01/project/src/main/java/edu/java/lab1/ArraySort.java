package edu.java.lab1;

import java.util.Arrays;

/**
 * Консольное приложение лабораторной работы № 1: упорядочение статического
 * массива целых чисел методом пузырька по возрастанию и по убыванию.
 * После упорядочения элементы массива выводятся на консоль.
 *
 * @author Рябов И.А., группа 5315
 * @version 1.0
 */
public class ArraySort {

	/** Статический массив чисел: на сколько дней выданы книги. */
	private static int[] numbers = { 30, 14, 21, 7, 28, 10, 45 };

	/**
	 * Упорядочивает массив методом пузырька.
	 *
	 * @param array     массив, который упорядочивается на месте
	 * @param ascending true — по возрастанию, false — по убыванию
	 */
	public static void sort(int[] array, boolean ascending) {
		for (int i = 0; i < array.length - 1; i++) {
			for (int j = 0; j < array.length - 1 - i; j++) {
				boolean wrongOrder = ascending ? array[j] > array[j + 1]
						: array[j] < array[j + 1];
				if (wrongOrder) {
					int tmp = array[j];
					array[j] = array[j + 1];
					array[j + 1] = tmp;
				}
			}
		}
	}

	/**
	 * Точка входа: инициализирует массив, упорядочивает его и выводит
	 * результат.
	 *
	 * @param args аргументы командной строки, не используются
	 */
	public static void main(String[] args) {
		int[] array = numbers.clone();
		System.out.println("Исходный массив:      " + Arrays.toString(array));
		sort(array, true);
		System.out.println("По возрастанию:       " + Arrays.toString(array));
		sort(array, false);
		System.out.println("По убыванию:          " + Arrays.toString(array));
	}
}
