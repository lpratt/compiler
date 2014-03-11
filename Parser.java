
public class Parser implements Constants {

	Scan my_scanner;
	Token current_token;
	boolean debug = false;

	/*
	 * A constructor
	 */
	public Parser(String filename){
		try {
			my_scanner = new Scan(filename);
		} catch (Exception e) {
		}
	}

	/*
	 * Sets current_token
	 */
	public void getNextToken() {
		try {
			my_scanner.getNextToken();
			current_token = my_scanner.getToken();
		} catch (ScannerException e) {
			System.out.println("ERROR GETTING NEXT TOKEN.");
			System.exit(1);
		}
	}

	/*
	 * A function that takes in a token, an expected string, and an error message
	 * Returns a boolean if the token value and expected string are the same
	 * If they're different, throws a ParserException with the error message.
	 */
	public boolean expect(Token t, int k, String message) throws ParserException {
		if (t.kind == k) {
			return true;
		} else {
			ParserException pe = new ParserException(message);
			throw pe;
		}
	}

	/*
	 * Highest level (entry) into parse
	 * Program -> Statement
	 */
	public TreeNode parse() throws ParserException {
		getNextToken();	// gets the first token of the program
		if (debug)
			System.out.println("Parse: "+current_token.value);
		TreeNode t = new TreeNode(current_token, PROGRAM);
		if (current_token.kind == T_EOF) {
			return t;
		} else {
			TreeNode s = parseStatement(); // TODO: change to Declaration_List
			t.next_nodes.add(s);
			return t;
		}
	}

	/*
	 * Statement -> Expression_Statement | 
	 * 				Compound_Statement | 
	 * 				While_Statement | 
	 * 				If_Statement |
	 * 				Return_Statement |
	 * 				Write_Statement
	 */
	public TreeNode parseStatement() throws ParserException {
		TreeNode t = null;
		if (current_token.kind == T_LBRACE) {	// Compound_Statement
			getNextToken(); 	// advance token to read in start of exp_stmt
			if (debug) 
				System.out.println("ParseStatement{: "+current_token.value);
			t = parseCompoundStatement();
			if (current_token.kind == T_RBRACE) {
				return t;
			} else {
				ParserException pe = new ParserException("Not a valid CompoundStatement: Missing '}'");
				throw pe;
			}
		} else if (current_token.kind == T_WHILE) { 	// While_Statement
			t = parseWhileStatement();
		} else if (current_token.kind == T_IF) { 	// If_Statement
			t = parseIfStatement();
		} else if (current_token.kind == T_RETURN) { 	// Return_Statement
			t = parseReturnStatement();
		} else if (current_token.kind == T_WRITE || current_token.kind == T_WRITELN) { 	// Write_Statement
			t = parseWriteStatement();
		} else {	// Expression_Statement
			t = parseExpressionStatement();
		}
		return t;
	}

	/*
	 * Compound_Statement -> {Expression_Statment(s)}
	 */
	public TreeNode parseCompoundStatement() throws ParserException {
		TreeNodeStatement t = new TreeNodeStatement(current_token, COMPOUND_STATEMENT, null);
		while (current_token.kind != T_RBRACE && current_token.kind != T_EOF) {
			TreeNode e = parseExpressionStatement();
			t.next_nodes.add(e);
			getNextToken();
			if (debug)
				System.out.println("Compound Statement: "+current_token.value);
		}
		return t;
	}

	/*
	 * If_Statement -> IF (Expression) Statement | IF (Expression) Statement ELSE Statement
	 */
	public TreeNode parseIfStatement() throws ParserException {
		Token save_token = current_token;
		getNextToken(); // should be (
		if (debug)
			System.out.println("If (: "+current_token.value);

		if (expect(current_token, T_LPAREN, "Not a valid IfStatement: Missing '('")) {
			getNextToken(); 	// advance token to read in start of exp
			if (debug)
				System.out.println("If exp: "+current_token.value);

			TreeNode e = parseExpression();
			getNextToken(); 	// should be )
			if (expect(current_token, T_RPAREN, "Not a valid IfStatement: Missing ')'")) {
				getNextToken(); 	// advance token to read in start of statement
				if (debug)
					System.out.println("If statement: "+current_token.value);

				TreeNode s = parseStatement();
				getNextToken(); 	// check if it's ELSE
				if (debug)
					System.out.println("If else: "+current_token.value);

				if (current_token.kind == T_ELSE) {
					getNextToken(); 	// advance token to read in start of statement
					if (debug)
						System.out.println("If else statement: "+current_token.value);
					TreeNode es = parseStatement();
					return new TreeNodeCondStatement(save_token, IF_STATEMENT, e, s, es);
				} else { 	// return 1st rule
					return new TreeNodeCondStatement(save_token, IF_STATEMENT, e, s, null);
				}
			}
		}
		return null;
	}


