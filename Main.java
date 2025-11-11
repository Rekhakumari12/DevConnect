public class Main {
  public static void main(String[] args) {
      System.err.println("Hello Java");
      Person p1 = new Person("Rekha", 25);
      Person p2 = new Student("Anjali", 20, 102);
      p1.interduce();
      p2.interduce();

      Student s1 = new Student("Hunar", 19, 101);
      s1.setAge(18);
      s1.interduce();
  }
}


