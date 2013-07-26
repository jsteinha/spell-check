import java.util.*;
public class TrieNode {
	static int GUIDCOUNT = 0;
	TrieNode[] children;
	char c;
	int guid;
	int depth;
	int count;
	TrieNode parent, root;
	public TrieNode(char c, TrieNode parent){
		guid = GUIDCOUNT++;
		this.c = c;
		this.parent = parent;
		this.root = parent == null ? this : parent.root;
		this.depth = parent == null ? 0 : parent.depth + 1;
		children = new TrieNode[29];
		count = 0;
	}
	void add(String suffix){
		count++;
		if(suffix.length() == 0){
			getChild('$').addFinal();
		} else {
			getChild(suffix.charAt(0)).add(suffix.substring(1));
		}
	}
	void addFinal(){
		count++;
		return;
	}
	int getIndex(char c){
		int index;
		if(c >= 'a' && c <= 'z'){
			index = (int)(c-'a');
		} else if(c == '$'){
			index = 26;
		} else if(c == '^'){
			index = 27;
		} else if(c == '*'){
			index = 28;
		} else {
			throw new RuntimeException("invalid character: " + c);
		}
		return index;
	}
	TrieNode getChild(char c){
		int index = getIndex(c);
		if(children[index] == null){
			children[index] = new TrieNode(c, this);
		}
		return children[index];
	}

	String spanTo(TrieNode target){
		if(this == target) return "";
		else return spanTo(target.parent) + target.c;
	}

	boolean hasExtention(String extension){
		return getExtension(extension) != null;
	}
	TrieNode getExtension(String extension){
		if(extension.length() == 0){
			return this;
		} else {
			int index = getIndex(extension.charAt(0));
			if(children[index] == null){
				return null;
			} else {
				return children[index].getExtension(extension.substring(1));
			}
		}
	}
	LinkedList<TrieNode> getAllExtensions(int len){
		LinkedList<TrieNode> ret = new LinkedList<TrieNode>();
		ret.add(this);
		if(len != 0){
			for(TrieNode child : children){
				if(child != null){
					ret.addAll(child.getAllExtensions(len-1));
				}
			}
		}
		return ret;
	}
	LinkedList<TrieNode> getAllExtensions(){
		LinkedList<TrieNode> ret = new LinkedList<TrieNode>();
		ret.add(this);
		for(TrieNode child : children){
			if(child != null){
				ret.addAll(child.getAllExtensions());
			}
		}
		return ret;
	}

	void print(){
		print(-1, false);
	}
	void print(int indent, boolean buffer){
		if(!buffer){
			for(int i = 0; i < indent; i++)
				System.out.print(" ");
		}
		System.out.print(c);
		if(c == '$'){
			System.out.println();
		} else {
			boolean buffer2 = true;
			for(TrieNode child : children){
				if(child != null){
					child.print(indent + 1, buffer2);
					buffer2 = false;
				}
			}
		}
	}

  @Override
  public String toString(){
    return parent == null ? "" : (parent.toString() + c);
  }
}
