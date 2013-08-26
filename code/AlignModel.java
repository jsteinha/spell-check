import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import java.util.*;
import fig.basic.LogInfo;
public class AlignModel implements Model<AbstractAlignment> {
	final Params params;
	final String source;
	final Trie dictionary;
	HashMap<AbstractAlignment, PackedAlignment> cache;
	HashBasedTable<PackedAlignment, PackedAlignment, Score> muCache;
  HashMap<Triple<PackedAlignment, PackedAlignment, PackedAlignment>, Double> KLCache;
  HashBasedTable<PackedAlignment, PackedAlignment, Edge> edges;
	public AlignModel(Params params, String source, Trie dictionary){
		this.params = params;
		this.source = source;
		this.dictionary = dictionary;
		cache = new HashMap<AbstractAlignment, PackedAlignment>();
		muCache = HashBasedTable.create();
    KLCache = new HashMap<Triple<PackedAlignment, PackedAlignment, PackedAlignment>, Double>();
    edges = HashBasedTable.create();
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

    boolean same = a.pack(this) == b.pack(this);
    /*if(!same){ // add edge from less abstract to more abstract
      edges.put(b.pack(this), a.pack(this), new Edge(0.0, null));
    }*/

		// Finally, recursion
		ans = new Score();
		for(BackPointer bp : b.pack(this).backpointers){
			double last = muLocal(a, bp);
      if(/*same && */bp.beta.indexOf("*") == -1){
        edges.put(bp.predecessor.pack(this), a.pack(this), new Edge(last + (same ? 0.0 : -Main.graphReg), bp));
      }
			Score rest = mu(a.goBack(bp), bp.predecessor);
			ans = ans.combine(rest.increment(last));
		}
		muCache.put(a.pack(this), b.pack(this), ans);
		return ans;
	}

	private double dictionaryScore(String reference, String transfemeTarget, TrieNode prefix){
		double ans = 0.0;
		for(int i = 0; i < transfemeTarget.length(); i++){
      ans += Math.log(prefix.getExtension(transfemeTarget.charAt(i)).count);
      ans -= Math.log(prefix.count);
			prefix = prefix.getExtension(reference.charAt(i));
		}
    if(Double.isNaN(ans)){
      LogInfo.logs("got NaN: %s,%s", transfemeTarget, prefix);
    }
		return ans;
	}
	private double dictionaryScore(AbstractAlignment a, BackPointer bp){
		int begin = bp.predecessor.targetPosition.depth,
				end = a.targetPosition.depth;
    TrieNode prefix;
    if(begin == 0 || a.target.charAt(begin-1) == '*'){
      prefix = getRoot(begin);
    } else {
      prefix = a.goBack(bp).targetPosition; // TODO probably really slow
    }
		return dictionaryScore(a.target.substring(begin,end), bp.beta, prefix);
	}

	double muLocal(AbstractAlignment a, BackPointer bp){
		// Assumption: either bp.beta = ***, or bp.beta is all 
		//             concrete characters. TODO is this assumption accurate?
		// Judges the score of a move given by bp from the perspective of a.
		// If a is concrete, this is just params.get(bp.alpha, bp.beta).
		// Otherwise, we make the approximation that all letters 
		// where a is abstract are independent.

		// First, compute score for the transition
    int begin = bp.predecessor.targetPosition.depth,
        end = a.targetPosition.depth;
    String beta2 = a.target.substring(begin,end);
		double ans = params.get(bp.alpha, beta2);
		
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

	public double KL(AbstractAlignment scope, AbstractAlignment lhs, AbstractAlignment rhs){
		// In general, the only approximation in the model comes from the fact 
		// that we model whether something belongs to the dictionary independently 
		// when it may not, in fact, be independent.
		//
		// General form of input:
		// scope = * * s3 s4 s5 s6
    //   lhs = * * *  s4 s5 s6
		//   rhs = * * *  *  s5 s6
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
    Triple<PackedAlignment, PackedAlignment, PackedAlignment> key = 
      Triple.makeTriple(scope.pack(this), lhs.pack(this), rhs.pack(this));
		Double ans = KLCache.get(key);
		if(ans != null){
			return ans;
		}

		// Finally, recursion
		ans = 0.0;
		for(BackPointer bp : scope.pack(this).backpointers){
      if(muLocal(lhs,bp) == Double.NEGATIVE_INFINITY) continue;
			ans += Math.exp(muLocal(lhs, bp)) * KL(bp.predecessor, lhs.goBack(bp), rhs.goBack(bp));
			ans += Math.exp(mu(lhs, scope).totalScore) * (muLocal(lhs, bp) - muLocal(rhs, bp));
		}
    if(true || ans > 1e-8){
      //LogInfo.logs("KL(%s,%s,%s)=%f [mu(%s,%s)=%f, mu(%s,%s)=%f]", scope, lhs, rhs, ans, lhs, scope, mu(lhs, scope).totalScore, rhs, scope, mu(rhs, scope).totalScore);
    }
		KLCache.put(key, ans);
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
