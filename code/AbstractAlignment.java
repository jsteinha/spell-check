import java.util.*;
import fig.basic.LogInfo;
public class AbstractAlignment implements Comparable extends TreeLike<AbstractAlignment> {
	List<BackPointer> backpointers;

	// information for DP state
  Score score;
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	public AbstractAlignment(String source,
                           int order,
												   int sourcePosition,
												   TrieNode targetPosition,
    this.source = source;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
		backpointers = new LinkedList<BackPointer>();
    score = new Score();
		score.count = targetPosition.count;
	}
	void addBP(BackPointer bp, Params params){
      backpointers.add(bp);
      if(params != null){
        Score curScore = params.score(bp);
        score = score.combine(curScore);
      }
	}
	void addBPs(PackedAlignment other){
		backpointers.addAll(other.backpointers);
    score = score.combine(other.score);
	}
	PackedAlignment extend(String transfemeSource,
                         String transfemeTarget,
                         Params params){
		int newSourcePosition = sourcePosition + 
														transfemeSource.length();
		Assert.assertSubstringEquals(transfemeSource,
																 source,
																 sourcePosition,
																 newSourcePosition);
		
		TrieNode newTargetPosition = 
			targetPosition.getExtension(transfemeTarget);
		Assert.assertNonNull(newTargetPosition);
		
    PackedAlignment ret = new PackedAlignment(
                               source,
															 this.order,
															 newSourcePosition,
															 newTargetPosition);
    ret.addBP(new BackPointer(this,
															transfemeSource,
															transfemeTarget),
							params);
    return ret;
	}

	@Override
	public boolean equals(Object that){
		return this.equals((PackedAlignment)that);
	}
	boolean equals(PackedAlignment that){
		return this.sourcePosition == that.sourcePosition &&
					 this.targetPosition == that.targetPosition;
	}

	// TODO this is probably slow
	@Override
	public int hashCode(){
		return Util.hashList(Util.hashInt(sourcePosition),
												 Util.hashInt(targetPosition.guid)
												).asInt();
	}

  @Override
  public String toString(){
    if(source == null) return "*";
		return source + "=>" + targetPosition.toString();
  }

  @Override
  public int compareTo(Object other){
    return score.totalScore > 
					 ((PackedAlignment)other).score.totalScore ? -1 : 1;
  }

	/* implement TreeLike methods */
	/* Assumptions:
	 * 	rhs will always have the same value 
	 *  of sourcePosition and targetPosition.depth as this
	 */

	public AbstractAlignment max(AbstractAlignment rhs){
		String lhsTarget = targetPosition.toString(),
					 rhsTarget = rhs.targetPosition.toString();
		Assert.assertEquals(lhsTarget.length(), rhsTarget.length());
		int lastUnequal = -1;
		for(int i = 0; i < lhsTarget.length(); i++)
			if(lhsTarget.charAt(i) != rhsTarget.charAt(i))
				lastUnequal = i;
		String maxTarget = "";
		for(int i = 0; i <= lastUnequal; i++)
			maxTarget += "*";
		maxTarget += lhsTarget.substring(lastUnequal+1);
		TrieNode maxTargetPosition = targetPosition.root.getExtension(maxTarget);
		return new AbstractAlignment(source,
																 order,
																 sourcePosition,
																 maxTargetPosition);
	}

	public double logSize(){
		return Math.log(score.count);
	}

	public boolean lessThan(AbstractAlignment rhs){
		String lhsTarget = targetPosition.toString(),
					 rhsTarget = rhs.targetPosition.toString();
		Assert.assertEquals(lhsTarget.length(), rhsTarget.length());
		for(int i = 0; i < lhsTarget.length(); i++){
			if(lhsTarget.charAt(i) != rhsTarget.charAt(i)){
				return rhsTarget.charAt(i) == '*';
			}
		}
		return false;
	}

	public boolean equalTo(AbstractAlignment rhs){
		String lhsTarget = targetPosition.toString(),
					 rhsTarget = rhs.targetPosition.toString();
		Assert.assertEquals(lhsTarget.length(), rhsTarget.length());
		return lhsTarget.equals(rhsTarget);
	}

	public String toString(AbstractAlignment rhs){

	}
}

