
public class TreeNode implements Constants {

	protected int line;
	protected String value;
	protected int node_type;
	protected TreeNode next;
	
	public TreeNode(Token t, int nt, TreeNode tnn) {
		line = t.line;
		value = t.value;
		node_type = nt;
		next = tnn;
	}
	
	public void printNode() {
		System.out.print("Node type "+node_type+ " on line "+line+ " goes to: ");
		next.printNode();
	}
	
}
