import java.sql.*;
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
     * @param text String to be printed out.
     * @throws InterruptedException if timeout is interrupted.
     */
    public void print(String text) throws InterruptedException {
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            TimeUnit.MILLISECONDS.sleep((long) 100); // in milliseconds
        }
        System.out.println();
    }


    /**
     * runs the application
     * @throws InterruptedException if the print function is interrupted.
     */
    public void run() throws InterruptedException, SQLException {
        print("Welcome to low budget PIAZZA \n " +
                "Please log in.");
        while (piazzaCtrl.user == null) {
            print("Email: ");
            String email = scanner.nextLine().trim();
            print("Password: ");
            String password = scanner.nextLine().trim();
            print("logging on with, email: " + email + " and password " + password);
            user = piazzaCtrl.login(email, password);
        }

        print("Welcome to the main hub.");
        String action = "";
        while (!action.matches("log out")) {
            print("create post \t view posts \t statistics \t log out");
            print("What do you want to do?");

            action = scanner.nextLine();
            switch(action) {
                case "create post": {
                    this.create_post();
                }
                break;
                case "view posts": {
                    this.view_threads();
                }
                break;
                case"statistics": {
                    piazzaCtrl.getPosts();
                }
                break;
                case "log out": {
                    //Do nothing
                }
                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + action);
            }

        }
        print("Logging out, good bye world");

    }

    private void view_threads() throws InterruptedException {
        List<Integer> threadIds = makePost.viewPost();
        String action = null;
        print("go_back \t view_post<id>");
        action = scanner.nextLine();
        if (action.contains("view_post")) {
            print(action.substring(9));
            int id = Integer.parseInt(action.substring(9));
            view_thread(id);
        }
    }

    private void view_thread(int id) throws InterruptedException {
        makePost.viewThread(id);
        print("go_back \t make_reply<id>");
        String action = scanner.nextLine();
        if(action.contains("make_reply")) {
            int post_id = 
            make_reply(id);
        }
    }

    private void make_reply(int id) {
        piazzaCtrl.checkReply(id);
    }

    /**
     * Guides the user in creating a post.
     */
    private void create_post() throws InterruptedException, SQLException {
        makePost.showFolders();
        print("Please choose folder by id");
        int folderid = Integer.parseInt(scanner.nextLine());
        print("Title: ");
        String title = scanner.nextLine();
        print("text: ");
        String text = scanner.nextLine();

        makePost.makeThread(title, folderid);
        makePost.makePost(text, "red", "post", makePost.getThreadIdLatest(), user.email);
        print("Please enter the tags separated by 'space'.");
        String[] tags = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");
        makePost.connectTagsAndPost(makePost.getPostIdLatest(), tags);
        print("creating :"  + " " + Integer.toString(folderid) + " " + title + " " + text);



    }

    public static void main(String[] args) throws InterruptedException, SQLException {
        App app = new App();
        app.run();
    }

}