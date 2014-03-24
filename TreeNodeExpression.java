
public class TreeNodeExpression extends TreeNode {

	protected TreeNode var;
	protected TreeNode int_value;
	protected TreeNode string_value;
	protected TreeNode operator;
	protected TreeNode function_call;
	protected TreeNode expression;
	
	public TreeNodeExpression(Token t, int nt, TreeNode exp) {
		super(t, nt);
		expression = exp;
	}

}
