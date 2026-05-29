import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TodoApp {
    private static final List<String> todos = new ArrayList<>();

    public static void main(String[] args) {
        // Start CPU stress threads to consume 100% CPU
        startCpuStress();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Todo Application!");
        System.out.println("Note: Application is running with heavy background processing...");

        while (true) {
            System.out.println("\n--- Todo Menu ---");
            System.out.println("1. View Todos");
            System.out.println("2. Add Todo");
            System.out.println("3. Remove Todo");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewTodos();
                    break;
                case "2":
                    System.out.print("Enter new todo: ");
                    String newTodo = scanner.nextLine();
                    todos.add(newTodo);
                    System.out.println("Added: " + newTodo);
                    break;
                case "3":
                    System.out.print("Enter todo number to remove: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine()) - 1;
                        if (index >= 0 && index < todos.size()) {
                            String removed = todos.remove(index);
                            System.out.println("Removed: " + removed);
                        } else {
                            System.out.println("Invalid todo number.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number.");
                    }
                    break;
                case "4":
                    System.out.println("Exiting Todo Application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewTodos() {
        if (todos.isEmpty()) {
            System.out.println("Your todo list is empty.");
        } else {
            System.out.println("Your Todos:");
            for (int i = 0; i < todos.size(); i++) {
                System.out.println((i + 1) + ". " + todos.get(i));
            }
        }
    }

    private static void startCpuStress() {
        int cores = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cores; i++) {
            Thread t = new Thread(() -> {
                while (true) {
                    // Complex math operations to prevent JIT optimization and ensure maximum CPU usage
                    double v = Math.pow(Math.random(), Math.random());
                }
            });
            t.setDaemon(true); // Daemon threads will stop when the main program exits
            t.start();
        }
    }
}
