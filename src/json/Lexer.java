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
	}
	
	static abstract class CB {
		State state = State.VALUE;
		StringBuffer cache; 
		StringBuffer hexCache; 
		abstract void tok(Token tok);
		abstract void tok(String s);
		abstract void tok(BigDecimal s);
	}
	
	// You shouldn't ever need more than a single instance of Lexer, so here's a
	// prepared instance to avoid having to create garbage. 
	//
	// I'm not enforcing the use of a single instance because:
	//
	// * who am I to judge whether you need more instances?
	// * the way java handles static is fairly retarded for non-trivial cases
	// * I think singletons are overrated.

	public static Lexer lexer = new Lexer();

	void lex (byte [] arr, CB cb) {
		for (int i = 0; i != arr.length; ++i) {
			byte c = arr[i];
			switch (cb.state) {
				case VALUE:
					if (isWS(c)) {
						continue;
					} 
					switch (c) {
						// String
						case '"':
							cb.state = State.STRING_START;
							cb.cache = new StringBuffer();
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
							cb.state = State.NUMBER_START;
							cb.cache = new StringBuffer();
							cb.cache.append(c); 
							continue;

						// Object
						case '{':
							cb.state = State.VALUE;
							cb.tok(Token.LCURLY);
							continue;

						// Array
						case '[':
							cb.state = State.VALUE;
							cb.tok(Token.LSQUARE);
							continue;

						// true
						case 't':
							cb.state = State.T;
							continue;

						// false
						case 'f':
							cb.state = State.F;
							continue;

						// null
						case 'n':
							cb.state = State.N;
							continue;

						default:
							error(cb.state);
					}

				case T:
					if ('r' == c) {
						cb.state = State.TR;
						continue;
					}
					error(cb.state, c);
				case TR:
					if ('u' == c) {
						cb.state = State.TRU;
						continue;
					}
					error(cb.state, c);
				case TRU:
					if ('e' == c) {
						cb.tok(Token.TRUE);
						cb.state = State.AFTER_VALUE;
						continue;
					}
					error(cb.state, c);

				case F:
					if ('a' == c) {
						cb.state = State.FA;
						continue;
					}
					error(cb.state, c);
				case FA:
					if ('l' == c) {
						cb.state = State.FAL;
						continue;
					}
					error(cb.state, c);
				case FAL:
					if ('s' == c) {
						cb.state = State.FALS;
						continue;
					}
					error(cb.state, c);
				case FALS:
					if ('e' == c) {
						cb.tok(Token.FALSE);
						cb.state = State.AFTER_VALUE;
						continue;
					}
					error(cb.state, c);

				case N:
					if ('u' == c) {
						cb.state = State.NU;
						continue;
					}
					error(cb.state, c);
				case NU:
					if ('l' == c) {
						cb.state = State.NUL;
						continue;
					}
					error(cb.state, c);
				case NUL:
					if ('l' == c) {
						cb.tok(Token.NULL);
						cb.state = State.AFTER_VALUE;
						continue;
					}
					error(cb.state, c);
					
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
							cb.state = State.VALUE;
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
							cb.cache.append(c);
							continue;
						default:
							cb.tok(num(cb.cache));
							--i;
							cb.state = State.AFTER_VALUE;
							continue;
					} 

				case STRING_START:
					switch (c) {
						case '"':
							cb.tok(cb.cache.toString());
							cb.state = State.AFTER_VALUE;
							continue;
						case '\\':
							cb.state = State.STR_ESC;
							continue;
						default:
							if (Character.isISOControl(c)) {
								error(cb.state, c);
							}
							cb.cache.append(c);
							continue;
					}

				case STR_ESC:
					switch (c) {
						case '"':
						case '/':
						case '\\':
							cb.cache.append(c);
							break;
						case 'b':
							cb.cache.append('\b');
							break;
						case 'f':
							cb.cache.append('\f');
							break;
						case 'n':
							cb.cache.append('\r');
							break;
						case 'r':
							cb.cache.append('\r');
							break;
						case 't':
							cb.cache.append('\t');
							break;
						case 'u':
							cb.state = State.HEX1;
							continue;
						default:
							error(cb.state, c);
					}
					cb.state = State.STRING_START;
					continue;

				case HEX1:
					if (!isHex(c)) {error(cb.state, c);}
					cb.hexCache = new StringBuffer();
					cb.hexCache.append(c);
					cb.state = State.HEX2;
					continue;
				case HEX2:
					if (!isHex(c)) {error(cb.state, c);}
					cb.hexCache.append(c);
					cb.state = State.HEX3;
					continue;
				case HEX3:
					if (!isHex(c)) {error(cb.state,c);}
					cb.hexCache.append(c);
					cb.state = State.HEX4;
					continue;
				case HEX4:
					if (!isHex(c)) {error(cb.state, c);}
					cb.hexCache.append(c);
					char u = toChar(cb.hexCache);
					cb.cache.append(u);
					cb.state = State.STRING_START;
					continue;

				default:
				 error(cb.state, c);
			} // state switch 
		}// for
	}
	
	boolean isWS(byte c) {
		return Character.isWhitespace(c);
	}
	
	char toChar (StringBuffer buf) {
		assert buf.length() == 4;
		return (char)Integer.parseInt(buf.toString(), 16);
	}
	
	BigDecimal num (StringBuffer b) {
		BigDecimal bd = null;
		try {
			bd =	new BigDecimal(b.toString());
		} catch (Throwable t) {
			error("not a number: "+b.toString());
		}
		return bd;
	}

	
	boolean isHex(byte c) {
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
	
	void error (State state) {
		error("??? "+ state);
	}

	void error (State state, byte c) {
		error("unexpected char: "+(char)c+" in state: "+state);
	}

	void error(String mes) {
		throw new RuntimeException(mes);
	}

	public static void main (String [] args) {
		String	json	= "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		byte [] jsona = json.getBytes();
	
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
		Lexer.lexer.lex(jsona, cb); 
		
		json	= "{\"a\":\"\\u2603\",\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		p(json);
		Lexer.lexer.lex(json.getBytes(), cb);

		json	= "{\"a\":19560954.609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		p(json);
		try {
			Lexer.lexer.lex(json.getBytes(), cb);
			p("failed: should not be here! 19560954.609845.4456456 is not a number");
		} catch (RuntimeException re) {
			p("expected failure: \n\t"+re.getMessage());
		}
	}

	static void p(Object o) {
		System.out.println(o);
	}

}
