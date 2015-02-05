package json;
import java.util.Map;
import java.util.HashMap;
public class EncoderTest {
  static void testBaseTypes () {
    StringBuilder b = new StringBuilder();
    Encoder.encode(b, true);
    Encoder.encode(b, 1);
    Encoder.encode(b, (long)1);
    Encoder.encode(b, (float)1.0);
    Encoder.encode(b, (double)1.0);
    Encoder.encode(b, (byte)1);
    Encoder.encode(b, (short)1);
    Encoder.encode(b, '1');
    if (!b.toString().equals("true111.01.01149")) {
      p("testBaseTypes failed: "+b.toString());
    }
  }

  static void testMap() {
      String	json	= "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
      Map m = (Map)JSON.parse(json);
      if (!m.containsKey("a") ||
          !m.containsKey("b") ||
          !m.containsKey("dindong")) {
        p("testMap failed: missing key");    
      }
      p("should look the same");
      p(json);		
      p(JSON.jsonify(m));
  }

  static void testArray () {
      int [] is = {1,2,3};
      String r = JSON.jsonify(is);
      if (!r.equals("[1,2,3]")) {
        p("testArray failed: "+r);
      }
  }

  static void testCircular() {
      Map map = new java.util.HashMap();
      map.put("bla", map);
      try {
        p(JSON.jsonify(map));
        p("failed: data contained circular refs, should have thrown exception");
      } catch (RuntimeException re) {
        p("expected failure: \n\t"+re.getMessage());
      }
  }

  static void testNonBaseType() {
     try {
        JSON.jsonify(System.out);
      } catch (RuntimeException re) {
        p("expected failure: \n\t"+re.getMessage());
        return;
      }
      p("failed expected unexpected object failure");

  }

  static void testBoolean() {
    Map map = new HashMap();
    map.put("bla", true);
    String json = JSON.jsonify(map);
    if (!(json.equals("{\"bla\":true}")) ){
      p("testBoolean failed: "+json);
    }
  }


  public static void main (String [] args) {
    testBaseTypes();
    testMap();
    testArray();
    testCircular();
    testNonBaseType();
    testBoolean();
  }
  static void p (Object o) { System.out.println(o);}
}
