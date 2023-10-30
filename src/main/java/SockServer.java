import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;

/**
 * A class to demonstrate a simple client-server connection using sockets.
 *
 */
public class SockServer {
  static Socket sock;
  static DataOutputStream os;
  static ObjectInputStream in;

  static int port = 8888;

  public static void main (String args[]) {

    if (args.length != 1) {
      System.out.println("Expected arguments: <port(int)>");
      System.exit(1);
    }

    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }

    try {
      //open socket
      ServerSocket serv = new ServerSocket(port);
      System.out.println("Server ready for connections");

      /**
       * Simple loop accepting one client and calling handling one request.
       *
       */


      while (true){
        System.out.println("Server waiting for a connection");
        sock = serv.accept(); // blocking wait
        System.out.println("Client connected");

        // setup the object reading channel
        in = new ObjectInputStream(sock.getInputStream());

        // get output channel
        OutputStream out = sock.getOutputStream();

        // create an object output writer (Java only)
        os = new DataOutputStream(out);

        boolean connected = true;
        while (connected) {
          String s = "";
          try {
            s = (String) in.readObject(); // attempt to read string in from client
          } catch (Exception e) { // catch rough disconnect
            System.out.println("Client disconnect");
            connected = false;
            continue;
          }

          JSONObject res = isValid(s);

          if (res.has("ok")) {
            writeOut(res);
            continue;
          }

          JSONObject req = new JSONObject(s);

          res = testField(req, "type");
          if (!res.getBoolean("ok")) { // no "type" header provided
            res = noType(req);
            writeOut(res);
            continue;
          }
          // check which request it is (could also be a switch statement)
          if (req.getString("type").equals("echo")) {
            res = echo(req);
          } else if (req.getString("type").equals("add")) {
            res = add(req);
          } else if (req.getString("type").equals("addmany")) {
            res = addmany(req);
          } else if (req.getString("type").equals("charcount")) {
            res = charCount(req);
          }
          else if (req.getString("type").equals("storyboard")) {
            res = storyboard(req);
          } else {
            res = wrongType(req);
          }
          writeOut(res);
        }
        // if we are here - client has disconnected so close connection to socket
        overandout();
      }
    } catch(Exception e) {
      e.printStackTrace();
      overandout(); // close connection to socket upon error
    }
  }


  /**
   * Checks if a specific field exists
   *
   */
  static JSONObject testField(JSONObject req, String key){
    JSONObject res = new JSONObject();

    // field does not exist
    if (!req.has(key)){
      res.put("ok", false);
      res.put("message", "Field " + key + " does not exist in request");
      return res;
    }
    return res.put("ok", true);
  }

  // handles the simple echo request
  static JSONObject echo(JSONObject req){
    System.out.println("Echo request: " + req.toString());
    JSONObject res = testField(req, "data");
    if (res.getBoolean("ok")) {
      if (!req.get("data").getClass().getName().equals("java.lang.String")){
        res.put("ok", false);
        res.put("message", "Field data needs to be of type: String");
        return res;
      }

      res.put("type", "echo");
      res.put("echo", "Here is your echo: " + req.getString("data"));
    }
    return res;
  }

  // handles the simple add request with two numbers
  static JSONObject add(JSONObject req){
    System.out.println("Add request: " + req.toString());
    JSONObject res1 = testField(req, "num1");
    if (!res1.getBoolean("ok")) {
      return res1;
    }

    JSONObject res2 = testField(req, "num2");
    if (!res2.getBoolean("ok")) {
      return res2;
    }

    JSONObject res = new JSONObject();
    res.put("ok", true);
    res.put("type", "add");
    try {
      res.put("result", req.getInt("num1") + req.getInt("num2"));
    } catch (org.json.JSONException e){
      res.put("ok", false);
      res.put("message", "Field num1/num2 needs to be of type: int");
    }
    return res;
  }

  // implemented StoryBoard
  static JSONObject storyboard(JSONObject request) {
    System.out.println("Storyboard request: " + request.toString());

    // Check if the "type" field is present and set to "storyboard"
    if (!request.has("type") || !request.getString("type").equals("storyboard")) {
      return createErrorResponse("Invalid or missing 'type' field in the request.");
    }

    // Check if the "view" field is present and set to true or false
    if (!request.has("view") || (request.optBoolean("view") != true && request.optBoolean("view") != false)) {
      return createErrorResponse("Invalid or missing 'view' field in the request.");
    }

    boolean view = request.getBoolean("view");

    // Initialize the storyboard and users arrays
    JSONArray story = new JSONArray();
    JSONArray users = new JSONArray();

    // Add a dummy story to the storyboard
    story.put("Once upon a time, in a faraway land...");
    users.put("Storyteller123");

    if (view) {
      // If 'view' is true, construct the success response to view the storyboard
      JSONObject successResponse = new JSONObject();
      successResponse.put("ok", true);
      successResponse.put("type", "storyboard");
      successResponse.put("storyboard", story);
      successResponse.put("users", users);
      return successResponse;
    } else {
      // If 'view' is false, the user wants to add to the storyboard

      // Check if the "name" and "story" fields are present
      if (!request.has("name") || !request.has("story")) {
        return createErrorResponse("Fields 'name' and 'story' are required to add to the storyboard.");
      }

      // Extract the "name" and "story" fields (username and sentence to be added)
      String name = request.getString("name");
      String storyStr = request.getString("story");

      // Add the user and their story to the storyboard
      users.put(name);
      story.put(storyStr);

      // Construct the success response after adding to the storyboard
      JSONObject successResponse = new JSONObject();
      successResponse.put("ok", true);
      successResponse.put("type", "storyboard");
      successResponse.put("storyboard", story);
      successResponse.put("users", users);
      return successResponse;
    }
  }




  // implemented CharCount
  static JSONObject charCount(JSONObject req) {
    System.out.println("CharCount request: " + req.toString());

    // Check if the "count" field is present in the request
    if (!req.has("count")) {
      return createErrorResponse("Field 'count' is missing in the request.");
    }

    // Extract the "count" field (the string to be analyzed)
    String countStr = req.getString("count");

    // Check if the "findchar" field is present and set to true
    boolean findChar = req.optBoolean("findchar", false);

    if (findChar) {
      // If the "findchar" field is true, check if the "find" field is present
      if (!req.has("find")) {
        return createErrorResponse("Field 'find' is missing in the request when 'findchar' is true.");
      }

      // Extract the "find" field (the character to search for)
      String findCharStr = req.getString("find");

      // Count the number of occurrences of the specified character in the string
      int charCount = 0;
      for (int i = 0; i < countStr.length(); i++) {
        if (countStr.charAt(i) == findCharStr.charAt(0)) {
          charCount++;
        }
      }

      // Build the success response for specific character search
      JSONObject successResponse = new JSONObject();
      successResponse.put("ok", true);
      successResponse.put("type", "charcount");
      successResponse.put("result", charCount);

      return successResponse;
    }

    // If findchar is false, calculate and build the success response for general character counting
    int totalCharCount = countStr.length();
    JSONObject successResponse = new JSONObject();
    successResponse.put("ok", true);
    successResponse.put("type", "charcount");
    successResponse.put("result", totalCharCount);

    return successResponse;
  }

