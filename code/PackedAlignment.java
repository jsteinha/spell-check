import java.util.*;
public class PackedAlignment {
	List<BackPointer> backpointers;
	int order;

	// information for DP state
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	LinkedList<Integer> sourceTransfemeBoundaries;
	LinkedList<Integer> targetTransfemeBoundaries;
	public PackedAlignment(int order,
												 int sourcePosition,
												 TrieNode targetPosition,
												 LinkedList<Integer> sourceTransfemeBoundaries,
												 LinkedList<Integer> targetTransfemeBoundaries){
		this.order = order;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
		this.sourceTransfemeBoundaries = sourceTransfemeBoundaries;
		this.targetTransfemeBoundaries = targetTransfemeBoundaries;
		while(sourceTransfemeBoundaries.size() > order){
			sourceTransfemeBoundaries.removeFirst();
			targetTransfemeBoundaries.removeFirst();
		}
		backpointers = new LinkedList<BackPointer>();
	}
	void addBP(BackPointer bp){
      backpointers.add(bp);
	}
	void addBPs(List<BackPointer> bps){
		backpointers.addAll(bps);
	}
	PackedAlignment extend(String transfemeSource, String transfemeTarget){
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
															 this.order,
															 newSourcePosition,
															 newTargetPosition,
															 newSourceBoundaries,
															 newTargetBoundaries);
    ret.addBP(new BackPointer(this, transfemeSource, transfemeTarget));
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

}

class BackPointer {
  final PackedAlignment predecessor;
  final String alpha, beta;
  public BackPointer(PackedAlignment predecessor, String alpha, String beta){
    this.predecessor = predecessor;
    this.alpha = alpha;
    this.beta = beta;
  }
}
