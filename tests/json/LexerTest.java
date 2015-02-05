package json;

import java.math.BigDecimal;

public class LexerTest {

  static int countTok;
  static int countTokString;
  static int countTokBD;
  
  static Lexer.CB cb = new Lexer.CB() {
    void tok(Lexer.Token tok) {
      countTok++;
    }
    void tok(String c) {
      countTokString++;
    }
    void tok(BigDecimal c) {
      countTokBD++;
    }
  };

  static void testClean() {
  	String	json	= "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		byte [] jsona = json.getBytes();
	

		Lexer.lexer.lex(jsona, cb); 
    if (countTok != 14 || countTokString != 4 || countTokBD != 5) {
      p("failed: invalid token count"); 
    }
    countTok = countTokString = countTokBD = 0;
		
		json	= "{\"a\":\"\\u2603\",\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		Lexer.lexer.lex(json.getBytes(), cb);
    if (countTok != 14 || countTokString != 5 || countTokBD != 4) {
      p("failed: invalid token count");
    }
    countTok = countTokString = countTokBD = 0;

		json	= "{}";
		Lexer.lexer.lex(json.getBytes(), cb);
    if (countTok != 2 || countTokString != 0 || countTokBD != 0) {
      p("failed: invalid token count");
    }
    countTok = countTokString = countTokBD = 0;

		json	= "{{},{}}";
		Lexer.lexer.lex(json.getBytes(), cb);
    if (countTok != 7 || countTokString != 0 || countTokBD != 0) {
      p("failed: invalid token count"); 
    }
    countTok = countTokString = countTokBD = 0;

		json	= "[]";
		Lexer.lexer.lex(json.getBytes(), cb);
    if (countTok != 2 || countTokString != 0 || countTokBD != 0) {
      p("failed: invalid token count");
    }
    countTok = countTokString = countTokBD = 0;

  }
  
  static void testFail() {
		String json	= "{\"a\":19560954.609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
    try {
			Lexer.lexer.lex(json.getBytes(), cb);
			p("failed: should not be here! 19560954.609845.4456456 is not a number");
		} catch (RuntimeException re) {
			p("expected failure: \n\t"+re.getMessage());
		}

  }

  static void testNewline() {
		String json	= "{\"a\":\"\\u2603\",\"b\":[1,2,3],\n\"dindong\":{\"b\":12}}";
    Lexer.lexer.lex(json.getBytes(), cb);
  }

  public static void main(String [] args) {
    testClean();
    testFail();
  }
  
  static void p (Object o) { System.out.println(o); }
}
