package json;

public class CustomEncoderTest {
  static final String somethingJson = "{'something' : 'yeah, baby!'}";
  static class Something {
  
  }

  static class SomethingElse {}

  static class SomethingEncoder implements CustomEncoder.Encoder<Something> {
    public void encode (StringBuilder b, Object s) {
      b.append(somethingJson);
    }
  }

  static class SmThngElseEnc implements CustomEncoder.Encoder<SomethingElse> {
    public void encode (StringBuilder b, Object o) {
      b.append("'somethingElse'");
    }
  }

  static void testCustom () {
    CustomEncoder enc = new CustomEncoder();
    enc.addEncoder(Something.class, new SomethingEncoder());
    

    String res = JSON.jsonifyCustom(new Something(), enc);
    if (!res.equals(somethingJson)) {
      p("testCustom failed: "+res);
    }
  }

  static void testCustomConstructed () {
    Object [] obj = { "one", "two", new Something()};
    CustomEncoder enc = new CustomEncoder();
    enc.addEncoder(Something.class, new SomethingEncoder());

    String res = JSON.jsonifyCustom(obj, enc);
    if (!res.equals("[\"one\",\"two\",{'something' : 'yeah, baby!'}]")) {
      p("testCustomConstructed failed: "+res);
    }
  }

  static void testCustomConstructedMultEnc() {
    Object [] obj = { "one", "two", new Something(), new SomethingElse()};
    CustomEncoder enc = new CustomEncoder();
    enc.addEncoder(Something.class, new SomethingEncoder());
    enc.addEncoder(SomethingElse.class, new SmThngElseEnc());

    String res = JSON.jsonifyCustom(obj, enc);
    if (!res.equals("[\"one\",\"two\",{'something' : 'yeah, baby!'},'somethingElse']")) {
      p("testCustomConstructedMultEnc failed: "+res);
    }

  }

  public static void main (String [] args) {
    testCustom();
    testCustomConstructed();
    testCustomConstructedMultEnc();
  }

  static void p (Object o) {
    System.out.println(o);
  }
}
