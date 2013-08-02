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

	private double dictionaryScore(String transfemeTarget, int position){
		TrieNode prefix = getRoot(position);
		int length = transfemeTarget.length(), i = 0;
		double ans = 0.0;
		// First, do independent parts based on which of transfemeTarget is '*'
		while(i < transfemeTarget.length() && transfemeTarget.charAt(i) == '*'){
			ans += Math.log(prefix.getExtension(transfemeTarget.charAt(i)).count);
			ans -= Math.log(prefix.getExtension('*').count);
			prefix = prefix.getExtension('*');
			i++;
		}
		// Next, do the dependent part
		ans += Math.log(prefix.getExtension(transfemeTarget.substring(i)).count);
		ans -= Math.log(prefix.getExtension(Strings.repeat("*", length-1)).count);

		return ans;
	}
	private double dictionaryScore(AbstractAlignment a, BackPointer bp){
		int begin = bp.predecessor.targetPosition.depth,
				end = a.targetPosition.depth;
		return dictionaryScore(a.target.substring(begin,end), begin);
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
		ans += dictionaryScore(a, bp);

		return ans;
	}
	TrieNode getRoot(TrieNode example){
		return getRoot(example.depth);
	}
	TrieNode getRoot(int length){
		if(length == 0){
			return dictionary.root;
		} else {
			return dictionary.root.getExtension("^"+Strings.repeat("*", length-1));
		}
	}

	/*private double logpS(String s, int position){
		return params.get(
	}

	private double logpT(String t, int position){

	}*/

	public double KL(AbstractAlignment scope, AbstractAlignment lhs, AbstractAlignment rhs){
		// In general, the only approximation in the model comes from the fact 
		// that we model whether something belongs to the dictionary independently 
		// when it may not, in fact, be independent.
		//
		// General form of input:
		// scope = * * s3 s4 s5 s6
		//   lhs = * * *  *  s5 s6
    //   rhs = * * *  s4 s5 s6
		// In this case, the KL divergence will be:
		//      p(t1)p(t2)p(t3|s3)p(t4|s4)p(t5|s5)p(t6|s6)
		//       x p(s3)p(s4,s5,s6)log(p(s4,s5,s6)/[p(s4)p(s5,s6)]),
		// where:
		// -p(ti) is the marginal probability of an output transfeme, i.e. 
		//    the sum over all source transfemes at that position of the 
    //    probability of this particular output
    // -p(si) is the marginal probability of a source transfeme
		// 
		// Note that we can determine this purely locally in terms of the 
		// current transfeme, i.e. we just need to multiply the old result by 
		// p(ti|si)p(si|s[1:i-1]) [muLocal], and then add 
		// mu(lhs,scope) x [log p(si|s[1:i-1]) - log phat(si|s[1:i-1])]

		// First, base case
		if(scope.sourcePosition == 0 && scope.targetPosition == dictionary.root()){
			return 0.0;
		}

		// Next, memoization
		Double ans = KLCache.get(scope.pack(this), lhs.pack(this), rhs.pack(this));
		if(ans != null){
			return ans;
		}

		// Finally, recursion
		ans = 0.0; //mu(lhs, scope) * (Math.log(dictionaryScore()-dictionaryScore()));
		for(BackPointer bp : scope.pack(this).backpointers){
			ans += muLocal(lhs, bp) * KL(bp.predecessor, lhs.goBack(bp), rhs.goBack(bp));
			ans += mu(lhs, scope).totalScore * (Math.log(dictionaryScore(rhs, bp))
															 -Math.log(dictionaryScore(lhs, bp)));
		}
		KLCache.put(scope.pack(this), lhs.pack(this), rhs.pack(this), ans);
		return ans;
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
