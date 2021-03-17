import java.time.LocalDateTime;

public class Instructor extends User{


    public Instructor(String email, String password, String firstname, String surname, LocalDateTime lastActive) {
        super(email, password, firstname, surname, lastActive);
    }

}
