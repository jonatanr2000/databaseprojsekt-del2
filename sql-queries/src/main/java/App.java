import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    PiazzaCtrl piazzaCtrl = new PiazzaCtrl();
    Scanner scanner = new Scanner(System.in);
    User user;


    /**
     * Prints the text in a slow fashion, adds a nice feel.
     *
     * @param text String to be printed out.
     * @throws InterruptedException if timeout is interrupted.
     */
    public void print(String text) throws InterruptedException {
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            TimeUnit.MILLISECONDS.sleep(100); // For dramatic effect <3
        }
        System.out.println();
    }

    /**
     * Prints out the text in the list in a slow fashion.
     * @param list List<String> that should be printed.
     * @throws InterruptedException if the printing is interrupted.
     */
    public void print(List<String> list) throws InterruptedException {
        for (String text : list) {
            print(text);
        }
    }


    /**
     * runs the application
     *
     * @throws InterruptedException if the print function is interrupted.
     */
    public void run() throws InterruptedException {
        print("Welcome to low budget PIAZZA \n" +
                "Please log in.");
        String action = "";
        while (!action.matches("exit")) {
            print("Type 'exit' if you want to close the program");
            if (user == null) {
                login();
                action = "";
            } else {
                print("Welcome to the main hub.");
                while (!action.matches("log out") && !action.matches("exit")) {
                    print("create post \t view threads \t statistics \t log out");
                    print("What do you want to do?");

                    action = scanner.nextLine();
                    switch (action) {
                        case "create post": {
                            this.create_post();
                        }
                        break;
                        case "view threads": {
                            this.view_threads();
                        }
                        break;
                        case "statistics": {
                            this.getStatistics();
                        }
                        break;
                        case "log out":
                        case "exit": {
                            piazzaCtrl.setUser(null);
                            this.user = null;
                        }
                        break;
                        default:
                            print("Sorry I did not understand that");
                    }
                }
                print("Logging out, good bye world");
            }
        }
    }

    /**
     * Views the statistics.
     * Checks on user is done in piazzaCtrl
     * If the user doesn't have the privileges to view statistics, they will be shown a error message instead.
     * @throws InterruptedException if print function is interrupted.
     */
    private void getStatistics() throws InterruptedException {
        List<String> stats = piazzaCtrl.getStatistics();
        if (stats.isEmpty()) {
            print("you must be logged in as an instructor to view the stats. If you are logged in as " +
                    "an instructor, please contact support.");
        }
        print(stats);
    }

    /**
     * Prompts the user for email and password (and sells it to China).
     * Tries to log the user in.
     * User gets feedback if email or password is wrong.
     * @throws InterruptedException if print function is interrupted
     */
    private void login() throws InterruptedException {
        print("Email: ");
        String email = scanner.nextLine().trim();
        print("Password: ");
        String password = scanner.nextLine().trim();
        print("logging on with, email: " + email + " and password " + password);
        user = piazzaCtrl.login(email, password);
        piazzaCtrl.setUser(user);
        if (user == null) {
            print("username or password is wrong.");
        }
    }

    /**
     * Shows all the threads.
     * Gives option to go back, view an individual thread or search for posts.
     * If a user wants to see a thread or search for a post, they will be shown that.
     * @throws InterruptedException if print function is interrupted.
     */
    private void view_threads() throws InterruptedException {
        List<String> threads = piazzaCtrl.getThreads();
        print(threads);
        String action = "";
        while (!action.matches("go_back")) {
            print("go_back \t view thread<id> \t search:<search text>");
            action = scanner.nextLine();
            if (action.contains("view thread")) {
                print(action.substring(11));
                int id = Integer.parseInt(action.substring(11));
                view_thread(id);
                threads = piazzaCtrl.getThreads();
                print(threads);
            } else if (action.contains("search:")) {
                String searchText = action.substring(7);
                print("searches for: " + searchText);
                List<Integer> ids = piazzaCtrl.search(searchText.trim());
                if (ids.size() > 0) {
                    List<String> posts = piazzaCtrl.getPosts(ids);
                    print(posts);
                }else {
                    print("Found no posts matching the search criteria.");
                }
            } else if (action.matches("go_back")) {
                print("going back");
            } else {
                print("Sorry I did not understand that.");
            }
        }
    }

    /**
     * Views an induviual thread and all the posts in that thread.
     * User can make a new reply to the first post.
     * Or like one of the posts in the thread
     * Technically they can like any post not just the ones shown.
     * @param threadID int of the thread being viewed
     * @throws InterruptedException if print is interrupted
     */
    private void view_thread(int threadID) throws InterruptedException {
        int postID = piazzaCtrl.getPostInThread(threadID);
        List<String> posts = piazzaCtrl.getPostsInThread(threadID);
        print(posts);
        String action = "";
        while (!action.matches("go_back")) {

            print("go_back \t make_reply \t like<id>");
            action = scanner.nextLine();
            if (action.contains("make_reply")) {
                make_reply(postID);
                posts = piazzaCtrl.getPostsInThread(threadID);
                print(posts);
            } else if (action.contains("like")) {
                Integer likeID = Integer.parseInt(action.substring(4));
                piazzaCtrl.regLike(user.email, likeID);
            } else if (action.matches("go_back")) {
                print("going back");
            } else {
                print("Sorry I did not understand that");
            }
        }
    }

    /**
     * Make a reply to the currently viewed thread.
     * @param postId int of the thread currently being viewed.
     * @throws InterruptedException if print is interrupted.
     */
    private void make_reply(int postId) throws InterruptedException {
        print("text: ");
        String text = scanner.nextLine();
        print("tags: ");
        String tags = scanner.nextLine();
        String[] tager = tags.split(" ");
        String colour;
        if (user.isInstrucor) {
            colour = "orange";
        } else {
            colour = "green";
        }
        int threadId = piazzaCtrl.findThreadIdFromPostId(postId);
        int postID = piazzaCtrl.makePost(text, colour, "reply", threadId, user.email);


        piazzaCtrl.connectTagsAndPost(postID, tager);

        print("creating reply:" + " " + postID + " " + " " + text);
        piazzaCtrl.checkReply(postID, user);
    }

    /**
     * Prompts the user for folder id (choice), title and text of the post.
     * Makes the post.
     * Then, prompts the user for tags.
     * Creates the tags that doesn't exit, and then connects the tags to the post.
     * If the tags did not get connected, the post will exist without tags, "support" can fix this.
     *
     * (Here we think it is better to let the post exist without tags rather than deleting the post if the tags are not connected.)
     */
    private void create_post() throws InterruptedException {
        List<String> folders = piazzaCtrl.showFolders();
        print(folders);
        print("Please choose folder by id");
        int folderId = Integer.parseInt(scanner.nextLine());
        print("Title: ");
        String title = scanner.nextLine();
        print("text: ");
        String text = scanner.nextLine();

        piazzaCtrl.makeThread(title, folderId);
        piazzaCtrl.makePost(text, "red", "post", piazzaCtrl.getThreadIdLatest(), user.email);
        print("Please enter the tags separated by 'space'.");
        String[] tags = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");
        boolean madeTags = piazzaCtrl.connectTagsAndPost(piazzaCtrl.getPostIdLatest(), tags);
        if (!madeTags) {
            print("Was unable to connect the tags to the post, please contact support.");
        }
        print("created :" + " " + folderId + " " + title + " " + text);
    }

    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        app.run();
    }

}