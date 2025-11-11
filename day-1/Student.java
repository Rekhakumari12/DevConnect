
public class Student extends Person{
  int studentId;

  public Student(String name, int age, int studentId) {
    super(name, age); // call superclass constructor
    this.studentId = studentId;
  }

    @Override
    public void interduce() {
      System.out.println("I'm "+getName()+" "+getAge()+" a student. My studentId is "+studentId);
    }
}

