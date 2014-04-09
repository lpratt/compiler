import java.util.Hashtable;
import java.util.ArrayList;

public class TypeChecker implements Constants {

	Parser my_parser;
	TreeNode parse_tree;
	ArrayList<Hashtable<String, TreeNode>> ht_stack;

	TreeNode last_added_function;
	boolean debug = true;
	boolean check = false;

	public TypeChecker(String filename) {
		try {
			my_parser = new Parser(filename);
			parse_tree = my_parser.parse();
			ht_stack = new ArrayList<Hashtable<String, TreeNode>>();
		} catch (Exception e) {
		}
	}



	/*
	 * Gets the already-in-table link and sets it
	 */
	public TreeNode setLink(String key) throws TypeCheckerException {
		//System.out.println("SETTING STRING LINK: "+key);
		TreeNode dec;
		for (int i = 0; i < ht_stack.size(); i++) {
			if (ht_stack.get(i).get(key) != null) {
				dec = ht_stack.get(i).get(key);
				if (debug)
					System.out.println(key+" linked to "+dec.value+" on line "+dec.line);
				return dec;
			}
		}

		TypeCheckerException tce = new TypeCheckerException("Key not in hashtable");
		throw tce;
	}


	/*
	 * Gets the already-in-table link and sets it
	 */
	@SuppressWarnings("finally")
	public TreeNode setLink(TreeNode key) throws TypeCheckerException {
		String key_string = key.value;
		try {
			TreeNodeDec vd = (TreeNodeDec) key;
			if (vd.is_minus) {
				key_string = "-"+vd.name;
			} else {
				key_string = vd.name;
			}
		} catch (Exception e) {
			if (key.is_minus) {
				key_string = "-"+key.value;
			}
		} finally {
			for (int i = 0; i < ht_stack.size(); i++) {
				if (ht_stack.get(i).get(key_string) != null) {
					key.declaration = ht_stack.get(i).get(key_string);
					if (debug)
						System.out.println("Setting link from "+key_string+" on line "+key.line+" to "+key_string+" on line "+key.declaration.line);
					return key;
				}
			}

			//System.out.println("ERROR: "+key_string);
			TypeCheckerException tce = new TypeCheckerException("Key not in hashtable");
			throw tce;
		}
	}

	/*
	 * Gets the already-in-table link from hashtable at index and sets it
	 */
	@SuppressWarnings("finally")
	public TreeNode setLink(TreeNode key, int index) throws TypeCheckerException {
		String key_string = key.value;
		try {
			TreeNodeDec vd = (TreeNodeDec) key;
			if (vd.is_minus) {
				key_string = "-"+vd.name;
			} else {
				key_string = vd.name;
			}
		} catch (Exception e) {
			if (key.is_minus) {
				key_string = "-"+key.value;
			}
		} finally {

			if (ht_stack.get(index).get(key_string) != null) {
				key.declaration = ht_stack.get(index).get(key_string);
				if (debug)
					System.out.println("Setting link from "+key_string+" on line "+key.line+" to "+key_string+" on line "+key.declaration.line);
				return key;
			}

			TypeCheckerException tce = new TypeCheckerException("Key not in hashtable");
			throw tce;
		}
	}

	/*
	 * Adds a TreeNode (variable initialization) to top hashtable at the top of the stack
	 * Will makeLinks from VAR_DEC, FUN_DEC, PARAM
	 */
	public void inputLink(TreeNode key) {
		String key_string = key.value;
		try {
			TreeNodeDec vd = (TreeNodeDec) key;
			if (vd.is_minus) {
				key_string = "-"+vd.name;
			} else {
				key_string = vd.name;
			}
		} catch (Exception e) {
			System.out.println("EXCEPTION IN INPUT");
		}
		if (key_string != null)
			ht_stack.get(0).put(key_string, key);

		if (debug) {
			if (key_string != null)
				System.out.println("Making link: "+key_string+" to "+key_string+" with ht of size "+ht_stack.get(0).size()+" and stack of size "+ht_stack.size());
		}
	}

	/*
	 * Calls checkType and will throw an error if not correct
	 * TODO: change what prints out?
	 */
	public boolean isTypeCorrect(TreeNode t) throws TypeCheckerException {
		String ct = checkType(t);
		if (ct.equals("false")) { 	// check the type
			TypeCheckerException tce = new TypeCheckerException(t.value+" not type correct!");
			throw tce;
		} else if (ct.equals("true")) {
			if (debug) 
				System.out.println("Correctly type checked function call on line "+t.line);

		} else {
			if (debug) {
				System.out.println("Correctly type checked expression on line "+t.line+" as "+ct);
			}
		}
		return true;
	}

