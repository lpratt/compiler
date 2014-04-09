import java.util.ArrayList;

public class TreeNode implements Constants {

	protected int line;
	protected String value;
	protected int node_type;
	protected ArrayList<TreeNode> next_nodes;
	protected boolean is_pointer;
	protected boolean is_minus;
	protected boolean is_address;
	protected boolean is_array;
	protected String array_size;
	protected TreeNode declaration;
	protected String type;
	
	public TreeNode(Token t, int nt) {
		line = t.line;
		value = t.value;
		node_type = nt;
		next_nodes = new ArrayList<TreeNode>();
		is_pointer = false;
		is_minus = false;
		is_address = false;
		is_array = false;
	}
	
}
