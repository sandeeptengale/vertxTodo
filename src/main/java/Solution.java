import java.util.Arrays;
import java.util.List;

public class Solution {
    public static void main(String[] args) {
        List<String> terminals = Arrays.asList("COMPLETED", "VARIANCE");
        List<String> input = Arrays.asList("COMPLETED");
        if (terminals.containsAll(input)) {
            System.out.println("Hello");
        }
    }
}