	/*
	 * While_Statement -> WHILE (Expression) Statement
	 */
	public TreeNode parseWhileStatement() throws ParserException {
		Token save_token = current_token;
		getNextToken(); 	// should be (
		if (debug)
			System.out.println("While (: "+current_token.value);

		if (expect(current_token, T_LPAREN, "Not a valid WhileStatement: Missing '('")) {
			getNextToken(); 	// advance token to read in start of exp
			if (debug)
				System.out.println("While exp: "+current_token.value);

			TreeNode e = parseExpression();
			getNextToken(); 	// should be )
			if (debug)
				System.out.println("While ): "+current_token.value);

			if (expect(current_token, T_RPAREN, "Not a valid WhileStatement: Missing ')'")) {
				getNextToken(); 	// advance token to read in start of statement
				if (debug)
					System.out.println("While statement: "+current_token.value);

				TreeNode s = parseStatement();
				return new TreeNodeCondStatement(save_token, WHILE_STATEMENT, e, s, null);
			}
		}
		return null;
	}

	/*
	 * Return_Statement -> RETURN; | RETURN Expression;
	 */
	public TreeNode parseReturnStatement() throws ParserException {
		TreeNode t = new TreeNode(current_token, RETURN_STATEMENT);
		getNextToken(); 	// should be ; or expression
		if (debug)
			System.out.println("Return: "+current_token.value);

		if (current_token.kind == T_SEMICOLON) {
			return t;
		} else {
			TreeNode e = parseExpression();
			t.next_nodes.add(e);
			return t;
		}
	}

	/*
	 * Write_Statement -> WRITE (Expression); | WRITELN();
	 */
	public TreeNode parseWriteStatement() throws ParserException {
		Token save_token = current_token;
		if (current_token.kind == T_WRITELN) {
			getNextToken(); 	// should be (
			if (debug)
				System.out.println("Writeln (: "+current_token.value);

			if (expect(current_token, T_LPAREN, "Not a valid WriteStatement: Missing ( after writeln")) {
				getNextToken(); 	// should be )
				if (debug)
					System.out.println("Writeln ): "+current_token.value);

				if (expect(current_token, T_RPAREN, "Not a valid WriteStatement: Missing ) after writeln")) {
					getNextToken(); 	// should be ;
					if (debug)
						System.out.println("Writeln ;: "+current_token.value);

					if (expect(current_token, T_SEMICOLON, "Not a valid WriteStatement: Missing ; after writeln()")) {
						return new TreeNodeStatement(save_token, WRITE_STATEMENT, null);
					}
				}
			}
		} else { 	// write statement
			getNextToken(); 	// should be (
			if (debug)
				System.out.println("Write (: "+current_token.value);

			if (expect(current_token, T_LPAREN, "Not a valid WriteStatement: Missing (")) {
				getNextToken(); 	// advance token to read in start of exp
				if (debug)
					System.out.println("Write exp: "+current_token.value);

				TreeNode e = parseExpression();
				getNextToken(); 	// should be )
				if (debug)
					System.out.println("Write ): "+current_token.value);

				if (expect(current_token, T_RPAREN, "Not a valid WriteStatement: Missing )")) {
					getNextToken(); 	// should be ;
					if (debug)
						System.out.println("Write ;: "+current_token.value);

					if (expect(current_token, T_SEMICOLON, "Not a valid WriteStatement: Missing ;")) {
						return new TreeNodeStatement(save_token, WRITE_STATEMENT, e);
					}
				}
			}
		}
		return null;
	}


	/*
	 * Expression_Statement -> Expression; | ;
	 */
	public TreeNode parseExpressionStatement() throws ParserException {
		if (current_token.kind == T_SEMICOLON) {
			return new TreeNodeStatement(current_token, EXPRESSION_STATEMENT, null);
		} else {
			TreeNode t = parseExpression();
			Token save_token = current_token;
			getNextToken();
			if (debug)
				System.out.println("Expression Statement: "+current_token.value);

			if (expect(current_token, T_SEMICOLON, "Not a valid ExpressionStatement: Missing ';'")) {
				return new TreeNodeStatement(save_token, EXPRESSION_STATEMENT, t);
			}
		}
		return null;
	}

	/*
	 * TODO: Expression -> Var = Expression
	 * Currently: Expression -> Var = Expression | <num>
	 */
	public TreeNode parseExpression() throws ParserException {
		TreeNodeExpression t = new TreeNodeExpression(current_token, EXPRESSION, null);
		Token save_token = current_token;
		if (current_token.kind == T_ID || current_token.kind == T_STAR) { 	// Var starts with <id>, *
			TreeNode v = parseVar();
			t.var = v;
			if (save_token.kind == T_STAR) {
				getNextToken(); 	// should be =; only do for *
				if (debug)
					System.out.println("Expression =: "+current_token.value);
			}

			if (expect(current_token, T_EQUAL, "Not a valid Expression: Missing '='")) {
				getNextToken(); 	// advance to start expression
				if (debug)
					System.out.println("Expression exp: "+current_token.value);
				TreeNode e = parseExpression();
				t.expression = e;
				return t;
			}
		} else if (current_token.kind == T_NUM) {
			return new TreeNode(current_token, EXPRESSION_INT);
		}
		else {
			ParserException pe = new ParserException("Not a valid Expression: not an id or num.");
			throw pe;
		}
		return null;
	}

