/**
 * Implements Bubble Sort, Quick Sort, and Merge Sort for Location arrays.
 */
public class SortingAlgorithms {

    // ---- Bubble Sort ----
    public static void bubbleSort(Location[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j].compareTo(arr[j + 1]) > 0) {
                    Location temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // ---- Quick Sort ----
    public static void quickSort(Location[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(Location[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    private static int partition(Location[] arr, int low, int high) {
        Location pivot = arr[low]; // Use first element as pivot
        int left = low + 1;        // Forward search index
        int right = high;          // Backward search index

        while (right > left) {
            // Scan forward to find element greater than pivot
            while (left <= right && arr[left].compareTo(pivot) <= 0)
                left++;
            // Scan backward to find element less than or equal to pivot
            while (left <= right && arr[right].compareTo(pivot) > 0)
                right--;
            // Swap the two out-of-place elements
            if (right > left) {
                Location temp = arr[left];
                arr[left] = arr[right];
                arr[right] = temp;
            }
        }

        // Move pivot into its final sorted position
        while (right > low && arr[right].compareTo(pivot) >= 0)
            right--;

        if (pivot.compareTo(arr[right]) > 0) {
            arr[low] = arr[right];
            arr[right] = pivot;
            return right;
        } else {
            return low;
        }
    }

    // ---- Merge Sort ----
    public static void mergeSort(Location[] arr) {
        mergeSort(arr, 0, arr.length - 1);
    }

    private static void mergeSort(Location[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(Location[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Location[] leftArr = new Location[n1];
        Location[] rightArr = new Location[n2];

        System.arraycopy(arr, left, leftArr, 0, n1);
        System.arraycopy(arr, mid + 1, rightArr, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArr[i].compareTo(rightArr[j]) <= 0) {
                arr[k++] = leftArr[i++];
            } else {
                arr[k++] = rightArr[j++];
            }
        }
        while (i < n1) arr[k++] = leftArr[i++];
        while (j < n2) arr[k++] = rightArr[j++];
    }
}