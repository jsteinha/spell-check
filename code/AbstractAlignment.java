import java.util.*;
import fig.basic.LogInfo;
public class AbstractAlignment implements Comparable extends TreeLike<AbstractAlignment> {
  Score score;
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	public AbstractAlignment(String source,
												   int sourcePosition,
												   TrieNode targetPosition,
    this.source = source;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
	}
	AbstractAlignment extend(String transfemeSource,
                         String transfemeTarget,
                         Params params){
		int newSourcePosition = sourcePosition + transfemeSource.length();
		// TODO kill at test time
		Assert.assertSubstringEquals(transfemeSource,
																 source,
																 sourcePosition,
																 newSourcePosition);
		
		TrieNode newTargetPosition = targetPosition.getExtension(transfemeTarget);
		//TODO kill at test time
		Assert.assertNonNull(newTargetPosition);
		
    AbstractAlignment ret = new AbstractAlignment(source,
															 									  newSourcePosition,
															 									  newTargetPosition);
    ret.pack().addBP(new BackPointer(pack(), transfemeSource, transfemeTarget),
									   params);
    return ret;
	}

	PackedAlignment intern = null;
	PackedAlignment pack(){
		if(intern != null){
			return intern;
		}
		intern = PackedAlignment.cache.get(this);
		if(intern == null){
			intern = new PackedAlignment(this);
			PackedAlignment.cache.put(this, intern);
		}
		return intern;
	}

	@Override
	public boolean equals(Object that){
		return this.equals((AbstractAlignment)that);
	}
	boolean equals(AbstractAlignment that){
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

	/*@Override
	public double logSize(){
		return Math.log(score.count);
	}*/

	@Override
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

	@Override
	public boolean equalTo(AbstractAlignment rhs){
		String lhsTarget = targetPosition.toString(),
					 rhsTarget = rhs.targetPosition.toString();
		Assert.assertEquals(lhsTarget.length(), rhsTarget.length());
		return lhsTarget.equals(rhsTarget);
	}

}