//  Check for error

  static JSONObject createErrorResponse(String errorMessage) {
    JSONObject errorResponse = new JSONObject();
    errorResponse.put("ok", false);
    errorResponse.put("type", "charcount");
    errorResponse.put("message", errorMessage);
    return errorResponse;
  }


  // handles the simple addmany request
  static JSONObject addmany(JSONObject req){
    System.out.println("Add many request: " + req.toString());
    JSONObject res = testField(req, "nums");
    if (!res.getBoolean("ok")) {
      return res;
    }

    int result = 0;
    JSONArray array = req.getJSONArray("nums");
    for (int i = 0; i < array.length(); i ++){
      try{
        result += array.getInt(i);
      } catch (org.json.JSONException e){
        res.put("ok", false);
        res.put("message", "Values in array need to be ints");
        return res;
      }
    }

    res.put("ok", true);
    res.put("type", "addmany");
    res.put("result", result);
    return res;
  }

  // creates the error message for wrong type
  static JSONObject wrongType(JSONObject req){
    System.out.println("Wrong type request: " + req.toString());
    JSONObject res = new JSONObject();
    res.put("ok", false);
    res.put("message", "Type " + req.getString("type") + " is not supported.");
    return res;
  }

  // creates the error message for no given type
  static JSONObject noType(JSONObject req){
    System.out.println("No type request: " + req.toString());
    JSONObject res = new JSONObject();
    res.put("ok", false);
    res.put("message", "No request type was given.");
    return res;
  }

  // From: https://www.baeldung.com/java-validate-json-string
  public static JSONObject isValid(String json) {
    try {
      new JSONObject(json);
    } catch (JSONException e) {
      try {
        new JSONArray(json);
      } catch (JSONException ne) {
        JSONObject res = new JSONObject();
        res.put("ok", false);
        res.put("message", "req not JSON");
        return res;
      }
    }
    return new JSONObject();
  }

  // sends the response and closes the connection between client and server.
  static void overandout() {
    try {
      os.close();
      in.close();
      sock.close();
    } catch(Exception e) {e.printStackTrace();}

  }

  // sends the response and closes the connection between client and server.
  static void writeOut(JSONObject res) {
    try {
      os.writeUTF(res.toString());
      // make sure it wrote and doesn't get cached in a buffer
      os.flush();

    } catch(Exception e) {e.printStackTrace();}

  }
}