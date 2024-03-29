import java.util.*;
import fig.basic.LogInfo;
public class TrieNode {
	static int GUIDCOUNT = 0;
	//private TrieNode[] children;
  HashMap<Character, TrieNode> children;
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
		//children = new TrieNode[29];
    children = new HashMap<Character, TrieNode>();
		count = 0;
	}
  void addAbstract(String suffix){
    count++;
    if(suffix.length() > 0){
      if(suffix.charAt(0) == '^'){
        getChild('^').addAbstract(suffix.substring(1));
      } else {
        getChild('*').addAbstract(suffix.substring(1));
        getChild(suffix.charAt(0)).add(suffix.substring(1));
      }
    }
  }
	void add(String suffix){
    //LogInfo.logs("suffix=%s", suffix);
		count++;
		if(suffix.length() > 0){
			getChild(suffix.charAt(0)).add(suffix.substring(1));
		}
	}
	/*void addFinal(){
		count++;
		return;
	}*/
	//int getIndex(char c){
	//	int index;
	//	if(c >= 'a' && c <= 'z'){
	//		index = (int)(c-'a');
	//	} else if(c == '$'){
	//		index = 26;
	//	} else if(c == '^'){
	//		index = 27;
	//	} else if(c == '*'){
	//		index = 28;
	//	} else {
	//		throw new RuntimeException("invalid character: " + c);
	//	}
	//	return index;
	//}
	TrieNode getChild(char c){
    if(children.get(c) == null){
      children.put(c, new TrieNode(c, this));
    }
    return children.get(c);
		//int index = getIndex(c);
		//if(children[index] == null){
		//	children[index] = new TrieNode(c, this);
		//}
		//return children[index];
	}

	String spanTo(TrieNode target){
		if(this == target) return "";
		else return spanTo(target.parent) + target.c;
	}

	boolean hasExtention(String extension){
		return getExtension(extension) != null;
	}
	TrieNode getExtension(char extension){
		return getExtension(""+extension);
	}
	TrieNode getExtension(String extension){
		if(extension.length() == 0){
			return this;
		} else {
      char c = extension.charAt(0);
      if(children.get(c) == null){
        return null;
      } else {
        return children.get(c).getExtension(extension.substring(1));
      }
			//int index = getIndex(extension.charAt(0));
			//if(children[index] == null){
			//	return null;
			//} else {
			//	return children[index].getExtension(extension.substring(1));
			//}
		}
	}
	LinkedList<TrieNode> getAllExtensions(int len){
		LinkedList<TrieNode> ret = new LinkedList<TrieNode>();
		ret.add(this);
		if(len != 0){
			for(TrieNode child : children.values()){
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
		for(TrieNode child : children.values()){
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
			for(TrieNode child : children.values()){
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