	/*
	 * Var -> <id> | <id>[Expression] | *<id>
	 */
	public TreeNode parseVar() throws ParserException {
		TreeNodeExpression t = new TreeNodeExpression(current_token, EXPRESSION_VAR, null);
		if (current_token.kind == T_ID) {
			getNextToken(); 	// check to see if [
			if (debug)
				System.out.println("Var [: "+current_token.value);
			
			if (current_token.kind == T_LBRACKET) {
				getNextToken(); 	// advance to start expression
				if (debug)
					System.out.println("Var exp: "+current_token.value);
				
				TreeNode e = parseExpression();
				getNextToken(); 	// should be ]
				if (debug)
					System.out.println("Var ]: "+current_token.value);

				if (expect(current_token, T_RBRACKET, "Not a valid var: Missing ']'")) {
					getNextToken(); 	// to stay consistent, allows for returning to expression on correct token
					if (debug)
						System.out.println("Var exp2: "+current_token.value);

					t.expression = e;
					return t;
				}
			} else {
				return t;
			}
		} else { 	// *
			getNextToken(); 	// should be <id>
			if (debug)
				System.out.println("Var id: "+current_token.value);

			if (expect(current_token, T_ID, "Not a valid var: not an <id> after *")) {
				t.value = "*"+current_token.value;
				return t;
			}
		}
		return null;
	}

	/*
	 * Preorder traversal of tree
	 */
	public static void printTree(TreeNode t, String spaces) {
		spaces += " ";
		if (t != null) {
			switch(t.node_type) {
			case 1:
				System.out.println(spaces+"Node type "+t.node_type+" (program) at line "+t.line+" goes to {");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+"}");
				break;
			case 10:
				System.out.println(spaces+"Node type "+t.node_type+" (compound statement) at line "+t.line+" consists of {");
				TreeNodeStatement s10 = (TreeNodeStatement) t;
				for (int i = 0; i < s10.next_nodes.size(); i++) {
					printTree(s10.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+"}");
				break;
			case 14: 
				System.out.println(spaces+"Node type "+t.node_type+" (expression statement) at line "+t.line+" goes to {");
				TreeNodeStatement s = (TreeNodeStatement) t;
				printTree(s.expression, spaces);
				System.out.println(spaces+"}");
				break;
			case 15:
				TreeNodeCondStatement ifcs = (TreeNodeCondStatement) t;
				System.out.println(spaces+"Node type "+t.node_type+" (if statement) at line "+t.line+" has condition (");
				printTree(ifcs.expression, spaces);
				System.out.println(spaces+") and statement {");
				printTree(ifcs.statement, spaces);
				System.out.println(spaces+"}");
				if (ifcs.else_statement != null) {
					System.out.println(spaces+"and else statement {");
					printTree(ifcs.else_statement, spaces);
					System.out.println(spaces+"}");
				}
				break;
			case 16:
				TreeNodeCondStatement wcs = (TreeNodeCondStatement) t;
				System.out.println(spaces+"Node type "+t.node_type+" (while statement) at line "+t.line+" has condition (");
				printTree(wcs.expression, spaces);
				System.out.println(spaces+") and statement {");
				printTree(wcs.statement, spaces);
				System.out.println(spaces+"}");
				break;
			case 17:
				System.out.println(spaces+"Node type "+t.node_type+" (return statement) at line "+t.line+" has return value of (");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+")");
				break;
			case 18:
				System.out.println(spaces+"Node type "+t.node_type+" (write statement) at line "+t.line+" writes {");
				TreeNodeStatement s18 = (TreeNodeStatement) t;
				printTree(s18.expression, spaces);
				System.out.println(spaces+"}");
				break;
			case 19: 	// TODO: make this print with more sense
				TreeNodeExpression e19 = (TreeNodeExpression) t;
				System.out.println(spaces+"Node type "+e19.node_type+" (expression) at line "+e19.line+" has id (");
				printTree(e19.var, spaces);
				System.out.println(spaces+") which equals {");
				if (e19.expression != null) {
					printTree(e19.expression, spaces);
				} else {
					System.out.println(spaces+e19.value);
				}
				System.out.println(spaces+"}");
				break;
			case 101:
				TreeNodeExpression e20 = (TreeNodeExpression) t;
				if (e20.expression != null) {
					System.out.println(spaces+e20.value+"[");
					printTree(e20.expression, spaces);
					System.out.println(spaces+"]");
				} else {
					System.out.println(spaces+e20.value);
				}
				break;
			case 100: 
				System.out.println(spaces+"Node type "+t.node_type+" (int_value) at line "+t.line+" has value "+t.value);
				break;
			}
		}
	}

	public static void main(String[] args) throws ParserException {
		Parser p = new Parser(args[0]);
		TreeNode t = p.parse();
		printTree(t,"");
	}

}
