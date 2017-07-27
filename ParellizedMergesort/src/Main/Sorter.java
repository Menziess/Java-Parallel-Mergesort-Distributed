package Main;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

public class Sorter extends RecursiveTask<int[]> {
	
	private static final long serialVersionUID = 1L;
	public static int iterations = 0;
	public static int belowThresholds = 0;
	
	private int[] array;
	private int low, mid, high;
	private int threshold;
	
	public Sorter() {}
	public Sorter(int[] array) {
		this(array, 0, array.length - 1, 0);
	}
	public Sorter(int[] array, int low, int high, int threshold) {
		this.low = low;
		this.high = high;
		this.mid = low + (high-low) / 2;
		this.array = array;
		this.threshold = threshold;
	}
	
	public int[] getArray() {
		return Arrays.copyOfRange(array, low, high + 1 /* exclusive */);
	}
	
	@Override
	protected int[] compute() {
		
		iterations++;
		
		if (high - low <= threshold) {
			int[] sorted = Arrays.copyOfRange(array, low, high + 1);
			Arrays.sort(sorted);
			
			belowThresholds++;
			
			return sorted;
		}
		
		Sorter s1 = new Sorter(array, low, mid, threshold);
		Sorter s2 = new Sorter(array, mid + 1, high, threshold); 		
		
		s2.fork();
		
		return merge(s1.compute(), s2.join());
	}
		
	/*
	 * Sorts a given part of the array by calling merge.
	 */
	protected int[] sort(int[] array) {
		return sort(array, 0, array.length - 1);
	}
	protected static int[] sort(int[] array, int low, int high) {
		if (low < high) {
			int half = low + (high - low) / 2;
			return merge(sort(array, low, half), sort(array, half + 1, high));
		} else {
			return new int[] {array[low]};
		}
	}
	
	/*
	 * Merges two arrays in a sorted fashion.
	 */
	protected void merge() {
		this.array = merge(sort(this.array, low, mid), sort(this.array, mid + 1, high));
	}
	public static int[] merge(int[] array1, int[] array2) {
		int[] result = new int[array1.length + array2.length];
		
//		System.out.println("Lengths: " + array1.length + " + " + array2.length);
		
		int pointer1 = 0;
		int pointer2 = 0;
		
		for (int i = 0; i < result.length; i++) {
			if (pointer1 > array1.length - 1) {
				result[i] = array2[pointer2];
//				System.out.println("X\tArr2: " + array2[pointer2]);
				pointer2++;
			} else if (pointer2 > array2.length - 1) {
				result[i] = array1[pointer1];
//				System.out.println("X\tArr1: " + array1[pointer1]);
				pointer1++;
			} else if (array1[pointer1] < array2[pointer2]) {
				result[i] = array1[pointer1];
//				System.out.println("\tArr1: " + array1[pointer1]);
				pointer1++;
			} else {
				result[i] = array2[pointer2];
//				System.out.println("\tArr2: " + array2[pointer2]);
				pointer2++;
			}
		}
		
		return result;
	}
}