package json;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.LinkedList;

public class JSON {
	
	static class LexerCB extends Lexer.CB {
		Stack<Object> stack = new Stack<Object>();
		boolean done;
		

		void tok(Lexer.Token t) {

			if (done) {error();}

			switch (t) {
				case LCURLY:
					stack.push(map());
					break;
				case LSQUARE:
					stack.push(list());
					break;
				case RCURLY:
					assert stack.peek() instanceof Map;
					Map m = (Map)stack.pop();
					stash(m);
					break;
				case RSQUARE:
					assert stack.peek() instanceof List;
					List l = (List)stack.pop();
					stash(l);
					break;
				case TRUE:
					stash(true);
					break;
				case FALSE:
					stash(false);
					break;
				case NULL:
					stash(null);
					break;
				case COMMA:
					assert stack.peek() instanceof List;
					break;
				case COLON:
					assert stack.peek() instanceof Key;
					break;
				default:
					error();
			}
		}
		void tok(String s) {
			if (done) {error();}
			if (stack.peek() instanceof Map) {
				stack.push(new Key(s));
			} else {
				stash(s);
			}
		}
		void tok(BigDecimal d) {
			stash(d);
		}
		void stash (Object o) {
			// stack is empty, done
			if (0 == stack.size()) {
				done = true;
				stack.push(o);
				return;
			}	
			Object top = stack.peek();
			if (top instanceof List) {
				((List<Object>)top).add(o);
			} else if (top instanceof Key) {
				String key = ((Key)stack.pop()).s;
				assert stack.size() > 0;
				assert stack.peek() instanceof Map;
				((Map<String, Object>)stack.peek()).put(key,o);
			} else {
				error();
			}
		}
		Map  map()  {return new HashMap();}
		List list() {return new LinkedList();}
		void error() {throw new RuntimeException();}
	}

	static class Key {
		Key (String s) {this.s = s;}
		String s;
	}
	
	static Object parseJSON (String json) {
		char [] arr = json.toCharArray();
		Lexer lex   = new Lexer();
		LexerCB cb = new LexerCB();
		
		lex.lex(arr, cb);
		return cb.stack.pop();
	}

	LexerCB cb;
	Object  obj;

	public JSON () {
	this.cb = new LexerCB();
	}
	public void parseJSON(char [] arr) {
		Lexer.lexer.lex(arr, this.cb);
	}
	public boolean done() {
		return this.cb.done;
	}
	public Object obj () {
		if (!done()) {
			throw new RuntimeException("not done!");
		}
		if (null == this.obj) {
			this.obj = this.cb.stack.pop();
		}
		return this.obj;
	}
	

	public static void main (String [] args) {
		if (0 < args.length) {}

    String json  = "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
    
		Object o = JSON.parseJSON(json);
		p(json);
		p(o);
		json  = "{\"a\" 19560954609845.4456456 \"b\" [1 2 3] \"dindong\" {\"b\" 12}}";
		p(json);
		o = JSON.parseJSON(json);
		p(o);
	}
	static void p (Object o) {
		System.out.println(o);
	}

}
