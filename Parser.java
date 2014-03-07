
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
	 * Statement -> Expression_Statement
	 */
	public TreeNode parseStatement() throws ParserException {
		TreeNode t = parseExpressionStatement();
		return t;
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
			if (current_token.kind == T_SEMICOLON) {
				return new TreeNodeStatement(save_token, EXPRESSION_STATEMENT, null, t);
			} else {
				ParserException pe = new ParserException("Not a vaoid ExpressionStatement.");
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
			ParserException pe = new ParserException("Not a vaoid Expression.");
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
				System.out.println("Node type "+t.node_type+" at line "+t.line+" goes to ");
				printTree(t.next);
				break;
			case 14: 
				System.out.println("Node type "+t.node_type+" at line "+t.line+" goes to ");
				TreeNodeStatement s = (TreeNodeStatement) t;
				printTree(s.expression);
				break;
			case 19:
				System.out.println("Node type "+t.node_type+" at line "+t.line+" has value "+t.value);
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
