
public class Parser implements Constants {

	Scan my_scanner;
	Token current_token;

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
	 * Highest level (entry) into parse
	 * Program -> Statement
	 */
	public TreeNode parse() throws ParserException {
		getNextToken();	// gets the first token of the program
		TreeNode t = new TreeNode(current_token, PROGRAM, null);
		if (current_token.kind == T_EOF) {
			return t;
		} else {
			TreeNode s = parseStatement(); // TODO: change to Declaration_List
			t.next = s;
			return t;
		}
	}

	/*
	 * Statement -> Expression_Statement | Compound_Statement | While_Statement
	 */
	public TreeNode parseStatement() throws ParserException {
		TreeNode t = null;
		if (current_token.kind == T_LBRACE) {	// Compound_Statement
			getNextToken(); 	// advance token to read in start of exp_stmt
			t = parseCompoundStatement();
			if (current_token.kind == T_RBRACE) {
				return t;
			} else {
				ParserException pe = new ParserException("Not a valid CompoundStatement");
				throw pe;
			}
		} else if (current_token.kind == T_WHILE) { 	// While_Statement
			t = parseWhileStatement();
		}
		else {	// Expression_Statement
			t = parseExpressionStatement();
		}
		return t;
	}

	/*
	 * Compound_Statement -> {Expression_Statment(s)}
	 */
	public TreeNode parseCompoundStatement() throws ParserException {
		TreeNodeStatement t = new TreeNodeStatement(current_token, COMPOUND_STATEMENT, null, null);
		while (current_token.kind != T_RBRACE && current_token.kind != T_EOF) {
			TreeNode e = parseExpressionStatement();
			t.next_nodes.add(e);
			getNextToken();
		}
		return t;
	}
	
	/*
	 * While_Statement -> WHILE (Expression) Statement
	 */
	public TreeNode parseWhileStatement() throws ParserException {
		getNextToken(); 	// should be (
		//System.out.println("Token 1: "+current_token.value);
		if (current_token.kind == T_LPAREN) {
			getNextToken(); 	// advance token to read in start of exp
			//System.out.println("Token 2: "+current_token.value);
			TreeNode e = parseExpression();
			getNextToken(); 	// should be )
			//System.out.println("Token 3: "+current_token.value+" "+current_token.kind);
			if (current_token.kind == T_RPAREN) {
				getNextToken(); 	// advance token to read in start of statement
				//System.out.println("Token 4: "+current_token.value);
				TreeNode s = parseStatement();
				return new TreeNodeCondStatement(current_token, WHILE_STATEMENT, null, e, s, null);
			} else {
				ParserException pe = new ParserException("Not a valid WhileStatement");
				throw pe;
			}
		} else {
			ParserException pe = new ParserException("Not a valid WhileStatement");
			throw pe;
		}
	}

	/*
	 * Expression_Statement -> Expression; | ;
	 */
	public TreeNode parseExpressionStatement() throws ParserException {
		if (current_token.kind == T_SEMICOLON) {
			return new TreeNodeStatement(current_token, EXPRESSION_STATEMENT, null, null);
		} else {
			TreeNode t = parseExpression();
			Token save_token = current_token;
			getNextToken();
			//System.out.println("Token a: "+current_token.value);
			if (current_token.kind == T_SEMICOLON) {
				return new TreeNodeStatement(save_token, EXPRESSION_STATEMENT, null, t);
			} else {
				ParserException pe = new ParserException("Not a valid ExpressionStatement.");
				throw pe;
			}
		}
	}

	/*
	 * Expression -> <id>
	 */
	public TreeNode parseExpression() throws ParserException{
		if (current_token.kind == T_ID) {
			return new TreeNode(current_token, EXPRESSION, null);	// TODO: What kind of node do I want?
		} else {
			ParserException pe = new ParserException("Not a valid Expression.");
			throw pe;
		}
	}

	/*
	 * Preorder traversal of tree
	 */
	public static void printTree(TreeNode t) {
		if (t != null) {
			switch(t.node_type) {
			case 1:
				System.out.println("Node type "+t.node_type+" (program) at line "+t.line+" goes to {");
				printTree(t.next);
				System.out.println("}");
				break;
			case 10:
				System.out.println("Node type "+t.node_type+" (compound statement) at line "+t.line+" consists of {");
				TreeNodeStatement s10 = (TreeNodeStatement) t;
				for (int i = 0; i < s10.next_nodes.size(); i++) {
					printTree(s10.next_nodes.get(i));
				}
				System.out.println("}");
				break;
			case 14: 
				System.out.println("Node type "+t.node_type+" (expression statement) at line "+t.line+" goes to {");
				TreeNodeStatement s = (TreeNodeStatement) t;
				printTree(s.expression);
				System.out.println("}");
				break;
			case 16:
				TreeNodeCondStatement wcs = (TreeNodeCondStatement) t;
				System.out.println("Node type "+t.node_type+" (while statement) at line "+t.line+" has condition (");
				printTree(wcs.expression);
				System.out.println(") and statement {");
				printTree(wcs.statement);
				System.out.println("}");
				break;
			case 19:
				System.out.println("Node type "+t.node_type+" (expression) at line "+t.line+" has value "+t.value);
				printTree(t.next);
				break;
			}
		}
	}

	public static void main(String[] args) throws ParserException {
		Parser p = new Parser(args[0]);
		TreeNode t = p.parse();
		printTree(t);
	}

}
