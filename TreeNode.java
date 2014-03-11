import java.util.ArrayList;

public class TreeNode implements Constants {

	protected int line;
	protected String value;
	protected int node_type;
	protected ArrayList<TreeNode> next_nodes;
	
	public TreeNode(Token t, int nt) {
		line = t.line;
		value = t.value;
		node_type = nt;
		next_nodes = new ArrayList<TreeNode>();
	}
	
}
