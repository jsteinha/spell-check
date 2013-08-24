import java.util.*;
import fig.basic.LogInfo;
public class Alignment {
	String source;
	int sourcePosition;
	TrieNode targetPosition;
	ArrayList<Integer> sourceTransfemeBoundaries;
	ArrayList<Integer> targetTransfemeBoundaries;
	public Alignment(AbstractAlignment example){
		source = example.source;
		sourcePosition = example.sourcePosition;
		targetPosition = example.targetPosition;
		sourceTransfemeBoundaries = new ArrayList<Integer>();
		targetTransfemeBoundaries = new ArrayList<Integer>();
	}
	public Alignment(String source,
									 int sourcePosition,
									 TrieNode targetPosition,
									 ArrayList<Integer> sourceTransfemeBoundaries,
									 ArrayList<Integer> targetTransfemeBoundaries){
    this.source = source;
		this.sourcePosition = sourcePosition;
		this.targetPosition = targetPosition;
		this.sourceTransfemeBoundaries = sourceTransfemeBoundaries;
		this.targetTransfemeBoundaries = targetTransfemeBoundaries;
	}
	Alignment extend(String transfemeSource, String transfemeTarget){
		int newSourcePosition = sourcePosition + transfemeSource.length();
		Assert.assertSubstringEquals(transfemeSource, source, sourcePosition, newSourcePosition);
		
		TrieNode newTargetPosition = targetPosition.getExtension(transfemeTarget);
		if(newTargetPosition == null){
			return null;
		}
		Assert.assertNonNull(newTargetPosition);
		
		ArrayList<Integer> newSourceBoundaries = 
				new ArrayList<Integer>(sourceTransfemeBoundaries);
		ArrayList<Integer> newTargetBoundaries = 
				new ArrayList<Integer>(targetTransfemeBoundaries);
		newSourceBoundaries.add(newSourcePosition);
		newTargetBoundaries.add(newTargetPosition.depth);
    Alignment ret = new Alignment(source,
															 	  newSourcePosition,
															 	  newTargetPosition,
															 	  newSourceBoundaries,
															 	  newTargetBoundaries);
    return ret;
	}

	@Override
	public boolean equals(Object that){
		return this.equals((Alignment)that);
	}
	boolean equals(Alignment that){
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

}

