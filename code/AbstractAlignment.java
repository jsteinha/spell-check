import java.util.*;
import fig.basic.LogInfo;
public class AbstractAlignment extends TreeLike<AbstractAlignment> {
  Score score;
	String source, target;
	int sourcePosition;
	TrieNode targetPosition;
	public AbstractAlignment(String source,
												   int sourcePosition,
												   TrieNode targetPosition){
    this.source = source;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
		// TODO currently here for convenience, may need to fix later 
		//      for performance reasons
		this.target = targetPosition.toString();
	}
	AbstractAlignment extend(String transfemeSource,
                           String transfemeTarget,
                           AlignModel model){
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
    ret.pack(model).addBP(new BackPointer(this, transfemeSource, transfemeTarget));
    return ret;
	}

	AbstractAlignment goBack(BackPointer bp){
		int newSourcePosition = sourcePosition - bp.alpha.length();
		TrieNode newTargetPosition = targetPosition;
		for(int i = 0; i < bp.beta.length(); i++)
			newTargetPosition = newTargetPosition.parent;
		return new AbstractAlignment(source, newSourcePosition, newTargetPosition);
	}

	PackedAlignment intern = null;
	PackedAlignment pack(AlignModel model){
		if(intern != null){
			return intern;
		}
		intern = model.cache.get(this);
		if(intern == null){
			intern = new PackedAlignment();
		  model.cache.put(this, intern);
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
		return source.substring(0,sourcePosition) + "=>" + targetPosition.toString();
  }

	/* implement TreeLike methods */
	/* Assumptions:
	 * 	rhs will always have the same value 
	 *  of sourcePosition and targetPosition.depth as this
	 */

	public AbstractAlignment max(AbstractAlignment rhs){
    //LogInfo.begin_track("max(%s,%s)", this, rhs);
		String lhsTarget = targetPosition.toString(),
					 rhsTarget = rhs.targetPosition.toString();
		Assert.assertEquals(lhsTarget.length(), rhsTarget.length());
		int lastUnequal = -1;
		for(int i = 0; i < lhsTarget.length(); i++)
			if(lhsTarget.charAt(i) != rhsTarget.charAt(i))
				lastUnequal = i;
		String maxTarget = "";
		for(int i = 0; i <= lastUnequal; i++){
      if(i==0){
        maxTarget += "^";
      } else {
			  maxTarget += "*";
      }
    }
		maxTarget += lhsTarget.substring(lastUnequal+1);
    //LogInfo.logs("maxTarget: %s", maxTarget);
		TrieNode maxTargetPosition = targetPosition.root.getExtension(maxTarget);
    //LogInfo.end_track();
		return new AbstractAlignment(source,
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
		//for(int i = 0; i < lhsTarget.length(); i++){
		for(int i = lhsTarget.length()-1; i >= 0; i--){
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

