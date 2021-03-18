import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class makePost extends DBConn {

    private String folder;
    private String tag;
    private PreparedStatement regStatement;

    public makePost(String folder, String tag) {
        this.folder = folder;
        this.tag = tag;
    }

    public void getMaxPostId() {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("select max(Post_Id) from post");
            ResultSet jls = newregStatement.executeQuery();


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("db error during prepare of get max value");
        }
    }


}
