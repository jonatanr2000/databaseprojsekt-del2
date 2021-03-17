import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class User {


    protected String email;
    protected String password;
    protected String firstname;
    protected String surname;
    protected LocalDateTime lastActive;
    protected ArrayList<Post> views = new ArrayList<>();
    protected ArrayList<Post> likes = new ArrayList<>();


    public User(String email, String password, String firstname, String surname, LocalDateTime lastActive) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.surname = surname;
        this.lastActive = lastActive;
    }


    public void view(Post viewedPost) {
        if (!views.contains(viewedPost)) {
            views.add(viewedPost);
        }
    }

    public void like (Post likedPost) {

    }




}
