import java.util.*;
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
		root.add(str);
	}
	void print(){
		root.print();
	}
	TrieNode root(){
		return root;
	}

	public static void main(String[] args){
		Trie trie = new Trie();
		trie.add("abra");
		trie.add("abracadabra");
		trie.add("ahead");
		trie.add("banana");
		trie.add("bandana");
		trie.print();
	}
}
