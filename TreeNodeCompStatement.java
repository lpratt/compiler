import java.util.ArrayList;

public class TreeNodeCompStatement extends TreeNode {
	
	protected ArrayList<TreeNode> declaration;
	protected ArrayList<TreeNode> statement;
	
	public TreeNodeCompStatement(Token t, int nt) {
		super(t, nt);
		declaration = new ArrayList<TreeNode>();
		statement = new ArrayList<TreeNode>();
	}

}
