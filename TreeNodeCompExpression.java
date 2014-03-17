
public class TreeNodeCompExpression extends TreeNode {

	protected TreeNode e1;
	protected TreeNode op;
	protected TreeNode e2;
	
	public TreeNodeCompExpression(Token t, int nt) {
		super(t, nt);
	}

}