	/*
	 * Type checks the given TreeNode
	 */
	public String checkType(TreeNode t) {
		switch (t.node_type) {
		case PROGRAM:
			break;
		case COMPOUND_STATEMENT:
			break;
		case EXPRESSION_STATEMENT:
			break;
		case IF_STATEMENT:
			break;
		case WHILE_STATEMENT:
			break;
		case RETURN_STATEMENT:
			break;
		case WRITE_STATEMENT:
			break;
		case WRITELN_STATEMENT:
			break;
		case EXPRESSION:
			TreeNodeExpression e = (TreeNodeExpression) t;
			if (e.var != null) { 	// var = expression
				String s = checkType(e.expression);
				if (e.var.declaration.type.equals(s)) {
					return s;
				}
				if (s.equals("read")) {
					return e.var.declaration.type;
				}
				return "false";
			} else {
				String s = checkType(e.expression);
				return s;
			}
		case COMP_EXPRESSION:
			TreeNodeCompExpression ce = (TreeNodeCompExpression) t;
			String sce1 = checkType(ce.e1);
			if (ce.op != null) {
				if (sce1.equals(checkType(ce.e2))) {
					return sce1;
				} else {
					return "false";
				}
			}
			return sce1;
		case ET_EXPRESSION:
			TreeNodeCompExpression ee = (TreeNodeCompExpression) t;
			if (ee.e1 == null) {
				//				System.out.println("CT ET E2: "+ee.e2.value);
			} else {
				//				System.out.println(ee.e1.declaration.type+" "+ee.e2.type);
				String eet = ee.e1.type;
				if (eet.equals(ee.e2.type)) {
					return eet;
				} else {
					return "false";
				}
			}
			break;
		case EXPRESSION_FACTOR:
			if (t.value.equals("read")) {
				return "read";
			}
			if (t.declaration != null) {
				return t.declaration.type;
			}
			return t.type;
		case FUN_CALL:
			TreeNodeDec dt = (TreeNodeDec) t.declaration;
			for (int i = 0; i < t.next_nodes.size(); i++) {
				try { 	// to make sure function has correct number of arguments
					String q = checkType(t.next_nodes.get(i).next_nodes.get(i));
					String s = checkType(dt.params.next_nodes.get(i));
					if (!q.equals(s)) {
						return "false";
					}
				} catch (Exception e2) {
					System.out.println("Incorrect number of args in "+dt.value+" on line "+dt.line);
					System.exit(1);
					//					TypeCheckerException tce = new TypeCheckerException("Incorrect number of args in "+dt.value+" on line "+dt.line);
					//					throw tce;
				}
			}
			return "true";
		case ARGS: 	// TODO: remove because dealt with above?
			break;
		case VAR_DEC:
			return t.type;
		case FUN_DEC:
			break;
		case PARAM_LIST:
			for (int i = 0; i < t.next_nodes.size(); i++) {
				checkType(t.next_nodes.get(i));
			}
			break;
		case PARAM:
			return t.type;
		case VAR:
			System.out.println("t.value: "+t.is_array);
			if (t.is_array) {
				System.out.println("alength: "+t.array_size);
			}
			break;
		}
		return "false";
	}

