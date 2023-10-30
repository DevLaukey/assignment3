import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Scanner;

class SockClient {
  static Socket sock = null;
  static String host = "localhost";
  static int port = 8888;
  static OutputStream out;
  static ObjectOutputStream os;
  static DataInputStream in;

  public static void main(String args[]) {

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.exit(1);
    }

    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }

    try {
      connect(host, port);
      System.out.println("Client connected to server.");
      boolean requesting = true;
      while (requesting) {
        System.out.println("What would you like to do: 1 - echo, 2 - add, 3 - addmany, 4 - charcount, 5 - storyboard (0 to quit)");
        Scanner scanner = new Scanner(System.in);
        int choice = Integer.parseInt(scanner.nextLine());

        JSONObject json = new JSONObject(); // request object
        switch (choice) {
          case 0:
            System.out.println("Choose quit. Thank you for using our services. Goodbye!");
            requesting = false;
            break;
          case 1:
            System.out.println("Choose echo, which String do you want to send?");
            String message = scanner.nextLine();
            json.put("type", "echo");
            json.put("data", message);
            break;
          case 2:
            System.out.println("Choose add, enter first number:");
            String num1 = scanner.nextLine();
            json.put("type", "add");
            json.put("num1", num1);

            System.out.println("Enter second number:");
            String num2 = scanner.nextLine();
            json.put("num2", num2);
            break;
          case 3:
            System.out.println("Choose addmany, enter as many numbers as you like, when done choose 0:");
            JSONArray array = new JSONArray();
            String num = "1";
            while (!num.equals("0")) {
              num = scanner.nextLine();
              array.put(num);
              System.out.println("Got your " + num);
            }
            json.put("type", "addmany");
            json.put("nums", array);
            break;
          case 4:
            System.out.println("Choose charcount, enter the text you want to count characters in:");
            String text = scanner.nextLine();
            json.put("type", "charcount");
            json.put("findchar", false);
            json.put("count", text);
            break;
          case 5:
            System.out.println("Choose storyboard: 1 - add, 2 - view");
            int storyboardChoice = Integer.parseInt(scanner.nextLine());
            json.put("type", "storyboard");
            if (storyboardChoice == 1) {
              System.out.println("Choose to add to the storyboard, enter your name:");
              String name = scanner.nextLine();
              System.out.println("Enter the sentence you want to add to the storyboard:");
              String sentence = scanner.nextLine();
              json.put("view", false);
              json.put("name", name);
              json.put("story", sentence);
            } else if (storyboardChoice == 2) {
              System.out.println("Choose to view the storyboard.");
              json.put("view", true);
            }
            break;
        }

        if (!requesting) {
          continue;
        }

        // Write the whole message
        os.writeObject(json.toString());
        os.flush();

        // Handle the response
        String i = (String) in.readUTF();
        JSONObject res = new JSONObject(i);
        System.out.println("Got response: " + res);
        if (res.getBoolean("ok")) {
          if (res.getString("type").equals("echo")) {
            System.out.println(res.getString("echo"));
          } else if (res.getString("type").equals("charcount")) {
            System.out.println("Character Count: " + res.getInt("result"));
          } else if (res.getString("type").equals("storyboard")) {
            JSONArray storyboard = res.getJSONArray("storyboard");
            JSONArray users = res.getJSONArray("users");
            System.out.println("Storyboard:");
            for (int j = 0; j < storyboard.length(); j++) {
              System.out.println(users.getString(j) + ": " + storyboard.getString(j));
            }
          } else {
            System.out.println(res.getInt("result"));
          }
        } else {
          System.out.println(res.getString("message"));
        }
      }
      overandout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void overandout() throws IOException {
    in.close();
    os.close();
    sock.close();
  }

  public static void connect(String host, int port) throws IOException {
    sock = new Socket(host, port);
    out = sock.getOutputStream();
    os = new ObjectOutputStream(out);
    in = new DataInputStream(sock.getInputStream());
  }
}
