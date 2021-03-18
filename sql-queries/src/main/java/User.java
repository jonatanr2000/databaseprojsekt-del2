import java.time.LocalDateTime;
import java.util.ArrayList;

public class User {


    protected String email;
    protected String password;
    protected String firstname;
    protected String surname;
    protected LocalDateTime lastActive;
    protected ArrayList<Post> views = new ArrayList<>();
    protected ArrayList<Post> likes = new ArrayList<>();
    protected Boolean isInstrucor;


    public User(String email, String password, String firstname, String surname, LocalDateTime lastActive, Boolean isInstructor) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.surname = surname;
        this.lastActive = lastActive;
        this.isInstrucor = isInstructor;
    }


    public void view(Post viewedPost) {
        if (!views.contains(viewedPost)) {
            views.add(viewedPost);
        }
    }

    public void like (Post likedPost) {

    }




}
