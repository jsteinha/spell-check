import java.util.*;
import com.google.common.base.Strings;
public class Trie {
// methods to support:
// hasExtension(String str)
// getExtension(String str)
// getAllExtentions(int len)
	TrieNode root;
	public Trie(){
		root = new TrieNode('\u0000', null);
	}
	void add(String str){
		for(int i = 0; i <= str.length(); i++)
			root.add("^"+Strings.repeat("*",i)+str.substring(i));
		//root.add("^"+str);
	}
	void print(){
		root.print();
	}
	TrieNode root(){
		return root;
	}

}
