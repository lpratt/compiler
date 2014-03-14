/*
 * TODO: allow for nested comments
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Scanner;

/*
 * Scanner class that has getNextToken() method
 */

public class Scan implements Constants {

	static Token next_token;
	static File file;
	static String line;
	static StringReader readline;
	static int linecount;
	static Scanner input;

	/*
	 * Constructor: takes in a filename
	 */
	public Scan(String filename) throws FileNotFoundException {
		file = new File(filename);
		input = new Scanner(file);
		next_token = new Token("",0,0);
		if (input.hasNextLine()) {
			line = input.nextLine();
			linecount = 1;
		} else {
			line = "";
			linecount = 0;
		}
	}

	/*
	 * nextToken accessor
	 */
	public Token getToken() {
		return next_token;
	}

	/*
	 * Takes in curpos, nextpos, t_#, and sets nextToken and line
	 * @param c, n, t
	 */
	private void setToken(int c, int n, int t) {
		String tokenValue = line.substring(c,n);
		if (t == T_ID) {
			if (KEYWORDS.containsKey(tokenValue)) {
				t = KEYWORDS.get(tokenValue);
			}
		}
		next_token = new Token(tokenValue, t, linecount);
		line = line.substring(n);
	}

	/*
	 * Logic for checking if next character is in ID (or keyword) token
	 * i = 1 when checking first character of token
	 * @param c, i
	 * @return boolean
	 */
	private boolean isID(char c, int i) {
		switch(i){
		case 1:
			return ((c >= 65 && c <= 90) || (c >= 97 && c <= 122));
		default:
			return ((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || (c >= 48 && c <= 57) || c == 95);
		}
	}

	/*
	 * Logic for checking if next character is in NUM token
	 * @param c
	 * @return boolean
	 */
	private boolean isNum(char c) {
		return (c >= 48 && c <= 57);
	}

	/*
	 * Finds the next token
	 * Reads through the source file until it finds the next token
	 * It then assigns a Token variable
	 */
	public void getNextToken() throws ScannerException {

		int curpos = 0;	// marks the start of the current token
		int nextpos = 0;	// looks ahead to see if next char is a part of curpos

		// Skip over whitespace
		while (curpos < line.length() && Character.isWhitespace(line.charAt(curpos))) {
			curpos++;
		}

		// if at the end of a line, read in next line
		if (curpos == line.length()) {
			if (input.hasNextLine()) {
				line = input.nextLine();
				linecount++;
				getNextToken();
				return;
			} else {
				next_token = new Token("EOF",T_EOF,linecount);	// TODO: edit string to ""
				line = "";
			}
		} else {
			if (line.charAt(curpos) == 47) {	// comment or / Token, doesn't supposed nested comments
				nextpos = curpos+1;
				try {
					if (line.charAt(nextpos) == 42) {	// comment, skip until reach end of line or end of comment
						int currlinecount = linecount;	// for unclosed comment error
						if (nextpos == line.length()-1) {	// deals with comment character by itself on a line TODO: single comment character on line by self (multiline)
							if (input.hasNextLine()) {
								line = input.nextLine();
								linecount++;
								nextpos = 0;
							} else {
								ScannerException se = new ScannerException("ERROR. Unclosed comment starting on line "+currlinecount);
								throw se;
							}	
						}
						while (nextpos < line.length()-1 || line.length() <= 1) {	// when it's not by itself
							nextpos++;
							currlinecount = linecount;
							if (line.length() <= 1) {
								if (input.hasNextLine()) {
									line = input.nextLine();
									linecount++;
									nextpos = 0;
								} else {
									ScannerException se = new ScannerException("ERROR. Unclosed comment starting on line "+currlinecount);
									throw se;
								}
							} else if (nextpos == line.length()-1) {
								if (line.charAt(nextpos-1) == 42 && line.charAt(nextpos) == 47) {
									line = line.substring(nextpos+1);
									getNextToken();
									return;
								} else {
									if (input.hasNextLine()) {
										line = input.nextLine();
										linecount++;
										nextpos = 0;
									} else {
										ScannerException se = new ScannerException("ERROR. Unclosed comment starting on line "+currlinecount);
										throw se;
									}
								}
							}
						}
						line = "";
						getNextToken();
						return;
					} else {
						setToken(curpos, nextpos, T_SLASH);
					}
				} catch (IndexOutOfBoundsException e) {	// / Token
					setToken(curpos, nextpos, T_SLASH);
				}
			} else if (isID(line.charAt(curpos),1)) {	// ID token
				nextpos = curpos+1;
				while (nextpos < line.length() && isID(line.charAt(nextpos),0)) {
					nextpos++;
				}
				setToken(curpos, nextpos, T_ID);
			} else if (isNum(line.charAt(curpos))) {	// Num token
				nextpos = curpos+1;
				while (nextpos < line.length() && isNum(line.charAt(nextpos))) {
					nextpos++;
				}
				setToken(curpos, nextpos, T_NUM);
			} else if (line.charAt(curpos) == 34) {	// Strlit token
				nextpos = curpos+1;
				while (nextpos < line.length() && line.charAt(nextpos) != 34) {
					nextpos++;
				}
				if (nextpos != line.length() && line.charAt(nextpos) == 34) {
					nextpos++;
				} else if (nextpos == line.length()) {
					ScannerException se = new ScannerException("ERROR. Unclosed string literal: line "+linecount);
					throw se;
				}
				setToken(curpos, nextpos, T_STRLIT);
			} else if (line.charAt(curpos) == 60) {	// <, <= Token
				nextpos = curpos+1;
				try {
					if (line.charAt(nextpos) == 61) {
						setToken(curpos, nextpos+1, T_LEQ);
					} else {
						setToken(curpos, nextpos, T_LESS);
					}
				} catch (Exception e) {
					setToken(curpos, nextpos, T_LESS);
				}
			} else if (line.charAt(curpos) == 62) {	// >, >= Token
				nextpos = curpos+1;
				try {
					if (line.charAt(nextpos) == 61) {
						setToken(curpos, nextpos+1, T_GEQ);
					} else {
						setToken(curpos, nextpos, T_GREATER);
					}
				} catch (Exception e) {
					setToken(curpos, nextpos, T_GREATER);
				}
			} else if (line.charAt(curpos) == 61) {	// =, == Token
				nextpos = curpos+1;
				try {
					if (line.charAt(nextpos) == 61) {
						setToken(curpos, nextpos+1, T_EQEQ);
					} else {
						setToken(curpos, nextpos, T_EQUAL);
					}
				} catch (Exception e) {
					setToken(curpos, nextpos, T_EQUAL);
				}
			} else if (line.charAt(curpos) == 33) {	// != Token
				nextpos = curpos+1;
				try {
					if (line.charAt(nextpos) == 61) {
						setToken(curpos, nextpos+1, T_NEQ);
					} else {
						ScannerException se = new ScannerException("ERROR. Unrecognized character: "+line.charAt(nextpos));
						throw se;
					}
				} catch (Exception e) {
					ScannerException se = new ScannerException("ERROR. Unrecognized character: "+line.charAt(nextpos-1));
					throw se;
				}
			} else {	// Tokens for single characters
				nextpos = curpos+1;
				switch (line.charAt(curpos)) {
				case 37: 
					setToken(curpos, nextpos, T_PERCENT);
					break;
				case 38: 
					setToken(curpos, nextpos, T_AMP);
					break;
				case 40:
					setToken(curpos, nextpos, T_LPAREN);
					break;
				case 41:
					setToken(curpos, nextpos, T_RPAREN);
					break;
				case 42: 
					setToken(curpos, nextpos, T_STAR);
					break;
				case 43: 
					setToken(curpos, nextpos, T_PLUS);
					break;
				case 44: 
					setToken(curpos, nextpos, T_COMMA);
					break;
				case 45: 
					setToken(curpos, nextpos, T_MINUS);
					break;
				case 59:
					setToken(curpos, nextpos, T_SEMICOLON);
					break;
				case 91:
					setToken(curpos, nextpos, T_LBRACKET);
					break;
				case 93:
					setToken(curpos, nextpos, T_RBRACKET);
					break;
				case 123:
					setToken(curpos, nextpos, T_LBRACE);
					break;
				case 125:
					setToken(curpos, nextpos, T_RBRACE);
					break;
				default:
					ScannerException se = new ScannerException("ERROR. Unrecognized character: "+line.charAt(curpos));
					throw se;
				}
			}
		}
	}
}

