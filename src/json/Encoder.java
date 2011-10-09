package json;
import java.util.Map;
import java.util.List;
public class Encoder {

	public static String encode (Object o) {
		StringBuilder b = new StringBuilder();
		encode(b, o);
		return b.toString();
	}
	private static void encode (StringBuilder buf, Object o) {
		if (o instanceof Map) {
			encode(buf, (Map)o);
		} else if (o instanceof List) {
			encode(buf, (List)o);
		} else if (o instanceof Number) {
			encode(buf, (Number)o);
		} else if (o instanceof CharSequence) {
			encode(buf, (CharSequence)o);
		} else {
			eggplod(o);
		}
	}
	static void eggplod(Object o) {throw new RuntimeException(o.toString());}
	static void encode (StringBuilder buf, Map m) {
		buf.append('{');
		for (Object k : m.keySet()) {
			Object v = m.get(k);
			encode(buf, k.toString());
			buf.append(':');
			encode(buf, v);
			buf.append(",");
		}
		buf.setCharAt(buf.length()-1, '}');
	}

	static void encode (StringBuilder buf, List l) {
		buf.append('[');
		for (Object k : l) {
			encode(buf, k);
			buf.append(",");
		}
		buf.setCharAt(buf.length()-1, ']');
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
		p(encode(m));
	}
}
