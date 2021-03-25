import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Makes the posts and threads.
 */
public class MakePost extends DBConn {

    /**
     * 2. A student makes a post belonging to the folder Exam and tagged with Question. Input to
     * the use case should be a post and the texts Exam and Question.
     */

    private int threadIdLatest;
    private int postIdLatest;
    private User user;

    /**
     * Constructor without parameters.
     */
    public MakePost() {
        this.connect();
    }

    /**
     * Sets the user.
     * @param user the user which is to be set.
     */
    public void setUser (User user) {
        this.user = user;
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

    /**
     * Shows all the folders that exist in the database with their associated ids.
     */
    public void showFolders() {
        try {
            PreparedStatement newregStatement = conn.prepareStatement("select * from piazza.folder");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                System.out.println("id: " + rs.getInt(1) + " " + rs.getString(2));
            }
        } catch (SQLException e) {
            System.out.println("Failed to find folders.");
        }
    }

    /**
     * Shows the threads in the database.
     * @return a list with the thread ids in the database. Null otherwise.
     */
    public List<Integer> showThreads() {
        try {
            List<Integer> threadIds = new ArrayList<>();
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.PostType = 'post' \n" +
                            "order by post.Thread_Id; ");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                threadIds.add(rs.getInt(1));
                System.out.println("thread id: " + rs.getInt(1) +
                        " title: " + rs.getString(2) +
                        " text: " + rs.getString(3) + "\n");
            }
            return threadIds;
        } catch (SQLException e) {
            System.out.println("Failed to find threads.");
            return null;
        }
    }

    /**
     * Shows the threads with the ids that are given.
     * @param indexes the ids of the threads that are wanted to be seen.
     * @return list of thread ids that are shown.
     */
    public List<Integer> showThreads(List<Integer> indexes) {
        try {
            String listString = indexes.toString();
            listString = "(" + listString.substring(1, listString.length()-1) + ")";
            //Since the input is a list of integers we don't think it's likely that this can be used to sql-injects.
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.Post_Id in "+listString+"  \n " +
                            "order by post.Thread_Id; ");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                System.out.println("thread id: " + rs.getInt(1) +
                        " title: " + rs.getString(2) +
                        " text: " + rs.getString(3) + "\n");
            }
            return indexes;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to find threads.");
            return null;
        }
    }

    /**
     * Displays all the posts in a given thread.
     * @param threadId the id of the thread.
     * @return the postId of the post with post type = 'post' in the thread. Null elsewise.
     */
    public Integer showPostsInThread(int threadId) {
        Integer postID = null;
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select post.Post_Id, post.PostText, post.PostType, post.Creator \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.Thread_Id = ( ? ) \n" +
                            "order by post.Thread_Id;");
            newregStatement.setInt(1, threadId);
            ResultSet rs = newregStatement.executeQuery();
            while (rs.next()) {
                System.out.println("post id: " + rs.getInt(1) +
                        " text: " + rs.getString(2) +
                        " post type: " + rs.getString(3) +
                        " creator: " + rs.getString(4) + "\n");
            }
        }catch (SQLException e) {
            System.out.println("Could not find posts.");
            e.printStackTrace();
        }
        try {
            PreparedStatement viewed_post = conn.prepareStatement("INSERT INTO piazza.view VALUES ((?), (?))");
            viewed_post.setString(1, user.email);
            viewed_post.setInt(2, threadId);
            viewed_post.execute();
        } catch (SQLException e) {
            //pass
        }

        try {
            // Finds the post with post type = 'post' in the thread
            PreparedStatement postInThread = conn.prepareStatement(
                    "select post.Post_Id, post.PostText, post.PostType, post.Creator \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id\n" +
                            "where post.Thread_Id = ( ? ) and post.PostType = 'post'\n" +
                            "order by post.Thread_Id;");
            postInThread.setInt(1, threadId);
            ResultSet postId = postInThread.executeQuery();
            if (postId.next()){
                postID = postId.getInt(1);
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return postID;
    }

    /**
     * Makes a thread with a title in a given folder.
     * @param title is the title of the thread.
     * @param folderId is the id of the folder in which to place the thread.
     * @return true if the operation succeeds, false elsewise.
     */
    public boolean makeThread(String title, int folderId) {
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("insert into piazza.thread (Title, Folder_Id) values( (?), (?) )");
            newregStatement.setString(1, title);
            newregStatement.setInt(2, folderId);
            newregStatement.execute();
            // Proceeds to find latest Thread_Id
            PreparedStatement statement = conn.prepareStatement
                    ("select Thread_Id from piazza.thread where Thread_Id = (select LAST_INSERT_ID())");
            ResultSet lastThreadId = statement.executeQuery();
            lastThreadId.next();
            this.threadIdLatest = lastThreadId.getInt(1);
            return true;
        } catch (SQLException e) {
            System.out.println("Could not make thread.");
            return false;
        }
    }

    /**
     * Makes a post in the database in a thread.
     * @param postText the text that should be in the post.
     * @param colourCode the colour code of the post, different colour means different thing.
     * @param postType the post type, might be a reply or a post.
     * @param threadId the id of the thread where the post is made.
     * @param creator the creator of the post.
     * @return the id of the post that was just made, if it fails null will be returned.
     */
    public Integer makePost(String postText, String colourCode, String postType, int threadId, String creator) {
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
                    ("select Post_Id from piazza.post where Post_Id = (select LAST_INSERT_ID())");
            ResultSet lastPostId = statement.executeQuery();
            lastPostId.next();
            this.postIdLatest = lastPostId.getInt(1);
            return this.postIdLatest;
        } catch (SQLException e) {
            System.out.println("Could not make a post.");
        }
        return null;
    }

    /**
     * Connects a post with different tags in the database.
     * @param postId the postId of the post which shall receive tags.
     * @param tags the tags which should be associated with a post.
     * @return true if the operation succeeds, false otherwise.
     */
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

    /**
     * Returns the latest thread id.
     * @return latest thread id.
     */
    public int getThreadIdLatest() {
        return threadIdLatest;
    }

    /**
     * Returns the latest post id.
     * @return latest post id.
     */
    public int getPostIdLatest() {
        return postIdLatest;
    }

    public static void main(String[] args) throws SQLException {
        MakePost mp = new MakePost();
        mp.connect();
        //mp.showFolders();
        //mp.makeTag("vanskelig");
        //mp.makeThread("Difficult question 3", 2);
        //mp.makePost("Really hard task.", "red", "post", mp.threadIdLatest, "ha@gmail.com");
        //mp.connectTagsAndPost(mp.postIdLatest, "exam", "whatup", "hellothere");
        //mp.showThreads();
        //mp.showPostsInThread(1);
        System.out.println(mp.showThreads(Arrays.asList(1, 2, 4, 5)));
    }

}