import java.util.*;
public class PackedAlignment {
	List<BackPointer> backpointers;
	int order;

	// information for DP state
  Score score;
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	LinkedList<Integer> sourceTransfemeBoundaries;
	LinkedList<Integer> targetTransfemeBoundaries;
	public PackedAlignment(String source,
                         int order,
												 int sourcePosition,
												 TrieNode targetPosition,
												 LinkedList<Integer> sourceTransfemeBoundaries,
												 LinkedList<Integer> targetTransfemeBoundaries){
    this.source = source;
		this.order = order;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
		this.sourceTransfemeBoundaries = sourceTransfemeBoundaries;
		this.targetTransfemeBoundaries = targetTransfemeBoundaries;
		while(sourceTransfemeBoundaries != null && 
          sourceTransfemeBoundaries.size() > order){
      //System.out.println("order: " + order);
			sourceTransfemeBoundaries.removeFirst();
			targetTransfemeBoundaries.removeFirst();
		}
		backpointers = new LinkedList<BackPointer>();
    score = new Score();
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
    //System.out.println("[" + source.substring(0, sourcePosition) + "]" + transfemeSource + "--->" + "[" + targetPosition + "]" + transfemeTarget);
		int newSourcePosition = sourcePosition + transfemeSource.length();
		Assert.assertSubstringEquals(transfemeSource, source, sourcePosition, newSourcePosition);
		
		TrieNode newTargetPosition = targetPosition.getExtension(transfemeTarget);
		Assert.assertNonNull(newTargetPosition);
		
		LinkedList<Integer> newSourceBoundaries = 
				new LinkedList<Integer>(sourceTransfemeBoundaries);
		LinkedList<Integer> newTargetBoundaries = 
				new LinkedList<Integer>(targetTransfemeBoundaries);
		newSourceBoundaries.add(newSourcePosition);
		newTargetBoundaries.add(newTargetPosition.depth);
    PackedAlignment ret = new PackedAlignment(
                               source,
															 this.order,
															 newSourcePosition,
															 newTargetPosition,
															 newSourceBoundaries,
															 newTargetBoundaries);
    ret.addBP(new BackPointer(this, transfemeSource, transfemeTarget), params);
    return ret;
	}

	@Override
	public boolean equals(Object that){
		return this.equals((PackedAlignment)that);
	}
	boolean equals(PackedAlignment that){
		return this.sourcePosition == that.sourcePosition &&
					 this.targetPosition == that.targetPosition &&
					 Util.listEquals(this.sourceTransfemeBoundaries, that.sourceTransfemeBoundaries) &&
					 Util.listEquals(this.targetTransfemeBoundaries, that.targetTransfemeBoundaries);
	}
	@Override
	public int hashCode(){
		return Util.hashList(Util.hashInt(sourcePosition),
												 Util.hashInt(targetPosition.guid),
												 Util.hashList(sourceTransfemeBoundaries),
												 Util.hashList(targetTransfemeBoundaries)).asInt();

	}

  @Override
  public String toString(){
    if(source == null) return "*";
    String target = targetPosition.toString();
    String source2 = "", target2 = "";
    int old_i = 0;
    for(Integer i : sourceTransfemeBoundaries){
      source2 += source.substring(old_i, i) + "|";
      old_i = i;
    }
    old_i = 0;
    for(Integer i : targetTransfemeBoundaries){
      target2 += target.substring(old_i, i) + "|";
      old_i = i;
    }
    return source2 + "=>" + target2;
  }
}

