import java.sql.SQLException;
import java.util.Arrays;
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
            TimeUnit.MILLISECONDS.sleep(100); // in milliseconds
        }
        System.out.println();
    }


    /**
     * runs the application
     *
     * @throws InterruptedException if the print function is interrupted.
     */
    public void run() throws InterruptedException, SQLException {
        print("Welcome to low budget PIAZZA \n" +
                "Please log in.");
        String action = "";
        while (!action.matches("exit")) {
            print("Type 'exit' if you want to close the program");
            if (piazzaCtrl.user == null) {
                login();
            } else {
                print("Welcome to the main hub.");
                while (!action.matches("log out") && !action.matches("exit")) {
                    print("create post \t view posts \t statistics \t log out");
                    print("What do you want to do?");

                    action = scanner.nextLine();
                    switch (action) {
                        case "create post": {
                            this.create_post();
                        }
                        break;
                        case "view posts": {
                            this.view_threads();
                        }
                        break;
                        case "statistics": {
                            this.getStatistics();
                        }
                        break;
                        case "log out":
                        case "exit": {
                            //Do nothing
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

    private void getStatistics() throws InterruptedException {
        List<String> stats = piazzaCtrl.getStatistics();
        for (String line : stats) {
            print(line);
        }
    }

    private void login() throws InterruptedException {
        print("Email: ");
        String email = scanner.nextLine().trim();
        print("Password: ");
        String password = scanner.nextLine().trim();
        print("logging on with, email: " + email + " and password " + password);
        user = piazzaCtrl.login(email, password);
        makePost.setUser(user);
    }

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
            } else if (action.contains("search:")) {
                String searchText = action.substring(7);
                // List<Integer> indexes = piazzaCtrl.search(searchText);
                List<Integer> indexes = Arrays.asList(1, 4, 5, 10);

                makePost.showThreads(indexes);
            } else if (action.matches("go_back")) {
                print("going back");
            } else {
                print("Sorry I did not understand that.");
            }
        }
    }

    private void view_thread(int id) throws InterruptedException {
        piazzaCtrl.view(user.email, id);
        int postID = makePost.showPostsInThread(id);
        String action = "";
        while (!action.matches("go_back")) {
            print("go_back \t make_reply \t like<id>");
            action = scanner.nextLine();
            if (action.contains("make_reply")) {
                make_reply(postID);
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
     * Guides the user in creating a post.
     */
    private void create_post() throws InterruptedException, SQLException {
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

    public static void main(String[] args) throws InterruptedException, SQLException {
        App app = new App();
        app.run();
    }

}