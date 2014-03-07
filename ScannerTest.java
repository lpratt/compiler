/*
 * Tests the scanner and prints out the tokens
 */

public class ScannerTest implements Constants {

	public static void main(String[] args) {
	    try {
		String input_filename = args[0];
		Scan my_scanner = new Scan(input_filename);

		while (my_scanner.getToken().kind != T_EOF) {
			try {
				my_scanner.getNextToken();
				System.out.println("Token "+my_scanner.getToken().kind+", string "+my_scanner.getToken().value+", line number "+my_scanner.getToken().line);
			} catch (Exception e) {
				System.out.println (e);
				System.exit(1);
			}
		}
	    } catch (Exception e) {
		System.out.println("Need to input a filename.");
		System.exit(1);
	    }
	}

}