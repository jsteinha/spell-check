import java.util.*;
import fig.basic.LogInfo;
public class AlignmentTrain implements Comparable {
	List<BackPointerTrain> backpointers;
	int order;

	// information for DP state
  Score score;
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	LinkedList<Integer> sourceTransfemeBoundaries;
	LinkedList<Integer> targetTransfemeBoundaries;
	public AlignmentTrain(String source,
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
			sourceTransfemeBoundaries.removeFirst();
			targetTransfemeBoundaries.removeFirst();
		}
		backpointers = new LinkedList<BackPointerTrain>();
    score = new Score();
	}
	void addBP(BackPointerTrain bp, Params params){
      backpointers.add(bp);
      if(params != null){
        Score curScore = params.score(bp);
        score = score.combine(curScore);
      }
	}
	void addBPs(AlignmentTrain other){
		backpointers.addAll(other.backpointers);
    score = score.combine(other.score);
	}
	AlignmentTrain extend(String transfemeSource,
                         String transfemeTarget,
                         Params params){
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
    AlignmentTrain ret = new AlignmentTrain(
                               source,
															 this.order,
															 newSourcePosition,
															 newTargetPosition,
															 newSourceBoundaries,
															 newTargetBoundaries);
    ret.addBP(new BackPointerTrain(this, transfemeSource, transfemeTarget), params);
    return ret;
	}

	@Override
	public boolean equals(Object that){
		return this.equals((AlignmentTrain)that);
	}
	boolean equals(AlignmentTrain that){
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

	double score(Params params){
    String target = targetPosition.toString();
		int old_i = 0, old_j = 0;
    double totalScore = 0.0, curScore;
		for(int p = 0; p < sourceTransfemeBoundaries.size(); p++){
			int i = sourceTransfemeBoundaries.get(p),
				  j = targetTransfemeBoundaries.get(p);
			String s = source.substring(old_i, i);
			String t = target.substring(old_j, j);
      curScore = params.get(s, t);
			LogInfo.logs("score of (%s->%s): %f", s, t, curScore);
      old_i = i;
      old_j = j;
      totalScore += curScore;
		}
    LogInfo.logs("total score: %f", totalScore);
    return totalScore;
	}

  @Override
  public int compareTo(Object other){
    return score.totalScore > ((AlignmentTrain)other).score.totalScore ? -1 : 1;
  }
}