	public TreeNode traverseTree(TreeNode t) throws TypeCheckerException {
		switch (t.node_type) {
		case PROGRAM:
			Hashtable<String, TreeNode> ht = new Hashtable<String, TreeNode>(); 	// create new hashtable
			ht_stack.add(0, ht); 	// and put it on the stack
			for (int i = 0; i < t.next_nodes.size(); i++) {
				t.next_nodes.set(i, traverseTree(t.next_nodes.get(i))); 	// recurse on next_nodes

			}
			ht_stack.remove(0); 	// remove ht from stack
			ht_stack.trimToSize(); 	// for testing purposes
			return t;
		case COMPOUND_STATEMENT:
			Hashtable<String, TreeNode> csht = new Hashtable<String, TreeNode>(); 	// create new hashtable
			ht_stack.add(0, csht); 	// and put it on the stack
			TreeNodeCompStatement comps = (TreeNodeCompStatement) t;
			for (int i = 0; i < comps.declaration.size(); i++) {
				comps.declaration.set(i, traverseTree(comps.declaration.get(i)));
			}
			for (int i = 0; i < comps.statement.size(); i++) {
				comps.statement.set(i, traverseTree(comps.statement.get(i)));
			}
			ht_stack.remove(0); 	// remove csht from stack
			ht_stack.trimToSize(); 	// for testing purposes
			return t;
		case EXPRESSION_STATEMENT:
			TreeNodeStatement es = (TreeNodeStatement) t;
			es.expression = traverseTree(es.expression);
			return es;
		case IF_STATEMENT:
			TreeNodeCondStatement is = (TreeNodeCondStatement) t;
			is.expression = traverseTree(is.expression);
			is.statement = traverseTree(is.statement);
			if (is.else_statement != null) {
				traverseTree(is.else_statement);
			}
			return is;
		case WHILE_STATEMENT:
			TreeNodeCondStatement ws = (TreeNodeCondStatement) t;
			ws.expression = traverseTree(ws.expression);
			ws.statement = traverseTree(ws.statement);
			return ws;
		case RETURN_STATEMENT: 	// TODO: not totally complete?
			TreeNodeExpression rs = (TreeNodeExpression) t.next_nodes.get(0);
			try {

				rs.expression = setLink(traverseTree(rs.expression)); 	// set the declaration field
				if (check)
					System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);

				if (!last_added_function.type.equals(rs.expression.declaration.type)) {
					TypeCheckerException tce = new TypeCheckerException("Return value of incorrect type.");
					throw tce;
					//					System.out.println("dectype");
					//					System.exit(0);
				} else {
					System.out.println("Return type matches "+rs.expression.declaration.type+" on line "+rs.line);
				}
			} catch (TypeCheckerException tce) {
				if (!last_added_function.type.equals(rs.expression.type)) {
					throw tce;
					//					System.exit(0);
				} else {
					System.out.println("Return type matches "+rs.expression.type+" on line "+rs.line);
				}
			}

			return rs;
		case WRITE_STATEMENT:
			TreeNodeStatement wrs = (TreeNodeStatement) t;
			wrs.expression = traverseTree(wrs.expression);
			return wrs;
		case WRITELN_STATEMENT:
			// don't need to change anything here
			return t;
		case EXPRESSION:
			TreeNodeExpression e = (TreeNodeExpression) t;
			if (e.var != null) { 	// var = expression
				e.var = setLink(e.var); 	// set the declaration field
				if (check)
					System.out.println("Setting check: line "+e.var.line+" to line "+e.var.declaration.line);

				if (e.var.is_array) { 	// checks to make sure [int]
					try { 
						Integer.parseInt(e.var.array_size); 	// know it's an int
					} catch (Exception e2) { 	// check to see if variable in hashtable
						TreeNode tnt = setLink(e.var.array_size); 	// setLink(String)
						String yes = checkType(tnt);
						if (!yes.equals("int")) { 	// if not an int, throw an exception
							TypeCheckerException tce = new TypeCheckerException("Array not [int]");
							throw tce;
						}
					}	
				}

				e.expression = traverseTree(e.expression);
				isTypeCorrect(e);
			} else { 	// comp_expression
				e.expression = traverseTree(e.expression);
				if (e.expression.node_type == EXPRESSION_FACTOR) {
					if (e.expression.is_array) { 	// checks to make sure [int]
						try { 
							Integer.parseInt(e.expression.array_size); 	// know it's an int
						} catch (Exception e2) { 	// check to see if variable in hashtable
							TreeNode tnt = setLink(e.expression.array_size); 	// setLink(String)
							String yes = checkType(tnt);
							if (!yes.equals("int")) { 	// if not an int, throw an exception
								TypeCheckerException tce = new TypeCheckerException("Array not [int]");
								throw tce;
							}
						}	
					}
				} 
			}
			return e;
		case COMP_EXPRESSION:
			TreeNodeCompExpression ce = (TreeNodeCompExpression) t;
			if (ce.op != null) {
				ce.e1 = traverseTree(ce.e1);
				if (ce.e1.is_array) {
					try { 
						Integer.parseInt(ce.e1.array_size); 	// know it's an int
					} catch (Exception e2) { 	// check to see if variable in hashtable
						TreeNode tnt = setLink(ce.e1.array_size); 	// setLink(String)
						String yes = checkType(tnt);
						if (!yes.equals("int")) { 	// if not an int, throw an exception
							TypeCheckerException tce = new TypeCheckerException("Array not [int]");
							throw tce;
						}
					}	
				}
				
				ce.e2 = traverseTree(ce.e2);
				if (ce.e2.is_array) {
					try { 
						Integer.parseInt(ce.e2.array_size); 	// know it's an int
					} catch (Exception e2) { 	// check to see if variable in hashtable
						TreeNode tnt = setLink(ce.e2.array_size); 	// setLink(String)
						String yes = checkType(tnt);
						if (!yes.equals("int")) { 	// if not an int, throw an exception
							TypeCheckerException tce = new TypeCheckerException("Array not [int]");
							throw tce;
						}
					}	
				}
				isTypeCorrect(ce);
			} else if (ce.e1.node_type == ET_EXPRESSION) {
				ce.e1 = traverseTree(ce.e1);
			}
			return ce;
		case ET_EXPRESSION: 	// TODO: rethink how to do this nicely
			TreeNodeCompExpression ee = (TreeNodeCompExpression) t;
			if (ee.e1 == null) {
				try {
					ee.e2 = setLink(ee.e2); 	// set the declaration field
					if (check)
						System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);

					if (ee.e2.declaration != null) { 	// TODO: This is hacky stuff. make it nicer
						ee.e2.type = ee.e2.declaration.type;
					}
				} catch (TypeCheckerException tce) {
				}
			} else {
				ee.e1 = traverseTree(ee.e1);
				try {
					ee.e1 = setLink(ee.e1); 	// set the declaration field
					if (check)
						System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);

					if (ee.e1.declaration != null) { 	// TODO: This is hacky stuff. make it nicer
						ee.e1.type = ee.e1.declaration.type;
					} else {
					}
				} catch (TypeCheckerException tce) { 	// TODO: more hacky stuff....
					TreeNodeCompExpression atemp = (TreeNodeCompExpression) ee.e1;
					ee.e1.type = atemp.e2.type;
				}
				traverseTree(ee.e2);
				try {
					ee.e2 = setLink(ee.e2); 	// set the declaration field
					if (check)
						System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);

