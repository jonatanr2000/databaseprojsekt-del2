import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PiazzaCtrl extends DBConn {

    // Attributes
    public User user;

    //Create and connect the controller
    public PiazzaCtrl() {
        this.connect();
    }

    public void view(String email, int postId) {
        try {
            System.out.println(email + postId);
            PreparedStatement regViewStatement = conn.prepareStatement("INSERT INTO piazza.view VALUES ( (?), (?) )");
            regViewStatement.setString(1, email);
            regViewStatement.setInt(2, postId);
            regViewStatement.execute();
        } catch (SQLException e) {
            //pass
        }
    }


    public void regLike(String email, Integer postId) {
        if (email != null && postId != null) {
            try {
                PreparedStatement regLikeStatement = conn.prepareStatement("INSERT INTO piazza.likes VALUES ( (?), (?) )");
                regLikeStatement.setString(1, email);
                regLikeStatement.setInt(2, postId);
                regLikeStatement.execute();
            } catch (Exception e) {
                System.out.println("db error during insert of Like user= " + email + " postNr=" + postId);
            }
        }
    }

    /**
     * Searches the database for the user and checks that the found user has the same password as the one given in.
     *
     * @param email    String email of the user that wants to log in
     * @param password String used to authenticate the user
     * @return user User that is logged in, if unable to log in returns null.
     */
    public User login(String email, String password) {
        try {
            //Finds user with matching email
            PreparedStatement getUsersQuery = conn.prepareStatement("SELECT * from piazza.users where users.Email = (?)");
            try {
                getUsersQuery.setString(1, email);

                ResultSet userResult = getUsersQuery.executeQuery();

                if (userResult.next()) {
                    //Check that the passwords are matching
                    if (userResult.getString("Password").matches(password)) {
                        //logs the user in and updates
                        this.user = new User(
                                email,
                                password,
                                userResult.getString("Firstname"),
                                userResult.getString("Surname"),
                                userResult.getObject("Last_Active", LocalDateTime.class),
                                userResult.getBoolean("Is_Instructor"));
                        System.out.println("Welcome user: " + this.user.email);
                        updateLastActive(email, LocalDateTime.now());
                    } else {
                        System.out.println("Password is wrong");
                    }
                } else {
                    System.out.println("Username is wrong");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.user;
    }

    /**
     * Updates the last active field in the given user.
     *
     * @param email String of the user who should be updated.
     * @param when  LocalDateTime of when the user was active.
     */
    private void updateLastActive(String email, LocalDateTime when) {
        try {
            PreparedStatement regLastActiveStatement = conn.prepareStatement("UPDATE piazza.users set Last_Active= (?) WHERE Email = (?)");
            regLastActiveStatement.setObject(1, when);
            regLastActiveStatement.setString(2, email);
            regLastActiveStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints out each user, how many threads they have seen and how many posts they have created.
     * This is only available for instructors
     */
    public List<String> getStatistics() {
        List<String> stats = new ArrayList<String>();
        //Checks that the user is logged in and is an instructor
        if (this.user != null) {
            if (this.user.isInstrucor) {
                try {
                    //Left joins user to view and the left joins post.
                    //This will result in "cross-join"ing view and post so we will only select distinct from those.
                    //order by number of views descending.
                    PreparedStatement statisticsQuery = conn.prepareStatement("SELECT piazza.users.Email, COUNT(DISTINCT piazza.view.Thread_Id), COUNT(DISTINCT piazza.post.Post_Id) " +
                            "FROM (piazza.users LEFT JOIN piazza.view ON users.Email = view.Email) LEFT JOIN piazza.post ON users.Email = post.Creator " +
                            "GROUP BY piazza.users.Email " +
                            "ORDER BY COUNT(DISTINCT view.Thread_Id) DESC");
                    ResultSet statisticsResult = statisticsQuery.executeQuery();


                    //prints out the results
                    while (statisticsResult.next()) {
                        stats.add(statisticsResult.getString(1) + " has seen: " + statisticsResult.getInt(2) + " threads, and has created: " + statisticsResult.getInt(3) + " posts.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("You must be an instructor to see this.");
            }
        } else {
            System.out.println("You must be logged in to see this content");
        }
        return stats;
    }

    public void checkReply(int inputPostID, User user) {
        try {
            PreparedStatement getPostQuery = conn.
                    prepareStatement("SELECT * FROM piazza.post WHERE Post_Id = (?)");
            getPostQuery.setInt(1, inputPostID);

            ResultSet postResult = getPostQuery.executeQuery();

            if (postResult.next()) {
                if (postResult.getString("colourCode").matches("red")) {
                    String colour;
                    if (user.isInstrucor) {
                        colour = "blue";
                    }else {
                        colour = "yellow";
                    }
                    PreparedStatement setColourStatement = conn.
                            prepareStatement("UPDATE piazza.post SET ColourCode = '"+colour+"' WHERE " +
                                    "Post_Id = (?) ");
                    setColourStatement.setInt(1, inputPostID);
                    setColourStatement.execute();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> search(String word) {
        ArrayList<Integer> keyWords = new ArrayList<>();
        try {
            PreparedStatement newRegStatement = conn
                .prepareStatement("SELECT * FROM piazza.post NATURAL JOIN piazza.thread " +
                                      "WHERE (Title LIKE '%"+word+"%') OR (PostText LIKE '%"+word+"%') ");
            ResultSet rs = newRegStatement.executeQuery();

            while (rs.next()) {
                keyWords.add(rs.getInt("Post_Id"));
            }
            return keyWords;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {

        String ja = "SELECT * FROM piazza.post NATURAL JOIN piazza.thread " +
        "WHERE (Title LIKE %'"+"word"+"'%) OR (PostText LIKE %'"+"word"+"'%) ";
        System.out.println(ja);

        PiazzaCtrl viewCtrl = new PiazzaCtrl();
        viewCtrl.connect();
        User user = viewCtrl.login("ha@gmail.com", "ok");
        System.out.println(viewCtrl.search("NO"));
        viewCtrl.getStatistics();


        viewCtrl.checkReply(1, user);
    }
}

