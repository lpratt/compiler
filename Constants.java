import java.util.HashMap;

public interface Constants {
	
	// TREENODE CONSTANTS
	static final int PROGRAM = 1;
	static final int COMPOUND_STATEMENT = 10;
	static final int EXPRESSION_STATEMENT = 14;
	static final int IF_STATEMENT = 15;
	static final int WHILE_STATEMENT = 16;
	static final int RETURN_STATEMENT = 17;
	static final int WRITE_STATEMENT = 18;
	static final int WRITELN_STATEMENT = 188;
	static final int EXPRESSION = 19;
	static final int VAR = 20;
	static final int COMP_EXPRESSION = 50;
	static final int ET_EXPRESSION = 51;
	static final int OP = 75;
	static final int FUN_CALL = 76;
	static final int ARGS = 77;
	static final int PARAM = 78;
	static final int PARAM_LIST = 79;
	static final int EXPRESSION_INT = 100;
	static final int EXPRESSION_VAR = 101;
	static final int EXPRESSION_STRING = 102;
	static final int EXPRESSION_RELOP = 103;
	static final int EXPRESSION_FACTOR = 104;
	static final int VAR_DEC = 200;
	static final int FUN_DEC = 201;
	static final int IGNORE = 500;
	
	// TOKEN CONSTANTS
	static final int T_ID = 1;	// identifiers - start with a letter and consist of letters, digits, and underscores
	static final int T_NUM = 2;	// numbers - non-negative integers
	static final int T_INT = 3;	// int
	static final int T_VOID = 4;	// void
	static final int T_STRING = 5;	// string
	static final int T_IF = 6;	// if
	static final int T_ELSE = 7;	// else
	static final int T_WHILE = 8;	// while
	static final int T_RETURN = 9;	// return
	static final int T_WRITE = 10;	// write
	static final int T_WRITELN = 11;	// writeln
	static final int T_READ = 12;	// read
	static final int T_SEMICOLON = 13;	// ;
	static final int T_COMMA = 14;	// ,
	static final int T_LBRACKET = 15;	// [
	static final int T_RBRACKET = 16;	// ]
	static final int T_LBRACE = 17;	// {
	static final int T_RBRACE = 18;	// }
	static final int T_LPAREN = 19;	// (
	static final int T_RPAREN = 20;	// )
	static final int T_LESS = 21;	// <
	static final int T_LEQ = 22;	// <=
	static final int T_EQUAL = 23;	// =
	static final int T_EQEQ = 24;	// ==
	static final int T_NEQ = 25;	// !=
	static final int T_GEQ = 26;	// >=
	static final int T_GREATER = 27;	// >
	static final int T_PLUS = 28;	// +
	static final int T_MINUS = 29;	// -
	static final int T_STAR = 30;	// *
	static final int T_SLASH = 31;	// /
	static final int T_PERCENT = 32;	// %
	static final int T_AMP = 33;	// &
	static final int T_STRLIT = 34;	// "string literal"
	static final int T_EOF = 35;	//

	static final HashMap<String, Integer> KEYWORDS = new HashMap<String,Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("int",T_INT);
			put("void",T_VOID);
			put("string",T_STRING);
			put("if",T_IF);
			put("else",T_ELSE);
			put("while",T_WHILE);
			put("return",T_RETURN);
			put("write",T_WRITE);
			put("writeln",T_WRITELN);
			put("read",T_READ);
		}};
}
