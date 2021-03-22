import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PiazzaCtrl extends DBConn{

    private String email;
    private Integer postId;
    private PreparedStatement regStatement;


    // Faktiske attributes vi skal ha
    public User user;

    public PiazzaCtrl() {
        this.connect();
    }

    public void view(String email, int postId) {
        this.email = email;
        this.postId = postId;
        try {
            regStatement = conn.prepareStatement("INSERT INTO piazza.likes VALUES ( (?), (?) )");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during prepare of insert into Reg");
        }
    }
    public void regLike () {
        if (email != null && postId != null) {
            try {
                regStatement.setString(1, email);
                regStatement.setInt(2, postId);
                regStatement.execute();
            } catch (Exception e) {
                System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
            }
        }
    }

    public void getLikes () {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("SELECT * from piazza.likes");
            ResultSet rs = newregStatement.executeQuery();
            rs.next();
    } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
        }
    }

    /**
     * Searches the database for the user and checks that the found user has the same password as the one given in.
     * @param email String email of the user that wants to log in
     * @param password String used to authenticate the user
     */
    public void login (String email, String password) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("SELECT * from piazza.users where users.Email = (?)");
            try {
                newregStatement.setString(1, email);

                ResultSet rs = newregStatement.executeQuery();

                if (rs.next()) {
                    if (rs.getString("Password").matches(password)) {
                        this.user = new User(
                                email,
                                password,
                                rs.getString("Firstname"),
                                rs.getString("Surname"),
                                rs.getObject("Last_Active", LocalDateTime.class),
                                rs.getBoolean("Is_Instructor"));
                        System.out.println("Welcome user" + this.user.email);
                        updateLastActive(email, LocalDateTime.now());
                    } else {
                        System.out.println("Password is wrong");
                    }
                } else {
                    System.out.println("Username is wrong");
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
        }
    }

    /**
     * Updates the last active field in the given user.
     * @param email String of the user who should be updated.
     * @param when LocalDateTime of when the user was active.
     */
    private void updateLastActive(String email, LocalDateTime when) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("UPDATE piazza.users set Last_Active= (?) WHERE Email = (?)");
            newregStatement.setObject(1, when);
            newregStatement.setString(2, email);
            newregStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Prints out each user, how many threads they have seen and how many posts they have created.
     * This is only available for instructors
     */
    public void getPosts() {
        if (this.user != null) {
            if (this.user.isInstrucor) {
                try {
                    PreparedStatement newregStatement = conn.prepareStatement("SELECT piazza.users.Email, COUNT(DISTINCT piazza.view.Thread_Id), COUNT(DISTINCT piazza.post.Post_Id) " +
                            "FROM (piazza.users LEFT JOIN piazza.view ON users.Email = view.Email) LEFT JOIN piazza.post ON users.Email = post.Creator " +
                            "GROUP BY piazza.users.Email " +
                            "ORDER BY COUNT(DISTINCT view.Thread_Id) DESC");
                    ResultSet rs = newregStatement.executeQuery();

                    while(rs.next()) {
                        System.out.println(rs.getString(1) + " has seen: " + rs.getInt(2) + " threads, and has created: " + rs.getInt(3) + " posts.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("db error during insert of Like user= " + user.email + " postNr=" + postId);
                }
            } else {
                System.out.println("You must be an instructor to see this.");
            }
        } else {
            System.out.println("You must be logged in to see this content");
        }
    }

    public static void main(String[] args) {

        PiazzaCtrl viewCtrl = new PiazzaCtrl();
        viewCtrl.connect();
        viewCtrl.login("ha@gmail.com", "ok");
        viewCtrl.getPosts();
    }
}

