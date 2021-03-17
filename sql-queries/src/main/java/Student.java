import java.time.LocalDateTime;

public class Student extends User{

    public Student(String email, String password, String firstname, String surname, LocalDateTime lastActive) {
        super(email, password, firstname, surname, lastActive);
    }
}
