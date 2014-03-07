/*
 * Class representing a Token.
 */

public class Token {
	protected String value;
	protected int kind;
	protected int line;
	
	/*
	 * Constructor. 
	 */
	public Token(String v, int k, int l) {
		value = v;
		kind = k;
		line = l;
	}
}
