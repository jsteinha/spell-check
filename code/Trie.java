import java.util.*;
import com.google.common.base.Strings;
public class Trie {
// methods to support:
// hasExtension(String str)
// getExtension(String str)
// getAllExtentions(int len)
	TrieNode root;
	boolean addAbstract;
	public Trie(boolean addAbstract){
		root = new TrieNode('\u0000', null);
		this.addAbstract = addAbstract;
	}
	void add(String str){
    str += "$";
		if(addAbstract){
			for(int i = 0; i <= str.length(); i++)
				root.add("^"+Strings.repeat("*",i)+str.substring(i));
		} else {
			root.add("^"+str);
		}
	}
	void print(){
		root.print();
	}
	TrieNode root(){
		return root;
	}

}
