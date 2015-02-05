package json;

public class Main {
  public static void main (String [] args) throws Throwable {
    byte[] bytes = new byte[1024];
    JSON json = new JSON();
    int len;
    while (-1 != (len = System.in.read(bytes))) {
      json.parse(bytes, 0, len); 
    }
    p(JSON.jsonify(json.obj()));
  }

  static void p(Object o) {
    System.out.println(o);
  }
}
