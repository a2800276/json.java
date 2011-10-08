package json;

import java.math.BigDecimal;

public class Lexer {
  enum State {
    VALUE,
    STRING_START,
    STR_ESC,
    NUMBER_START,
    ARRAY_START,
    T,
    TR,
    TRU,
    TRUE,
    F,
    FA,
    FAL,
    FALS,
    N,
    NU,
    NUL,
    AFTER_VALUE,
    HEX1,
    HEX2,
    HEX3,
    HEX4,
    
  }
  enum Token {
    LCURLY,
    LSQUARE,
    RCURLY,
    RSQUARE,
    TRUE,
    FALSE,
    NULL,
    COMMA,
    COLON,
    NUMBER,
    STRING,
    STR_HEX,
  }
  static abstract class CB {
    abstract void tok(Token tok);
    abstract void tok(String s);
    abstract void tok(BigDecimal s);

  }


  State state = State.VALUE;
  StringBuffer cache; 
  StringBuffer hexCache; 

  void lex (char [] arr, CB cb) {
    for (int i = 0; i != arr.length; ++i) {
      char c = arr[i];
      switch (state) {
        case VALUE:
          if (isWS(c)) {
            continue;
          } 
          switch (c) {
            // String
            case '"':
              state = State.STRING_START;
              cache = new StringBuffer();
              continue;

            // Number
            case '-':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '0':
              state = State.NUMBER_START;
              cache = new StringBuffer();
              cache.append(c); 
              continue;

            // Object
            case '{':
              state = State.VALUE;
              cb.tok(Token.LCURLY);
              continue;

            // Array
            case '[':
              state = State.VALUE;
              cb.tok(Token.LSQUARE);
              continue;
            // true
            case 't':
              state = State.T;
              continue;
            // false
            case 'f':
              state = State.F;
              continue;
            // null
            case 'n':
              state = State.N;
              continue;
            default:
              error();
          }

        case T:
          if ('r' == c) {
            state = State.TR;
            continue;
          }
          error(c);
        case TR:
          if ('u' == c) {
            state = State.TRU;
            continue;
          }
          error();
        case TRU:
          if ('e' == c) {
            cb.tok(Token.TRUE);
            state = State.AFTER_VALUE;
            continue;
          }
          error();

        case F:
          if ('a' == c) {
            state = State.FA;
            continue;
          }
          error();
        case FA:
          if ('l' == c) {
            state = State.FAL;
            continue;
          }
          error();
        case FAL:
          if ('s' == c) {
            state = State.FALS;
            continue;
          }
          error();
        case FALS:
          if ('e' == c) {
            cb.tok(Token.FALSE);
            state = State.AFTER_VALUE;
            continue;
          }
          error();

        case N:
          if ('u' == c) {
            state = State.NU;
            continue;
          }
          error();
        case NU:
          if ('l' == c) {
            state = State.NUL;
            continue;
          }
          error();
        case NUL:
          if ('l' == c) {
            cb.tok(Token.NULL);
            state = State.AFTER_VALUE;
            continue;
          }
          
        case AFTER_VALUE: 
          if (isWS(c)) {
            continue;
          } 
          switch(c) {
            case '}':
              cb.tok(Token.RCURLY);
              continue;
            case ']':
              cb.tok(Token.RSQUARE);
              continue;
            case ',':
              cb.tok(Token.COMMA);
              continue;
            case ':':
              cb.tok(Token.COLON);
              continue;
            default:
              --i;
              state = State.VALUE;
              continue;
          }

        case NUMBER_START:
          switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'e':
            case 'E':
            case '+':
            case '-':
            case '.':
              cache.append(c);
              continue;
            default:
              cb.tok(num(cache));
              --i;
              state = State.AFTER_VALUE;
              continue;
          } 

        case STRING_START:
          switch (c) {
            case '"':
              cb.tok(cache.toString());
              state = State.AFTER_VALUE;
              continue;
            case '\\':
              state = State.STR_ESC;
              continue;
            default:
              if (Character.isISOControl(c)) {
                error();
              }
              cache.append(c);
              continue;
          }
        case STR_ESC:
          switch (c) {
            case '"':
            case '/':
            case '\\':
              cache.append(c);
              break;
            case 'b':
              cache.append('\b');
              break;
            case 'f':
              cache.append('\f');
              break;
            case 'n':
              cache.append('\r');
              break;
            case 'r':
              cache.append('\r');
              break;
            case 't':
              cache.append('\t');
              break;
            case 'u':
              state = State.HEX1;
              continue;
            default:
              error();
          }
          state = State.STRING_START;
          continue;
        case HEX1:
          if (!isHex(c)) {error();}
          hexCache = new StringBuffer();
          hexCache.append(c);
          state = State.HEX2;
          continue;
        case HEX2:
          if (!isHex(c)) {error();}
          hexCache.append(c);
          state = State.HEX3;
          continue;
        case HEX3:
          if (!isHex(c)) {error();}
          hexCache.append(c);
          state = State.HEX4;
          continue;
        case HEX4:
          if (!isHex(c)) {error();}
          char u = toChar(hexCache);
          cache.append(u);
          state = State.STRING_START;
        default:
         error();
      } // state switch 
    }// for
  }
  
  boolean isWS(char c) {
    return Character.isWhitespace(c);
  }
  
  char toChar (StringBuffer buf) {
    assert buf.length() == 4;
    return (char)Integer.parseInt(buf.toString(), 16);
  }
  
  BigDecimal num (StringBuffer b) {
    BigDecimal bd = null;
    try {
      bd =  new BigDecimal(b.toString());
    } catch (Throwable t) {
      error("not a number: "+b.toString());
    }
    return bd;
  }

  
  boolean isHex(char c) {
    switch (c) {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        return true;
      default :
        return false;
    }
  }
  
  void error () {
    error("??? "+state);
  }

  void error (char c) {
    error("unexpected char: "+c+" in state: "+state);
  }

  void error(String mes) {
    throw new RuntimeException(mes);
  }

  public static void main (String [] args) {
    String  json  = "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
    char [] jsona = json.toCharArray();
  
    Lexer lex = new Lexer();
    Lexer.CB cb = new Lexer.CB() {
      void tok(Token tok) {
        p(tok);
      }
      void tok(String c) {
        p(c);
      }
      void tok(BigDecimal c) {
        p(c);
      }
    };

    p(json);
    lex.lex(jsona, cb); 
    
    json  = "{\"a\":19560954.609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
    p(json);
    lex.lex(json.toCharArray(), cb);
  }

  static void p(Object o) {
    System.out.println(o);
  }

}
