import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller for the database actions.
 */
public class PiazzaCtrl extends DBConn {

    // Attributes
    private int threadIdLatest;
    private int postIdLatest;
    private User user;



    /**
     * Create and connect the controller
     */
    public PiazzaCtrl() {
        this.connect();
    }

    /**
     * Creates a view between user and the thread, meaning that the user has now seen the thread.
     * @param email String of the user.
     * @param threadId int of the thread being viewed.
     */
    public void view(String email, int threadId) {
        try {
            System.out.println(email + threadId);
            PreparedStatement regViewStatement = conn.prepareStatement("INSERT INTO piazza.view VALUES ( (?), (?) )");
            regViewStatement.setString(1, email);
            regViewStatement.setInt(2, threadId);
            regViewStatement.execute();
        } catch (SQLException e) {
            //pass
        }
    }


    /**
     * Creates a like between user and the post, meaning that the user has now liked the post.
     * @param email String of the user.
     * @param postId int of the thread being viewed.
     */
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
                        updateLastActive(email, LocalDateTime.now());
                    }
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
        List<String> stats = new ArrayList<>();
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
            }
        }
        return stats;
    }

    /**
     * Check if a post has a reply or not. If it has, it will change the colour code by whom has answered it.
     * @param inputPostID is an integer input representing a post_Id.
     * @param user represents the user that is logged in.
     */
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

    /**
     * Searches the database for specific keywords in posts.
     * @param word is the keyword the student is searching for.
     * @return a list of all Post_ID's values that contains the the key word. Or an empty list if no matches.
     */
    public ArrayList<Integer> search(String word) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            PreparedStatement newRegStatement = conn
                .prepareStatement("SELECT * FROM piazza.post NATURAL JOIN piazza.thread " +
                                      "WHERE (Title LIKE '%"+word+"%') OR (PostText LIKE '%"+word+"%') ");
            ResultSet rs = newRegStatement.executeQuery();

            //As long as rs has a row, it will add the post_Id to the keyWords list
            while (rs.next()) {
                ids.add(rs.getInt("Post_Id"));
            }
            //Will contain at least one or more Post_Id's
            return ids;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //if rs is empty, it will return an empty list
        return new ArrayList<>();
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
     */
    public boolean makeTag(String tag){
        try {
            PreparedStatement insertViewStatement = conn.prepareStatement("INSERT INTO piazza.tag values( (?) )");
            insertViewStatement.setString(1, tag);
            insertViewStatement.execute();
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
            PreparedStatement getFoldersQuery = conn.prepareStatement("select * from piazza.folder");
            ResultSet rs = getFoldersQuery.executeQuery();
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
            PreparedStatement getThreadsQuery = conn.prepareStatement
                    ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.PostType = 'post' \n" +
                            "order by post.Thread_Id; ");
            ResultSet rs = getThreadsQuery.executeQuery();
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
                PreparedStatement getPostsInList = conn.prepareStatement
                        ("select piazza.post.Thread_Id, piazza.thread.Title, piazza.post.PostText \n" +
                                "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                                "where post.Post_Id in " + listString + "  \n " +
                                "order by post.Thread_Id; ");
                ResultSet rs = getPostsInList.executeQuery();
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
     * @return a list of strings if the method succeeds, an empty list otherwise.
     */
    public List<String> getPostsInThread(int threadId) {
        List<String> postList = new ArrayList<>();
        try {
            PreparedStatement getPostsInThreadQuery = conn.prepareStatement
                    ("select post.Post_Id, post.PostText, post.PostType, post.Creator \n" +
                            "from piazza.post inner join piazza.thread on post.Thread_Id = thread.Thread_Id \n" +
                            "where post.Thread_Id = ( ? ) \n" +
                            "order by post.Post_Id;");
            getPostsInThreadQuery.setInt(1, threadId);
            ResultSet rs = getPostsInThreadQuery.executeQuery();
            while (rs.next()) {
                postList.add(   "post id: " + rs.getInt(1) +
                        " text: " + rs.getString(2) +
                        " post type: " + rs.getString(3) +
                        " creator: " + rs.getString(4) + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        view(user.email, threadId);
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
            }
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
            PreparedStatement makeThreadStatement = conn.prepareStatement
                    ("insert into piazza.thread (Title, Folder_Id) values( (?), (?) )");
            makeThreadStatement.setString(1, title);
            makeThreadStatement.setInt(2, folderId);
            makeThreadStatement.execute();
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
            PreparedStatement makePostStatement = conn.prepareStatement
                    ("insert into piazza.post (PostText, ColourCode, PostType, Thread_Id, Creator) values( (?), (?), (?), (?), (?) )");
            makePostStatement.setString(1, postText);
            makePostStatement.setString(2, colourCode);
            makePostStatement.setString(3, postType);
            makePostStatement.setInt(4, threadId);
            makePostStatement.setString(5, creator);
            makePostStatement.execute();
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
            PreparedStatement connectTagsStatement = conn.prepareStatement
                    ("insert into piazza.tags ( Description, Post_Id) values( (?), (?) )");
            for (String tag: tags) {
                connectTagsStatement.setString(1, tag);
                connectTagsStatement.setInt(2, postId);
                connectTagsStatement.execute();
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
            PreparedStatement findThreadIdFromPostQuery = conn.prepareStatement(
                    "select post.Thread_Id\n" +
                            "from piazza.post\n" +
                            "where post.Post_Id = ( ? );"
            );
            findThreadIdFromPostQuery.setInt(1, postId);
            ResultSet rs = findThreadIdFromPostQuery.executeQuery();
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