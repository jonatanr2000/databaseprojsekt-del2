import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    PiazzaCtrl piazzaCtrl = new PiazzaCtrl();
    MakePost makePost = new MakePost();
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
            //TimeUnit.MILLISECONDS.sleep(100); // in milliseconds
        }
        System.out.println();
    }

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
            if (piazzaCtrl.user == null) {
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
                            piazzaCtrl.user = null;
                            makePost.setUser(null);
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
        for (String line : stats) {
            print(line);
        }
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
        makePost.setUser(user);
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
        List<Integer> threadIds = makePost.showThreads();
        String action = "";
        while (!action.matches("go_back")) {
            print("go_back \t view_post<id> \t search:<search text>");
            action = scanner.nextLine();
            if (action.contains("view_post")) {
                print(action.substring(9));
                int id = Integer.parseInt(action.substring(9));
                view_thread(id);
                makePost.showThreads();
            } else if (action.contains("search:")) {
                String searchText = action.substring(7);
                print("searches for: " + searchText);
                List<Integer> ids = piazzaCtrl.search(searchText.trim());
                if (ids.size() > 0) {
                    makePost.showThreads(ids);
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
     * @param id int of the thread being viewed
     * @throws InterruptedException if print is interrupted
     */
    private void view_thread(int id) throws InterruptedException {
        piazzaCtrl.view(user.email, id);
        int postID = makePost.getPostInThread(id);
        List<String> posts = makePost.getPostsInThread(id);
        print(posts)
        String action = "";
        while (!action.matches("go_back")) {

            print("go_back \t make_reply \t like<id>");
            action = scanner.nextLine();
            if (action.contains("make_reply")) {
                make_reply(postID);
                makePost.showPostsInThread(id);
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
     * @param id int of the thread currently being viewed.
     * @throws InterruptedException if print is interrupted.
     */
    private void make_reply(int id) throws InterruptedException {
        print("text: ");
        String text = scanner.nextLine();
        String colour;
        if (user.isInstrucor) {
            colour = "orange";
        } else {
            colour = "green";
        }
        int postID = makePost.makePost(text, colour, "reply", id, user.email);

        print("creating reply:" + " " + postID + " " + " " + text);
        piazzaCtrl.checkReply(id, user);
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
        makePost.showFolders();
        print("Please choose folder by id");
        int folderId = Integer.parseInt(scanner.nextLine());
        print("Title: ");
        String title = scanner.nextLine();
        print("text: ");
        String text = scanner.nextLine();

        makePost.makeThread(title, folderId);
        makePost.makePost(text, "red", "post", makePost.getThreadIdLatest(), user.email);
        print("Please enter the tags separated by 'space'.");
        String[] tags = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");
        boolean madeTags = makePost.connectTagsAndPost(makePost.getPostIdLatest(), tags);
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