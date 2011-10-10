package json;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class Encoder {
	
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
	}
	
	StringBuilder buf;
	List          circ;

	public Encoder () {
		this.buf  = new StringBuilder();
		this.circ = new java.util.LinkedList<Object>();
	}

	void encode (Object o) {
		if (null == o) { 
			buf.append("null");
			return;
		}
		if (o instanceof Map) {
			encode((Map)o);
		} else if (o instanceof List) {
			encode((List)o);
		} else if (o instanceof Number) {
			encode(buf, (Number)o);
		} else if (o instanceof CharSequence) {
			encode(buf, (CharSequence)o);
		} else if (o instanceof Character) {
			encode(buf, ((Character)o).charValue());
		} else if (o.getClass().isArray()) {
			encodeArray(o);
		}else {
			p(o.getClass().getName());
			eggsplod(o.getClass());
		}
	}
	void eggsplod(Object o) {throw new RuntimeException(o.toString());}

	void encode (Map m) {
		checkCircular(m);
		buf.append('{');
		for (Object k : m.keySet()) {
			Object v = m.get(k);
			encode(buf, k.toString());
			buf.append(':');
			encode(v);
			buf.append(",");
		}
		buf.setCharAt(buf.length()-1, '}');
	}

	void encode (List l) {
		checkCircular(l);
		buf.append('[');
		for (Object k : l) {
			encode(k);
			buf.append(",");
		}
		buf.setCharAt(buf.length()-1, ']');
	}

	void encodeArray (Object arr) {
		checkCircular(arr);
		assert arr.getClass().isArray();

		buf.append('[');
		Object o = null;
		for (int i=0; ;++i) {
			try {
				o = java.lang.reflect.Array.get(arr, i);
				encode(o);
				buf.append(",");
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				break;
			}
		}
		buf.setCharAt(buf.length()-1, ']');
	}

	void checkCircular(Object m) {
		if (circ.contains(m)) {
			eggsplod("circular");
		} else {
			circ.add(m);
		}
	}

	static void encode (StringBuilder buf, CharSequence s) {
		char c = 0;
		buf.append('"');
		for (int i =0; i!=s.length(); ++i) {
			c = s.charAt(i);
			if (Character.isISOControl(c)) {
				continue; // really!? just skip?
			}
			switch(c) {
				case '"':
				case '\\':
				case '\b':
				case '\f':
				case '\n':
				case '\r':
				case '\t':
					buf.append('\\');
					buf.append(c);
					continue;
				default:
					buf.append(c);
			}
		}
		buf.append('"');
	}
	static void encode (StringBuilder buf, Number n) {
		buf.append(n.toString());
	}
	static void encode (StringBuilder buf, boolean b) {
		if (b) {buf.append("true");}
		else   {buf.append("false");}
	}
	static void encode (StringBuilder buf, int i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, long i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, float i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, double i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, byte i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, short i) {
		buf.append(i);
	}
	static void encode (StringBuilder buf, char c) {
		buf.append((int)c);
	}

	static void p (Object o) {
		System.out.println(o);
	}

	public static void main (String [] args) {
		StringBuilder b = new StringBuilder();
		encode(b, true);
		encode(b, 1);
		encode(b, (long)1);
		encode(b, (float)1.0);
		encode(b, (double)1.0);
		encode(b, (byte)1);
		encode(b, (short)1);
		encode(b, '1');
	
		p(b);

		String	json	= "{\"a\":19560954609845.4456456,\"b\":[1,2,3],\"dindong\":{\"b\":12}}";
		Map m = (Map)JSON.parseJSON(json);
		p(json);		
		p(jsonify(m));
		
		int [] is = {1,2,3};
		p(jsonify(is));
		
		Map map = new java.util.HashMap();
		map.put("bla", map);
		p(jsonify(map));
	}
}
