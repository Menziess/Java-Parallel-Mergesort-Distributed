package Main;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Stefan on 16-May-17.
 */

public class MergeSort {
	
	private int[] array;
	private int threshold;	
	private ForkJoinPool pool;
	
	public MergeSort(int[] array) {
		this.array = array;
	}

	/*
	 * Starts serial sorting.
	 */
	public int[] serial() {
		if (!validArray(array)) return array;
		System.out.print("Starting serial sort... aaand ");
		
		int high = array.length - 1;
		int mid = high / 2;
		
		return array = Sorter.merge(Sorter.sort(array, 0, mid), Sorter.sort(array, mid + 1, high));
	}
	
	/*
	 * Starts serial sorting.
	 */
	public int[] parallel() {
		if (!validArray(array)) return array;
		System.out.print("Starting parallel sort... aaand ");
		
		int low = 0;
		int high = array.length - 1;
		int mid = high / 2;

		int[] array1 = Arrays.copyOfRange(array, low, mid + 1 /* exclusive */);
		int[] array2 = Arrays.copyOfRange(array, mid + 1 /* inclusive */, high + 1 /* exclusive */);
		
		Sorter sorter1 = new Sorter(array1);
		Sorter sorter2 = new Sorter(array2);
		
		Thread t1 = new Thread(() -> sorter1.merge());
		Thread t2 = new Thread(() -> sorter2.merge());
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return array = Sorter.merge(sorter1.getArray(), sorter2.getArray());
	}
	
	/*
	 * Starts enhanced forkjoin parallel sorting.
	 */
	public int[] forkjoin() {
		if (!validArray(array)) return array;
		System.out.print("Starting parallel forkjoin sort... aaand ");
		
		int length = array.length - 1;

		threshold = array.length / Runtime.getRuntime().availableProcessors();
		if (threshold < 5) threshold = 5;
		
		Sorter sorter = new Sorter(array, 0, length, threshold);
		
		pool = new ForkJoinPool();
		pool.invoke(sorter);
		
		return sorter.compute();
	}
	
	/*
	 * Checks if array is filled.
	 */
	public static boolean validArray(int[] array) {
		if (array == null) {
			System.out.println("Array is null");
			return false;
		} else if (array.length < 2) {
			return false;
		}
		return true;
	}
}