					if (ee.e2.declaration != null) { 	// TODO: This is hacky stuff. make it nicer
						ee.e2.type = ee.e2.declaration.type;
					}
				} catch (TypeCheckerException tce) {
				}
				isTypeCorrect(ee);
			}

			return ee;
		case EXPRESSION_FACTOR:
			try {
				t = setLink(t); 	// set the declaration field
				if (check)
					System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);
			} catch (TypeCheckerException tce) {
			}
			return t;
		case FUN_CALL:
			t = setLink(t, ht_stack.size()-1); 	// set the declaration field
			if (check)
				System.out.println("Setting check: line "+t.line+" to line "+t.declaration.line);

			for (int i = 0; i < t.next_nodes.size(); i++) {
				t.next_nodes.set(i, traverseTree(t.next_nodes.get(i)));
			}
			isTypeCorrect(t);
			return t;
		case ARGS:
			for (int i = 0; i < t.next_nodes.size(); i++) {
				t.next_nodes.set(i, traverseTree(t.next_nodes.get(i)));
			}
			return t;
		case VAR_DEC:
			TreeNodeDec vd = (TreeNodeDec) t;
			inputLink(vd); 	// add link to top of stack hashtable
			return vd;
		case FUN_DEC:
			inputLink(t); 	// add link to top of stack hashtable (should be program ht)
			last_added_function = t;
			Hashtable<String, TreeNode> fdht = new Hashtable<String, TreeNode>(); 	// create new hashtable
			ht_stack.add(0, fdht); 	// and put it on the stack
			TreeNodeDec fd = (TreeNodeDec) t;
			if (fd.params != null)
				fd.params = traverseTree(fd.params); 	// recurse on params
			if (fd.comp_statement != null)
				fd.comp_statement = traverseTree(fd.comp_statement); 	// recurse on comp_statement
			ht_stack.remove(0); 	// remove fdht from stack
			ht_stack.trimToSize(); 	// for testing purposes
			return t;
		case PARAM_LIST:
			for (int i = 0; i < t.next_nodes.size(); i++) {
				t.next_nodes.set(i, traverseTree(t.next_nodes.get(i)));
			}
			return t;
		case PARAM:
			inputLink(t); 	// add link to top of stack hashtable
			return t;
		}
		return t;
	}

	public void print() throws TypeCheckerException {
		parse_tree = traverseTree(parse_tree);
		// TODO: print out tree like in parser, but with declarations and types?
	}

	public static void main(String[] args) throws TypeCheckerException {
		TypeChecker t = new TypeChecker(args[0]);
		t.print();
	}

}
