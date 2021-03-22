import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    public App() {

    }

    public void print(String text) throws InterruptedException {
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            TimeUnit.MILLISECONDS.sleep((long) 100); // in milliseconds
        }
        System.out.println();
    }

    public void run() throws InterruptedException {
        PiazzaCtrl piazzaCtrl = new PiazzaCtrl();
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
                    //Do nothing
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


    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        app.run();
    }

}