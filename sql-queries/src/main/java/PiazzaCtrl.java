import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PiazzaCtrl extends DBConn{

    private String email;
    private Integer postId;
    private PreparedStatement regStatement;


    // Faktiske attributes vi skal ha
    private User user;

    public PiazzaCtrl() {


    }

    public void view(String email, int postId) {
        this.email = email;
        this.postId = postId;
        try {
            regStatement = conn.prepareStatement("INSERT INTO likes VALUES ( (?), (?) )");
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
            PreparedStatement newregStatement = conn.prepareStatement("SELECT * from likes");
            ResultSet rs = newregStatement.executeQuery();
            rs.next();
    } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
        }
    }

    public void login (String email, String password) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("SELECT * from users where Email = (?)");
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


    public void getPost() {
        if 
        if (this.user.isInstrucor) {
            try {
                PreparedStatement newregStatement = conn.prepareStatement("SELECT * from likes");
                ResultSet rs = newregStatement.executeQuery();
                rs.next();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("db error during insert of Like user= "+email+" postNr="+postId);
            }
        }else {
            System.out.println("You must be an instructor to see this.");
        }
    }

    public static void main(String[] args) {

        PiazzaCtrl viewCtrl = new PiazzaCtrl();
        viewCtrl.connect();
        viewCtrl.login("ha@gmail.com", "ok");

    }
}

