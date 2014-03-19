// TODO: Declarations

public class Parser implements Constants {

	Scan my_scanner;
	Token current_token;
	Token pushback_token;
	boolean debug = false;

	/*
	 * A constructor
	 */
	public Parser(String filename){
		pushback_token = null;
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
			if (pushback_token == null) {
				my_scanner.getNextToken();
				current_token = my_scanner.getToken();
			} else {
				current_token = pushback_token;
				pushback_token = null;
			}
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
		if (t.kind == k)
			return true;
		ParserException pe = new ParserException(message+" at line "+t.line);
		throw pe;
	}

	/*
	 * A function that takes in a token, a list of expected string, and an error message
	 * Returns a boolean if the token value and any of the expected strings are the same
	 * If they're different, throws a ParserException with the error message.
	 */
	public boolean expect(Token t, int[] k, String message) throws ParserException {
		for (int i = 0; i < k.length; i++) {
			if (t.kind == k[i])
				return true;
		}
		ParserException pe = new ParserException(message+" at line "+t.line);
		throw pe;
	}

	/*
	 * Checks to see if a current token kind is a relop
	 */
	public boolean isRelop(int k) {
		if (k == T_LEQ || k == T_LESS || k == T_EQEQ || k == T_NEQ || k == T_GREATER || k == T_GEQ)
			return true;
		return false;
	}

	/*
	 * Checks to see if a current token kind is an addop
	 */
	public boolean isAddop(int k) {
		if (k == T_PLUS || k == T_MINUS)
			return true;
		return false;
	}

	/*
	 * Checks to see if a current token kind is an mulop
	 */
	public boolean isMulop(int k) {
		if (k == T_STAR || k == T_SLASH || k == T_PERCENT)
			return true;
		return false;
	}

	/*
	 * Checks to see if a current token kind is a type specifier
	 */
	public boolean isTypeSpecifier(int k) {
		if (k == T_INT || k == T_STRING || k == T_VOID)
			return true;
		return false;
	}
	
