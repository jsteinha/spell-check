import java.util.*;
import fig.basic.LogInfo;
// A PackedAlignment keeps aggregate statistics 
// about multiple AbstractAlignments that are 
// "equivalent" in some way.
public class PackedAlignment {
	// global cache for looking up packed alignments
	static Map<AbstractAlignment, PackedAlignment> cache = 
			new HashMap<AbstractAlignment, PackedAlignment>();

	List<BackPointer> backpointers;
	Score score;
	public PackedAlignment(AbstractAlignment instance){
		// TODO: this is slow, turn off at test time
		if(cache.get(instance) != null){
			throw new RuntimeException("tried to create existing DP state: " + instance);
		}

		backpointers = new LinkedList<BackPointer>();
		score = new Score();
    if(instance != null){
		  score.count = instance.targetPosition.count;
    } else {
      score.count = -1;
    }
	}
	void addBP(BackPointer bp, Params params){
      backpointers.add(bp);
      if(params != null){
        Score curScore = params.score(bp);
        score = score.combine(curScore);
      }
	}
}

