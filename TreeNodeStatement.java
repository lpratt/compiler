import java.util.ArrayList;

public class TreeNodeStatement extends TreeNode {
	
	protected TreeNode expression;
	protected ArrayList<TreeNode> next_nodes;

	public TreeNodeStatement(Token t, int nt, TreeNode tnn, TreeNode tne) {
		super(t, nt, tnn);
		expression = tne;
		next_nodes = new ArrayList<TreeNode>();
	}

}
