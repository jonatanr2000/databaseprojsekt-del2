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
            return false;
        }
    }

    /**
     * Returns a list of folders as strings with folder id and folder name.
     * @return the list of strings, null otherwise.
     */
    public List<String> showFolders() {
        List<String> folderList = new ArrayList<>();
        try {
            PreparedStatement newregStatement = conn.prepareStatement("select * from piazza.folder");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                folderList.add("id: " + rs.getInt(1) + " " + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return folderList;
    }

    /**
     * Returns a list of threads as strings with thread id, title and the text of the post.
     * @return the list of threads, an empty string otherwise.
     */
    public List<String> getThreads() {
        List<String> threadList = new ArrayList<>();
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.PostType = 'post' \n" +
                            "order by post.Thread_Id; ");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                threadList.add( "thread id: " + rs.getInt(1) +
                                " title: " + rs.getString(2) +
                                " text: " + rs.getString(3) + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return threadList;
    }

    /**
     * Returns a list of the thread ids that exist in the database.
     * @return a list of integers if the method succeeds, an empty list otherwise.
     */
    public List<Integer> getThreadId() {
        List<Integer> threadList = new ArrayList<>();
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.PostType = 'post' \n" +
                            "order by post.Thread_Id; ");
            ResultSet rs = newregStatement.executeQuery();
            while(rs.next()) {
                threadList.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return threadList;
    }

    /**
     * Returns a list of posts as strings with thread id, title and the text of the post. The
     * posts that will be returned are the one with an id specified in the parameters.
     * @param indexes the ids of the posts that are to be shown.
     * @return list of strings if the method succeeds, an empty list otherwise.
     */
    public List<String> getPosts(List<Integer> indexes) {
        try {
            if (indexes.size() > 0) {
                List<String> threadList = new ArrayList<>();
                String listString = indexes.toString();
                listString = "(" + listString.substring(1, listString.length() - 1) + ")";
                //Since the input is a list of integers we don't think it's likely that this can be used to sql-injects.
                PreparedStatement newregStatement = conn.prepareStatement
                        ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                                "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                                "where post.Post_Id in " + listString + "  \n " +
                                "order by post.Thread_Id; ");
                ResultSet rs = newregStatement.executeQuery();
                while (rs.next()) {
                    threadList.add( "thread id: " + rs.getInt(1) +
                                    " title: " + rs.getString(2) +
                                    " text: " + rs.getString(3) + "\n");
                }
                return threadList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to find threads.");
        }
        return null;
    }

    /**
     * Returns a list of strings with post id, text, post type and creator of the
     * posts in a thread.
     * @param threadId of the thread that the posts will be retrieved from.
     * @return a list of strings if the method succeeds, an empty list elsewise.
     */
    public List<String> getPostsInThread(int threadId) {
        List<String> postList = new ArrayList<>();
        try {
            PreparedStatement newregStatement = conn.prepareStatement
                    ("select post.Post_Id, post.PostText, post.PostType, post.Creator \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.Thread_Id = ( ? ) \n" +
                            "order by post.Post_Id;");
            newregStatement.setInt(1, threadId);
            ResultSet rs = newregStatement.executeQuery();
            while (rs.next()) {
                postList.add(   "post id: " + rs.getInt(1) +
                                " text: " + rs.getString(2) +
                                " post type: " + rs.getString(3) +
                                " creator: " + rs.getString(4) + "\n");
            }
        } catch (SQLException e) {
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
        return postList;
    }

    /**
     * Finds the post with post type = 'post' in the given thread.
     * @param threadId of the thread.
     * @return postId of the post with post type = 'post' in the given thread.
     */
    public Integer getPostInThread(int threadId) {
        Integer postID = null;
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
     * @return true if the operation succeeds, false otherwise.
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
            e.printStackTrace();
            return null;
        }
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the thread id of a post with the given post id.
     * @param postId of the post where to find the thread id.
     * @return thread id of the post if the method succeeds, null otherwise.
     */
    public Integer findThreadIdFromPostId(int postId) {
        Integer threadId = null;
        try {
            PreparedStatement newregStatement = conn.prepareStatement(
                    "select post.Thread_Id\n" +
                        "from post\n" +
                        "where post.Post_Id = ( ? );"
            );
            newregStatement.setInt(1, postId);
            ResultSet rs = newregStatement.executeQuery();
            rs.next();
            threadId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return threadId;
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
        //System.out.println(mp.showThreads(Arrays.asList(1, 2, 4, 5)));
    }

}