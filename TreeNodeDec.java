
public class TreeNodeDec extends TreeNode {

	protected String type;
	protected String name;
	protected boolean is_pointer;
	protected boolean is_array;
	protected int array_size;
	
	protected TreeNode params;
	protected TreeNode comp_statement;
	
	public TreeNodeDec(Token t, int nt) {
		super(t, nt);
		is_pointer = false;
		is_array = false;
	}

}
