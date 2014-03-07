
public class TreeNodeStatement extends TreeNode {
	
	protected TreeNode expression;

	public TreeNodeStatement(Token t, int nt, TreeNode tnn, TreeNode tne) {
		super(t, nt, tnn);
		expression = tne;
	}

}
