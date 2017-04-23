package core;
import java.util.Random;

public class RandomString {

  private static final char[] symbols;

  static {
    StringBuilder tmp = new StringBuilder();
    for (char ch = '0'; ch <= '9'; ++ch)
      tmp.append(ch);
    for (char ch = 'a'; ch <= 'z'; ++ch)
      tmp.append(ch);
    symbols = tmp.toString().toCharArray();
  }   

  private final Random random = new Random();

  private final char[] buf;

  /**
   * Generate a specific length random string
   * @param length
   */
  public RandomString(int length) {
    if (length < 1)
      throw new IllegalArgumentException("length < 1: " + length);
    buf = new char[length];
  }

  public String nextString() {
    for (int idx = 0; idx < buf.length; ++idx) 
      buf[idx] = symbols[random.nextInt(symbols.length)];
    return new String(buf);
  }
  
 
  public String[] generateKeyPairName(){
	String name = nextString();
	String pubKeyName = name + "_public.key";
	String privKeyName = name + "_private.key";
	String [] keyPairName = {pubKeyName, privKeyName};
	return keyPairName;
  }
}