	/*
	 * Highest level (entry) into parse
	 * Program -> Statement
	 */
	public TreeNode parse() throws ParserException {
		if (debug)
			System.out.println("Enter parse()");

		getNextToken();	// gets the first token of the program
		if (debug)
			System.out.println("Parse: "+current_token.value);

		TreeNode t = new TreeNode(current_token, PROGRAM);
		if (current_token.kind == T_EOF) {

			if (debug)
				System.out.println("Leaving parse()");	
			return t;
		} else {
			TreeNode s = parseStatement(); // TODO: change to Declaration_List
			t.next_nodes.add(s);

			if (debug)
				System.out.println("Leaving parse()");
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
		if (debug)
			System.out.println("Enter parseStatement()");

		TreeNode t = null;
		if (current_token.kind == T_LBRACE) {	// Compound_Statement
			getNextToken(); 	// advance token to read in start of exp_stmt
			if (debug) 
				System.out.println("ParseStatement{: "+current_token.value);
			t = parseCompoundStatement();
			if (expect(current_token, T_RBRACE, "Not a valid CompoundStatement: Missing '}'")) {
				if (debug)
					System.out.println("Leaving parseStatement()");
				return t;
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

		if (debug)
			System.out.println("Leaving parseStatement()");
		return t;
	}

	/*
	 * Compound_Statement -> {Statment(s)}
	 * TODO: expand into {LOCAL_DECS STATEMENT_LIST}
	 */
	public TreeNode parseCompoundStatement() throws ParserException {
		if (debug)
			System.out.println("Enter parseCompoundStatement()");

		TreeNodeStatement t = new TreeNodeStatement(current_token, COMPOUND_STATEMENT, null);
		while (current_token.kind != T_RBRACE && current_token.kind != T_EOF) {
			TreeNode e = parseStatement();
			t.next_nodes.add(e);
			getNextToken();
			if (debug)
				System.out.println("Compound Statement: "+current_token.value);
		}

		if (debug)
			System.out.println("Leaving parseCompoundStatement()");
		return t;
	}

	/*
	 * If_Statement -> IF (Expression) Statement | IF (Expression) Statement ELSE Statement
	 */
	public TreeNode parseIfStatement() throws ParserException {
		if (debug)
			System.out.println("Enter parseIfStatement()");

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

					if (debug)
						System.out.println("Leaving parseIfStatement()");
					return new TreeNodeCondStatement(save_token, IF_STATEMENT, e, s, es);
				} else { 	// return 1st rule
					pushback_token = current_token;
					if (debug)
						System.out.println("Leaving parseIfStatement()");
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
		if (debug)
			System.out.println("Enter parseWhileStatement()");

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

				if (debug)
					System.out.println("Leaving parseWhileStatement()");
				return new TreeNodeCondStatement(save_token, WHILE_STATEMENT, e, s, null);
			}
		}
		return null;
	}

	/*
	 * Return_Statement -> RETURN; | RETURN Expression;
	 */
	public TreeNode parseReturnStatement() throws ParserException {
		if (debug)
			System.out.println("Enter parseReturnStatement()");

		TreeNode t = new TreeNode(current_token, RETURN_STATEMENT);
		getNextToken(); 	// should be ; or expression
		if (debug)
			System.out.println("Return: "+current_token.value);

		if (current_token.kind == T_SEMICOLON) {
			if (debug)
				System.out.println("Leaving parseReturnStatement()");
			return t;
		} else {
			TreeNode e = parseExpression();
			t.next_nodes.add(e);
			if (debug)
				System.out.println("Leaving parseReturnStatement()");
			return t;
		}
	}

	/*	
	 * Write_Statement -> WRITE (Expression); | WRITELN();
	 */
	public TreeNode parseWriteStatement() throws ParserException {
		if (debug)
			System.out.println("Enter parseWriteStatement()");

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
						if (debug)
							System.out.println("Leaving parseWriteStatement()");
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
						if (debug)
							System.out.println("Leaving parseWriteStatement()");
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
		if (debug)
			System.out.println("Enter parseExpressionStatement()");

		if (current_token.kind == T_SEMICOLON) {
			if (debug)
				System.out.println("Leaving parseExpressionStatement()");
			return new TreeNodeStatement(current_token, EXPRESSION_STATEMENT, null);
		} else {
			TreeNode t = parseExpression();
			Token save_token = current_token;
			getNextToken();
			if (debug)
				System.out.println("Expression Statement: "+current_token.value);

			if (expect(current_token, T_SEMICOLON, "Not a valid ExpressionStatement: Missing ';'")) {
				if (debug)
					System.out.println("Leaving parseExpressionStatement()");
				return new TreeNodeStatement(save_token, EXPRESSION_STATEMENT, t);
			}
		}
		return null;
	}

	/*
	 * Expression -> Var = Expression | Comp_Expression
	 * Var and Comp_Expression can both start with <id>, *<id>, TODO: <id>[Expression], <num>
	 */

	public TreeNode parseExpression() throws ParserException {
		if (debug)
			System.out.println("Enter parseExpression()");

		TreeNodeExpression t = new TreeNodeExpression(current_token, EXPRESSION, null);

		// if it's <id> or *<id>, could be var or comp_exp
		// assume it's a comp_exp
		TreeNode ce = parseCompExpression();

		if (current_token.kind == T_EQUAL) { 	// continue on var = expression
			t.var = ce;
			getNextToken(); 	// advance to start expression
			if (debug)
				System.out.println("Expression exp: "+current_token.value);

			TreeNode exp = parseExpression();
			t.expression = exp;

			if (debug)
				System.out.println("Leaving parseExpression()");
			return t;
		}

		t.expression = ce;
		return t;
	}
	/*public TreeNode parseExpression() throws ParserException {
		if (debug)
			System.out.println("Enter parseExpression()");

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

			if (isRelop(current_token.kind)) { 	// because Var and E can both start with an <id> or *<id>
				TreeNodeCompExpression c = new TreeNodeCompExpression(save_token, COMP_EXPRESSION);
				v.node_type = EXPRESSION_FACTOR;
				c.e1 = v;
				c.op = parseRelop();
				getNextToken(); 	// advance to start e
				if (debug)
					System.out.println("Comp_exp (relop) E2: "+current_token.value);

				c.e2 = parseE();

				if (debug)
					System.out.println("Leaving parseExpression()");
				return c;
			}

			if (isAddop(current_token.kind)) { 	// because Var and E can both start with an <id> or *<id>
				TreeNodeCompExpression c = new TreeNodeCompExpression(save_token, E_EXPRESSION);
				v.node_type = EXPRESSION_FACTOR;
				c.e1 = v;
				c.op = parseAddop();
				getNextToken(); 	// advance to start e
				if (debug)
					System.out.println("Comp_exp (addop) E2: "+current_token.value);

				c.e2 = parseE();

				if (debug)
					System.out.println("Leaving parseExpression()");
				return c;
			}

			if (current_token.kind == T_RBRACKET) { 	// represents an <id>[Expression] that needs to be ended
				if (debug)
					System.out.println("Leaving parseExpression()");
				return v;
			}

			if (current_token.kind == T_SEMICOLON) { 	// represents comp_exp -> E
				v.node_type = EXPRESSION_FACTOR;

				if (debug)
					System.out.println("Leaving parseExpression()");
				return v;
			}

			if (expect(current_token, T_EQUAL, "Not a valid Expression: Missing '='")) {
				getNextToken(); 	// advance to start expression
				if (debug)
					System.out.println("Expression exp: "+current_token.value);

				TreeNode e = parseExpression();
				t.expression = e;

				if (debug)
					System.out.println("Leaving parseExpression()");
				return t;
			}
		} else if (current_token.kind == T_NUM) {
			TreeNode n = new TreeNode(current_token, EXPRESSION_INT);
			getNextToken(); 	// check to see if comp_exp (should be relop)
			adv = false;
			if (debug)
				System.out.println("Comp_exp num: "+current_token.value);

			if (isRelop(current_token.kind)) { 	// continue parsing as if comp_exp
				adv = true;
				TreeNodeCompExpression c = new TreeNodeCompExpression(save_token, COMP_EXPRESSION);
				n.node_type = EXPRESSION_FACTOR;
				c.e1 = n;
				c.op = parseRelop();
				getNextToken(); 	// advance to start e
				if (debug)
					System.out.println("Comp_exp (relop) E2: "+current_token.value);

				c.e2 = parseE();

				if (debug)
					System.out.println("Leaving parseExpression()");
				return c;
			}

			if (isAddop(current_token.kind)) { 	// continue parsing as if comp_exp
				adv = true;
				TreeNodeCompExpression c = new TreeNodeCompExpression(save_token, E_EXPRESSION);
				n.node_type = EXPRESSION_FACTOR;
				c.e1 = n;
				c.op = parseAddop();
				getNextToken(); 	// advance to start e
				if (debug)
					System.out.println("Comp_exp (addop) E2: "+current_token.value);

				c.e2 = parseE();

				if (debug)
					System.out.println("Leaving parseExpression()");
				return c;
			}

			if (current_token.kind == T_RBRACKET) { 	// represents an <id>[Expression] that needs to be ended
				if (debug)
					System.out.println("Leaving parseExpression()");
				return n;
			}

			if (current_token.kind == T_SEMICOLON) { 	// represents comp_exp -> E
				n.node_type = EXPRESSION_FACTOR;

				if (debug)
					System.out.println("Leaving parseExpression()");
				return n;
			}

			if (debug)
				System.out.println("Leaving parseExpression()");
			return n;

		} else if (current_token.kind == T_MINUS || current_token.kind == T_AMP) {
			if (debug)
				System.out.println("Leaving parseExpression()");
			return parseCompExpression();
		}
		else {
			ParserException pe = new ParserException("Not a valid Expression: not an id or num.");
			throw pe;
		}
		return null;
	}*/

	/*
	 * Comp_Expression -> E RELOP E | E
	 */
	public TreeNode parseCompExpression() throws ParserException {
		if (debug)
			System.out.println("Enter parseCompExpression()");

		TreeNodeCompExpression t = new TreeNodeCompExpression(current_token, COMP_EXPRESSION);
		t.e1 = parseE();

		getNextToken(); 	// advance to check relop/op
		if (debug)
			System.out.println("CE relop: "+current_token.value);

		if (isRelop(current_token.kind)) { 	// E RELOP E
			t.op = parseRelop();
			getNextToken(); 	// advance to start e2
			if (debug)
				System.out.println("CE e2: "+current_token.value);

			t.e2 = parseE();
		} else if (current_token.kind == T_EQUAL) { 	// supposed to be a var, go back
			t.e1.node_type = VAR;
			return t.e1;
		} else { 	
			pushback_token = current_token;
		}
		if (debug)
			System.out.println("Leaving parseCompExpression()");
		return t;
	}

	/*
	 * Var -> <id> | <id>[Expression] | *<id>
	 * TODO: this method is never used. this shouldn't happen
	 */
	/*public TreeNode parseVar() throws ParserException {
		if (debug)
			System.out.println("Enter parseVar()");

		TreeNodeExpression t = new TreeNodeExpression(current_token, EXPRESSION_VAR, null);
		if (current_token.kind == T_ID) {
			getNextToken(); 	// check to see if [
			if (debug)
				System.out.println("Var [: "+current_token.value);

			if (current_token.kind == T_LBRACKET) { 	// if <id>[Expression]
				getNextToken(); 	// advance to start expression
				if (debug)
					System.out.println("Var exp: "+current_token.value);

				TreeNode e = parseExpression();
				if (adv)
					getNextToken(); 	// should be ]
				if (debug)
					System.out.println("Var ]: "+current_token.value);

				if (expect(current_token, T_RBRACKET, "Not a valid var: Missing ']'")) {
					getNextToken(); 	// to stay consistent, allows for returning to expression on correct token
					if (debug)
						System.out.println("Var exp2: "+current_token.value);

					t.expression = e;
					t.value = t.value+"["+e.value+"]"; 	// TODO: not sure if I want to change the value

					if (debug)
						System.out.println("Leaving parseVar()");
					return t;
				}
			} else { 	// <id>
				pushback_token = current_token;

				if (debug)
					System.out.println("Leaving parseVar()");
				return t;
			}
		} else { 	// *<id>
			getNextToken(); 	// should be <id>
			if (debug)
				System.out.println("Var id: "+current_token.value);

			if (expect(current_token, T_ID, "Not a valid var: not an <id> after *")) {
				t.value = "*"+current_token.value;

				if (debug)
					System.out.println("Leaving parseVar()");
				return t;
			}
		}
		return null;
	}
	 */

	/*
	 * E -> E ADDOP T | T
	 */
	public TreeNode parseE() throws ParserException {
		if (debug)
			System.out.println("Enter parseE()");

		TreeNodeCompExpression t = new TreeNodeCompExpression(current_token, ET_EXPRESSION);
		t.e2 = parseT();

		getNextToken(); 	// to get addop
		if (debug)
			System.out.println("E addop1: "+current_token.value);

		while(isAddop(current_token.kind)) {
			TreeNode o = parseAddop();
			getNextToken(); 	// to get to start of T
			if (debug)
				System.out.println("E isaddop: "+current_token.value);

			TreeNodeCompExpression e = new TreeNodeCompExpression(current_token, ET_EXPRESSION);
			e.e1 = t;
			e.op = o;
			e.e2 = parseT();
			getNextToken(); 	// to get addop TODO: needed?
			if (debug)
				System.out.println("E isaddop1: "+current_token.value);

			t = e;
		}

		if (debug)
			System.out.println("E: "+current_token.value);

		pushback_token = current_token;

		if (debug)
			System.out.println("Leaving parseE()");
		return t;
	}

	/*
	 * T -> T MULOP F | F
	 */
	public TreeNode parseT() throws ParserException {
		if (debug)
			System.out.println("Enter parseT()");

		TreeNodeCompExpression t = new TreeNodeCompExpression(current_token, ET_EXPRESSION);
		t.e2 = parseF();

		getNextToken(); 	// to get mulop
		if (debug)
			System.out.println("T mulop1: "+current_token.value);

		while(isMulop(current_token.kind)) {
			TreeNode o = parseMulop();
			getNextToken(); 	// to get to start of F
			if (debug)
				System.out.println("T ismulop: "+current_token.value);

			TreeNodeCompExpression e = new TreeNodeCompExpression(current_token, ET_EXPRESSION);
			e.e1 = t;
			e.op = o;
			e.e2 = parseF();
			getNextToken(); 	// to get mulop TODO: needed?
			if (debug)
				System.out.println("T ismulop1: "+current_token.value);

			t = e;
		}

		pushback_token = current_token;
		if (debug)
			System.out.println("T: "+current_token.value);

		if (debug)
			System.out.println("Leaving parseT()");
		return t;
	}

	/*
	 * F -> -Factor | &Factor | *Factor | Factor
	 * TODO: Expand this
	 */
	public TreeNode parseF() throws ParserException {
		if (debug)
			System.out.println("Enter parseF()");

		TreeNode f = null; 	// send these tokens to parseFactor()
		if (current_token.kind == T_ID || current_token.kind == T_NUM || current_token.kind == T_STRLIT || current_token.kind == T_READ || current_token.kind == T_LPAREN) {
			TreeNode f1 = parseFactor();

			if (debug)
				System.out.println("Leaving parseF()");
			
			return f1;
		} 

		int[] k = new int[] {T_MINUS, T_AMP, T_STAR};
		if (expect(current_token, k, "Not a valid F")) {
			Token save_token = current_token;
			getNextToken(); 	// advance to start factor
			if (debug)
				System.out.println("F: "+current_token.value);

			f = parseFactor();
			f.value = save_token.value+current_token.value;

			if (debug)
				System.out.println("Leaving parseF()");
			return f;
		}
		return null;
	}

	/*
	 * Factor -> (Expression) | Fun_Call | read() | <num> | <string> (strlit) | <id> | *<id> | <id>[Expression]
	 * TODO: Expand into full production
	 */
	public TreeNode parseFactor() throws ParserException {
		if (debug)
			System.out.println("Enter parseFactor()");

		TreeNode f = new TreeNode(current_token, EXPRESSION_FACTOR);

		if (current_token.kind == T_LPAREN) { 	// parses (Expression)
			getNextToken(); 	// to advance to start of expression
			if (debug)
				System.out.println("Factor Expression: "+current_token.value);

			TreeNode e = parseExpression();
			f.next_nodes.add(e);

			getNextToken(); 	// should be )
			if (debug)
				System.out.println("Factor ) "+current_token.value);

			if (expect(current_token, T_RPAREN, "Not a valid factor (expression): Missing ')'")) {
				if (debug)
					System.out.println("Leaving parseFactor()");

				f.value = f.value+f.next_nodes.get(0).value+")"; 	// TODO: do I really want to change the value?	
				return f;
			}
		}

		if (current_token.kind == T_STAR) { 	// parses *<id>
			getNextToken(); 	// should be <id>
			if (debug)
				System.out.println("Factor *<id>: "+current_token.value);

			if (expect(current_token, T_ID, "Not a valid factor: needs to be *<id>")) {
				if (debug)
					System.out.println("Leaving parseFactor()");

				f.value = "*"+f.value;
				return f;
			}
		}

		int[] k = new int[] {T_NUM, T_STRLIT, T_ID, T_READ}; 
		if (expect(current_token, k, "Not a valid factor: needs to be <num>, <strlit>, or <id>")) {
			if (current_token.kind == T_ID) { 	// parses <id>[Expression]
				getNextToken(); 	// should be [
				if (debug)
					System.out.println("Factor id[: "+current_token.value);

				if (current_token.kind == T_LBRACKET) { 	// continue parsing <id>[Expression]
					getNextToken(); 	// advance to start expression
					if (debug)
						System.out.println("Factor id[] expression: "+current_token.value);

					TreeNode e = parseExpression();
					f.next_nodes.add(e);
					getNextToken(); 	// should be ]
					if (debug)
						System.out.println("Factor id[]: "+current_token.value);

					if (expect(current_token, T_RBRACKET, "Not a valid factor <id>[Expression]: Missing ']'")) {
						if (debug)
							System.out.println("Leaving parseFactor()");

						f.value = f.value+"["+f.next_nodes.get(0).value+"]"; 	// TODO: do I really want to change the value?	
						return f;
					}
				} else if (current_token.kind == T_LPAREN) { 	// parse Fun_Call
					f.node_type = FUN_CALL;
					getNextToken(); 	// advance to start of args
					if (debug)
						System.out.println("Factor args: "+current_token.value);

					if (current_token.kind == T_RPAREN) {
						if (debug)
							System.out.println("Leaving parseFactor()");

						return f;
					}

					f.next_nodes.add(parseFunCall());
					getNextToken(); 	// should be )
					if (debug)
						System.out.println("Factor funcall ): "+current_token.value);

					if (expect(current_token, T_RPAREN, "Not a valid factor Fun_Call: Missing ')'")) {
						if (debug)
							System.out.println("Leaving parseFactor()");

						return f;
					}
				}
				else {
					pushback_token = current_token;
				}
			}

			if (current_token.kind == T_READ) { 	// parses read()
				getNextToken(); 	// should be (
				if (debug)
					System.out.println("Factor read (: "+current_token.value);

				if (expect(current_token, T_LPAREN, "Not a valid read: Missing '('")) {
					getNextToken(); 	// should be )
					if (debug)
						System.out.println("Factor read ): "+current_token.value);

					if (expect(current_token, T_RPAREN, "Not a valid read: Missing ')'")) {
						return f;
					}
				}
			}
			if (debug)
				System.out.println("Leaving parseFactor()");
			//return new TreeNode(current_token, EXPRESSION_FACTOR);
			return f;
		}
		return null;
	}

	/*
	 * Fun_Call -> <id>(ARGS)
	 * ARGS -> <empty> (done in factor) | Expression, Expression...
	 */
	public TreeNode parseFunCall() throws ParserException {
		if (debug)
			System.out.println("Enter parseFunCall()");

		TreeNode t = new TreeNode(current_token, ARGS);
		t.next_nodes.add(parseExpression());
		getNextToken(); 	// should be , or other
		if (debug)
			System.out.println("FunCall ,: "+current_token.value);

		while (current_token.kind == T_COMMA) {
			getNextToken(); 	// advance to start of next expression
			if (debug)
				System.out.println("FunCall while: "+current_token.value);

			t.next_nodes.add(parseExpression());
			getNextToken(); 	// should be , or nothing
			if (debug)
				System.out.println("FunCall while ,: "+current_token.value);
		}

		pushback_token = current_token;

		if (debug)
			System.out.println("Leaving parseFunCall()");

		return t;
	}

	/*
	 * Relop -> <= | < | == | != | > | >=
	 */
	public TreeNode parseRelop() {
		if (debug) {
			System.out.println("Enter parseRelop()");
			System.out.println("Leaving parseRelop()");
		}
		return new TreeNode(current_token, EXPRESSION_RELOP);
	}

	/*
	 * Addop -> + | -
	 */
	public TreeNode parseAddop() {
		if (debug) {
			System.out.println("Enter parseAddop()");
			System.out.println("Leaving parseAddop()");
		}
		return new TreeNode(current_token, OP);
	}

	/*
	 * Mulop -> * | / | %
	 */
	public TreeNode parseMulop() {
		if (debug) {
			System.out.println("Enter parseAddop()");
			System.out.println("Leaving parseAddop()");
		}
		return new TreeNode(current_token, OP);
	}

	/*
	 * Preorder traversal of tree
	 * TODO: Factor printing everything
	 */
	public static void printTree(TreeNode t, String spaces) {
		spaces += " ";
		if (t != null) {
			switch (t.node_type) {
			case PROGRAM: 
				System.out.println(spaces+"Program node at line "+t.line+" consists of {");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+"}");
				break;
			case COMPOUND_STATEMENT:
				System.out.println(spaces+"Compound Statement node at line "+t.line+" consists of {");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+"}");
				break;
			case EXPRESSION_STATEMENT:
				TreeNodeStatement es = (TreeNodeStatement) t;
				System.out.println(spaces+"Expression Statement node at line "+es.line+" has expression {");
				printTree(es.expression, spaces);
				System.out.println(spaces+"}");
				break;
			case IF_STATEMENT:
				TreeNodeCondStatement is = (TreeNodeCondStatement) t;
				System.out.println(spaces+"If Statement node at line "+is.line+" has expression (");
				printTree(is.expression, spaces);
				System.out.println(spaces+") and statement {");
				printTree(is.statement, spaces);
				if (is.else_statement != null) {
					System.out.println(spaces+"} and else statement {");
					printTree(is.else_statement, spaces);
				}
				System.out.println(spaces+"}");
				break;
			case WHILE_STATEMENT:
				TreeNodeCondStatement ws = (TreeNodeCondStatement) t;
				System.out.println(spaces+"While Statement node at line "+ws.line+" has expression (");
				printTree(ws.expression, spaces);
				System.out.println(spaces+") and statement {");
				printTree(ws.statement, spaces);
				System.out.println(spaces+"}");
				break;
			case RETURN_STATEMENT:
				System.out.println(spaces+"Return Statement node at line "+t.line+" returns (");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+")");
				break;
			case WRITE_STATEMENT:
				TreeNodeStatement wrs = (TreeNodeStatement) t;
				System.out.println(spaces+"Write Statement node at line "+wrs.line+" writes (");
				printTree(wrs.expression, spaces);
				System.out.println(spaces+")");
				break;
			case EXPRESSION: 
				TreeNodeExpression e = (TreeNodeExpression) t;
				if (e.var != null) {
					System.out.println(spaces+"Expression Node at line "+e.line+" has var "+e.var.value);
					System.out.println(spaces+"} = expression {");
					printTree(e.expression, spaces);
					System.out.println(spaces+"}");
				} else {
					System.out.println(spaces+"Expression Node at line "+e.line+" is a comp_expression {");
					printTree(e.expression, spaces);
					System.out.println(spaces+"}");
				}
				break;
			case COMP_EXPRESSION:
				TreeNodeCompExpression ce = (TreeNodeCompExpression) t;
				if (ce.op != null) {
					System.out.println(spaces+"Comp Expression Node at line "+ce.line+" has left child {");
					printTree(ce.e1, spaces);
					System.out.println(spaces+"} operator "+ce.op.value+" and right child {");
					printTree(ce.e2, spaces);
					System.out.println(spaces+"}");
				} else if (ce.e1.node_type == ET_EXPRESSION) {
					System.out.println(spaces+"Comp Expression Node at line "+ce.line+" has E {");
					printTree(ce.e1, spaces);
					System.out.println(spaces+"}");
				}
				break;
			case ET_EXPRESSION:
				TreeNodeCompExpression ee = (TreeNodeCompExpression) t;
				if (ee.e1 == null) {
					System.out.println(spaces+"ET Expression Node at line "+ee.line+" has E or T {");
					printTree(ee.e2, spaces);
					System.out.println(spaces+"}");
				} else {
					System.out.println(spaces+"ET Expression Node at line "+ee.line+" has left child {");
					printTree(ee.e1, spaces);
					System.out.println(spaces+"} operator "+ee.op.value+" and right child {");
					printTree(ee.e2, spaces);
					System.out.println(spaces+"}");
				}
				break;
			case EXPRESSION_FACTOR:
				System.out.println(spaces+"Factor Node at line "+t.line+" has the value "+t.value);
				break;
			case FUN_CALL:
				System.out.println(spaces+"Fun Call node at line "+t.line+" has value "+t.value+" and args (");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+")");
				break;
			case ARGS:
				System.out.println(spaces+"Args node at line "+t.line+" consists of {");
				for (int i = 0; i < t.next_nodes.size(); i++) {
					printTree(t.next_nodes.get(i), spaces);
				}
				System.out.println(spaces+"}");
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
