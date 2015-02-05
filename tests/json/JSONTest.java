package json;

public class JSONTest {
  
  static void testInvalid() {
    String json  = "{{\"a\":19560954609845.4456456}:1,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		try {
			Object o = JSON.parse(json);
			p("failed: JSON-Obj as key should cause exception");
		} catch (Throwable t) {
			p("failed as expected: \n\t"+t.getMessage());
		}

  }
  
  static void testNoComma() {
    // too liberal behaviour, key value don't need to be separated by comma
    String json  = "{\"a\" 19560954609845.4456456 \"b\" [1 2 3] \"dindong\" {\"b\" 12}}";
    try {
      JSON.parse(json);
      p("testNoComma failed, but arguably ok");
    } catch (RuntimeException t) {
      p("expected falure: "+t.getMessage());
    }
  }

  static void testState() {
    String json  = "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
    
    JSON j = new JSON();
		byte [] a = json.getBytes();
		byte [] b = new byte[1];
		for (int i = 0; /*i!=a.length*/; ++i) {
			System.arraycopy(a,i,b,0,1); /* heh? whydidothat? */
			j.parse(b); 
			if (j.done()) break; /* to test this I guess */
                           /* should provide parse(byte[], off, len) */
		}

    Object o1 = JSON.parse(json);
    if (!o1.equals(j.obj())) {
      p("testState failed");
    }

  }
  
  static void testRndFail() {
    String json = "{\"key\":\"value\":\"value\"}";
    try {
      JSON.parse(json);
      p("testRndFail missed incorrect COLON");
    } catch (RuntimeException e) {
    
    }
    json = "[,,,]";
    try {
      JSON.parse(json);
      p("testRndFail missed incorrect COMMA");
    } catch (RuntimeException e) {}
    
    json = "{ \"toll\" : \"toll\" }\n";
    JSON.parse(json);
  }

  public static void main (String [] args) {
    testInvalid();
    testNoComma();
    testState();
    testRndFail();
  }

  static void p (Object o) {System.out.println(o);}
}


