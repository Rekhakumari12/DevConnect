import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
  // static - can be access without creating a instance.
  public static void main(String agrs[]){
    List<String> books = new ArrayList<>();
    
    books.add("Atomic habbits");
    books.add("Clean Code");
    books.add("Atomic habbits"); // Duplicate allowed

    // System.out.println("Filtered books - "+books);
    // System.out.println(books.get(0));
    // books.forEach((book)->System.out.println(book));
    // List<String> squared = books.stream()
    // .filter(book-> book.startsWith("A"))
    // .collect(Collectors.toList());

    // System.out.println("Filtered books - "+squared);
    // List<Integer> nums = Arrays.asList(1,2,3,4);
    // int sum = nums.stream().reduce(0, (acc,curr) -> acc+curr);
    // System.out.println("Sum of number - "+sum);

    // Methode reference

    List<String> words = Arrays.asList("Apple", "Banana", "Cherry");
    words.forEach(System.out::println);

    List<String> upper = words.stream().map(String::toUpperCase).collect(Collectors.toList());
    System.out.println(upper);

  }
}
