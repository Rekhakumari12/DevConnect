import java.util.ArrayList;
import java.util.List;

public class Main {
  // static - can be access without creating a instance.
  public static void main(String agrs[]){
    List<String> books = new ArrayList<>();
    
    books.add("Atomic habbits");
    books.add("Clean Code");
    books.add("Atomic habbits"); // Duplicate allowed

    System.out.println(books);
    System.out.println(books.get(0));

  }
}
