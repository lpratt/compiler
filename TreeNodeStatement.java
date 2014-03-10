
public class TreeNodeStatement extends TreeNode {
	
	protected TreeNode expression;
	
	public TreeNodeStatement(Token t, int nt, TreeNode tne) {
		super(t, nt);
		expression = tne;
	}

}
