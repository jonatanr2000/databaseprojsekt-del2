import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MakePost extends DBConn {

    /**
     * 2. A student makes a post belonging to the folder Exam and tagged with Question. Input to
     * the use case should be a post and the texts Exam and Question.
     */

    private String folder;
    private String tag;
    private String text;
    private String title;
    private PreparedStatement regStatement;
    private int threadIdLatest;
    private int postIdLatest;

    public MakePost(String title, String text, String folder, String tag) {
        this.folder = folder;
        this.tag = tag.toLowerCase();
        this.text = text;
        this.tag = title;
        this.connect();
    }

    public MakePost() {
        this.connect();
    }

    /**
     * Makes a new tag, but not if the tag already exists.
     * @param tag the tag
     * @return false if tag exists, true if tag is created
     * @throws SQLException
     */
    public boolean makeTag(String tag) throws SQLException {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("INSERT INTO piazza.tag values( (?) )");
            newregStatement.setString(1, tag);
            newregStatement.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("This tag already exists");
            return false;
        }
    }

    public void showFolders() throws SQLException {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("select * from piazza.folder");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                System.out.println("id: " + rs.getRow() + " " + rs.getString(2));
            }
        } catch (SQLException e) {
            System.out.println("Failed to find folders.");
        }
    }

    public boolean makeThread(String title, int folderId) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("insert into piazza.thread (Title, Folder_Id) values( (?), (?) )");
            newregStatement.setString(1, title);
            newregStatement.setInt(2, folderId);
            newregStatement.execute();
            // Proceeds to find latest Thread_Id
            PreparedStatement statement = conn.prepareStatement
                    ("select Thread_Id from thread where Thread_Id = (select LAST_INSERT_ID())");
            ResultSet lastThreadId = statement.executeQuery();
            lastThreadId.next();
            this.threadIdLatest = lastThreadId.getInt(1);
            return true;
        } catch (SQLException e) {
            System.out.println("Could not make thread.");
            return false;
        }
    }

    public boolean makePost(String postText, String colourCode, String postType, int threadId, String creator) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("insert into piazza.post (PostText, ColourCode, PostType, Thread_Id, Creator) values( (?), (?), (?), (?), (?) )");
            newregStatement.setString(1, postText);
            newregStatement.setString(2, colourCode);
            newregStatement.setString(3, postType);
            newregStatement.setInt(4, threadId);
            newregStatement.setString(5, creator);
            newregStatement.execute();
            // Draws out the postId of the last post
            PreparedStatement statement = conn.prepareStatement
                    ("select Post_Id from post where Post_Id = (select LAST_INSERT_ID())");
            ResultSet lastPostId = statement.executeQuery();
            lastPostId.next();
            this.postIdLatest = lastPostId.getInt(1);
            return true;
        } catch (SQLException e) {
            System.out.println("Could not make a post.");
            return false;
        }
    }

    public boolean connectTagsAndPost(int postId, String... tags) {
        try {
            for (String tag: tags) {
                makeTag(tag);
            }
            PreparedStatement newregStatement = conn.prepareStatement
                    ("insert into piazza.tags ( Description, Post_Id) values( (?), (?) )");
            for (String tag: tags) {
                newregStatement.setString(1, tag);
                newregStatement.setInt(2, postId);
                newregStatement.execute();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Could not connect tags and posts.");
            return false;
        }
    }

    public int getThreadIdLatest() {
        return threadIdLatest;
    }

    public int getPostIdLatest() {
        return postIdLatest;
    }

    public static void main(String[] args) throws SQLException {
        MakePost mp = new MakePost("Task 2", "I don't quite get it", "exam", "question");
        mp.connect();
        //mp.showFolders();
        //mp.makeTag("vanskelig");
        mp.makeThread("Difficult question 3", 2);
        mp.makePost("Really hard task.", "red", "post", mp.threadIdLatest, "ha@gmail.com");
        mp.connectTagsAndPost(mp.postIdLatest, "exam", "whatup", "hellothere");
    }

}
