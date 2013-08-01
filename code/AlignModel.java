import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import java.util.*;
public class AlignModel implements Model<AbstractAlignment> {
	final Params params;
	final String source;
	final Trie dictionary;
	HashMap<AbstractAlignment, PackedAlignment> cache;
	HashBasedTable<PackedAlignment, PackedAlignment, Score> muCache;
	public AlignModel(Params params, String source, Trie dictionary){
		this.params = params;
		this.source = source;
		this.dictionary = dictionary;
		cache = new HashMap<AbstractAlignment, PackedAlignment>();
		muCache = HashBasedTable.create();
	}
	public Score mu(AbstractAlignment a, AbstractAlignment b){
		// First, base case
		if(a.sourcePosition == 0 && a.targetPosition == dictionary.root()){
			return new Score(0.0, 0.0, 1);
		}

		// Next, memoization
		Score ans = muCache.get(a.pack(this),b.pack(this));
		if(ans != null){
			return ans;
		}

		// Finally, recursion
		ans = new Score();
		for(BackPointer bp : b.pack(this).backpointers){
			double last = muLocal(a, bp);
			Score rest = mu(a.goBack(bp), bp.predecessor);
			ans = ans.combine(rest.increment(last));
		}
		muCache.put(a.pack(this), b.pack(this), ans);
		return ans;
	}
	double muLocal(AbstractAlignment a, BackPointer bp){
		// Assumption: either bp.beta = ***, or bp.beta is all 
		//             concrete characters.
		// Judges the score of a move given by bp from the perspective of a.
		// If a is concrete, this is just params.get(bp.alpha, bp.beta).
		// Otherwise, we make the approximation that all letters 
		// where a is abstract are independent.

		// First, compute score for the transition
		double ans = params.get(bp.alpha, bp.beta);
		
		// Second, compute the dictionary score
		int begin = bp.predecessor.targetPosition.depth,
				end = a.targetPosition.depth;
		int pos = begin;
	  TrieNode prefix = dictionary.root().getExtension("^"+Strings.repeat("*", begin-1));
		while(pos < end && a.target.charAt(pos) == '*'){
			ans += Math.log(prefix.getExtension(""+bp.beta.charAt(pos-begin)).count);
			ans -= Math.log(prefix.getExtension("*").count);
			prefix = prefix.getExtension("*");
			pos++;
		}
		ans += Math.log(prefix.getExtension(bp.beta.substring(pos-begin)).count);
		ans -= Math.log(prefix.getExtension(Strings.repeat("*", end-pos)).count);
		return ans;
	}
	TrieNode getRoot(TrieNode example){
		return dictionary.root.getExtension("^"+Strings.repeat("*", example.depth-1));
	}

	public double KL(AbstractAlignment scope, AbstractAlignment lhs, AbstractAlignment rhs){
		throw new RuntimeException("not yet implemented");
	}

	private String truth;
	void setTruth(String truth){
		this.truth = truth;
	}
	public double score(AbstractAlignment a){
		if(a.sourcePosition != a.source.length() ||
			 a.targetPosition.c != '$' ||
			 a.target.contains("*")){
			return Double.NaN;
		} else {
			return a.target.equals(truth) ? 1.0 : 0.0;
		}
	}
}
