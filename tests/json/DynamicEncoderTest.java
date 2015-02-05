package json;


public class DynamicEncoderTest {
  static final String HOUSE_JSON = "{'house': {'number_of_windows':1, 'roof': 'yes'}}";
  static class House {
    int numWindows;
    boolean roof;
    public String toJSON () {
      return HOUSE_JSON;
    }
  }
  
  public static void testHouse () {
    String json = JSON.jsonifyDynamic(new House());
    if (!json.equals(HOUSE_JSON)) {
      p("failed: testHouse:"+json);
    }
  }

  public static void main (String [] args) {
    testHouse();
  }

  static void p (Object o) {
    System.out.println(o);
  }
}
