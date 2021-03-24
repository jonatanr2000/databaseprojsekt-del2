import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    PiazzaCtrl piazzaCtrl = new PiazzaCtrl();


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
    public void run() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        print("Welcome to low budget PIAZZA \n " +
                "Please log in.");
        while (piazzaCtrl.user == null) {
            print("Email: ");
            String email = scanner.nextLine().trim();
            print("Password: ");
            String password = scanner.nextLine().trim();
            print("logging on with, email: " + email + " and password " + password);
            piazzaCtrl.login(email, password);
        }

        print("Welcome to the main hub.");
        String action = "";
        while (!action.matches("log out")) {
            print("create post \t view posts \t statistics \t log out");
            print("What do you want to do?");

            action = scanner.nextLine();
            switch(action) {
                case "create post": {
                    //this.create_post();
                }
                break;
                case "view posts": {
                    //Do nothing
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

    /**
     * Guides the user in creating a post.
     */
    /*private void create_post() {
        piazzaCtrl.show
    }*/

    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        app.run();
    }

}