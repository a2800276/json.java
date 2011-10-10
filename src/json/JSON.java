package json;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.LinkedList;

/** Simple JSON parsing for Java.  This class provides a rudimentary
  * callback implementation that may be passed to @see json.Lexer . In
  * case you are interested in writing your own specialized callback to
  * use with json.Lexer the callback contained herein may be a good
  * starting point.
  *
  * JSON objects (`{"bla":1}`) are converted to `java.utils.Map's` (Maps
  * in the interface HashMaps for the implementation), JSON arrrays are
  * converted to `java.util.List's` (LinkedList for the implementation),
  * JSON Strings become Java Strings, Numbers become `BigDecimal`, `true`
  * and `false` are boolean and null is, well, null.
  *
  * <h2> Usage </h2> <code>
  *
  * <code>
  *   String json = "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
  *   Object    o = JSON.parseJSON(json);
  * </code> 
  *
  * In the example above, `o` will be a `java.util.Map` containing three
  * keys, "a", "b" and "dingdong", each Strings. "a"'s value is a
  * BigDecimal, "b"'s an array containing the BigDecimal values 1, 2,
  * and 3 and "dingdong"'s value is another Map ...
  *
  * The intended use case for this is to write non-blocking webservices,
  * this interface is meant to provide the functionality to process any
  * scrap of data that happens to be available on the network. This
  * requires a slightly more elaborate interface than the simple verson
  * above:
  * 
  *   <code>
  *  		JSON json = new JSON();
  *  		while (arr = bytesAvailableFromSomewhere()) {
  *  			j.parse(arr);
  *  			if (json.done()) break;
  *  		}
  *  		Object result = json.obj();
  *   </code>
  *
  * <h2> Accepted JSON </h2>
  *
  * This implementation should be able to handle any JSON conforming to
  * JSON as described here (http://json.org).
  *
  * Technically, this parser accepts a superset of JSON that allows
  * redundant ':' and ',' inside of JSON objects and Arrays to be left
  * out, so it would accept:
  *
  *   <code>
  *     { "a" 19560954609845.4456456 "b" [1 2 3] "dindong" {"b" 12}}
  *   </code>
  *
  * as the equivalent of:
  *
  *   <code>
  *     { "a" : 19560954609845.4456456, "b" : [1 2 3], "dindong" : {"b" 12}}
  *   </code>
  *
  */
public class JSON {
	
	/**
	 * Utility method to parse a String containing valid JSON
	 */	
	public static Object parse (String json) {
		LexerCB cb = new LexerCB();
		Lexer.lexer.lex(json.toCharArray(), cb);
		return cb.stack.pop();
	}

	/**
	 * Mungle up an Object into JSON. There are a bunch of
	 * cases this can't handle, for example: just any old stuff.
	 *
	 * Object passed to this method needs to be:
	 * <ul>
	 * <li> primitive
	 * <li> java.util.Map
	 * <li> java.util.List
	 * <li> an Array of one of the above
	 * </ul>
	 */
	public static String jsonify (Object o) {
		Encoder e = new Encoder();
		        e.encode(o);
		return e.buf.toString();
		
		// Of course, there would be a number of ways to encode just any old
		// stuff. Easiest would be calling `toString` on unknown classes and
		// encoding their String representation. OR treat them as an
		// data-containers and encode all their public fields. OR treat them
		// as Beans(tm) [yuckyuckyuck].
		//
		// The best way to go would be some sort of `unknown class Handler`
		// to stuff into the encoder to roll your own strategy for dealing
		// with this...
 		//
		// I have yet to decide an will do so when I need to.
	}

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
				error("unexpected: "+o.getClass().getName()+" after: "+(stack.peek().getClass().getName()));
			}
		}
		Map  map()   {return new HashMap();}
		List list()  {return new LinkedList();}

		void error()         {error("?");}
		void error(String m) {throw new RuntimeException(m);}
	}

	static class Key {
		// Internal Marker class to keep track of keys and values on the
		// stack of the parser. A `Key` object may only be placed on top of
		// a `Map` (JSON Object). Encountering a `COLON` should only happen
		// when there is a `Key` on top of the stack, etc.
		Key (String s) {this.s = s;}
		String s;
	}


	LexerCB cb = new LexerCB();
	Object  obj; // result.

	/**
	 * Parse whatever bits of JSON you have available to you.
	 */
	public void parse(char [] arr) {
		Lexer.lexer.lex(arr, this.cb);
	}

	/**
	 * Returns whether the parser is in a consistant, balanced state.
	 * Once the parser is `done` passing further data to it via `parse`
	 * will trigger an Exception.
	 */
	public boolean done() {
		return this.cb.done;
	}

	/** 
	 * Retrieve the results of the parse. You need to ensure that the 
	 * complete JSON object has been passed to parse, else this will throw
	 * and Exception. Ideally, call `done()` before trying to retrieve the
	 * results
	 */
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
    
		Object o = JSON.parse(json);
		p(json);
		p(o);


		json  = "{{\"a\":19560954609845.4456456}:1,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		p(json);
		try {
			o = JSON.parse(json);
			p("failed: JSON-Obj as key should cause exception");
		} catch (Throwable t) {
			p("failed as expected: \n\t"+t.getMessage());
		}


		json  = "{\"a\" 19560954609845.4456456 \"b\" [1 2 3] \"dindong\" {\"b\" 12}}";
		p(json);
		o = JSON.parse(json);
		p(o);

		
		JSON j = new JSON();
		char [] a = json.toCharArray();
		char [] b = new char[1];
		for (int i = 0; /*i!=a.length*/; ++i) {
			System.arraycopy(a,i,b,0,1);
			j.parse(b);
			if (j.done()) break;
		}
		p(j.obj());
	}

	static void p (Object o) {
		System.out.println(o);
	}

}
