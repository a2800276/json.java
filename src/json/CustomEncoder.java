package json;
import java.util.Map;
import java.util.HashMap;
public class CustomEncoder extends Encoder {
  
  public interface Encoder<T> {
    public void encode (StringBuilder buf, Object r);
  }
  
  private Map<Class<?>,CustomEncoder.Encoder<?>> encoders;
    

  public CustomEncoder() {
    super();
    this.encoders = new HashMap<Class<?>, CustomEncoder.Encoder<?>>();
  } 

  public void addEncoder (Class c, CustomEncoder.Encoder<?> encoder) {
    this.encoders.put(c, encoder);
  }
  
  boolean canEncode(Object o) {
    return this.encoders.containsKey(o.getClass());
  }
  void encodeCustom(Object o) {
    Encoder<?> encoder = this.encoders.get(o.getClass());
    encoder.encode(this.buf, o);
  }
}
