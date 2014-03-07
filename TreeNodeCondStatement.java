
public class TreeNodeCondStatement extends TreeNodeStatement {

	protected TreeNode statement;
	protected TreeNode else_statement;
	
	/*
	 * t = Token
	 * nt = node_type
	 * tnn = tree node next
	 * tne = tree node expression
	 * tns = tree node statement
	 * tnes = tree node else statement
	 */
	public TreeNodeCondStatement(Token t, int nt, TreeNode tnn, TreeNode tne, TreeNode tns, TreeNode tnes) {
		super(t, nt, tnn, tne);
		statement = tns;
		else_statement = tnes;
	}

}
