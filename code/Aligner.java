import java.util.*;
import fig.basic.LogInfo;
public class Aligner {
	static AlignState align(Params params, String source, Trie dictionary){
		AbstractAlignment init = new AbstractAlignment(source, 0, dictionary.root());

		// HACK just assume that 99 is an upper bound on the number of grades
		AlignState state = new AlignState(init, 99);

		while(state.hasNext()){
			ArrayList<AbstractAlignment> beam = state.next();
			for(AbstractAlignment alignment : beam){
				List<TrieNode> extensions = alignment.targetPosition.getAllExtensions(Main.maxTransfemeSize);
				for(TrieNode targetExtension: extensions){
					for(int i = alignment.sourcePosition; i <= source.length() && i <= alignment.sourcePosition+Main.maxTransfemeSize; i++){
						if(i == alignment.sourcePosition && targetExtension == alignment.targetPosition){
							continue; // make sure we have at least one change
						}
						AbstractAlignment newAlignment = 
							alignment.extend(source.substring(alignment.sourcePosition, i),
															 alignment.targetPosition.spanTo(targetExtension),
														   params);
						state.add(newAlignment);
					}
				}
			}
		}
		return state;
	}


	/*static AlignState align(Params params, String source, Trie dictionary){
    //source += "$"; // add end of string character
		PackedAlignment init = new PackedAlignment(source,
                                               params.modelOrder(),
																	             0,
																	             dictionary.root(),
																	             new LinkedList<Integer>(),
																               new LinkedList<Integer>());
    init.score = new Score(0.0, 0.0);
		AlignState state = new AlignState(init, 99);
		while(state.hasNext()){
			List<PackedAlignment> beam = state.next(); // returns truncated beam
			for(PackedAlignment alignment : beam){
				// TODO: want something more efficient for test time
				List<TrieNode> extensions = alignment.targetPosition.getAllExtensions(Main.maxTransfemeSize);
				for(TrieNode targetExtension : extensions){
					for(int i = alignment.sourcePosition; i <= source.length() && i <= alignment.sourcePosition+Main.maxTransfemeSize; i++){
						if(i == alignment.sourcePosition && targetExtension == alignment.targetPosition){
							continue; // make sure we make at least one change
						}
						PackedAlignment newAlignment = 
								alignment.extend(source.substring(alignment.sourcePosition, i),
																 alignment.targetPosition.spanTo(targetExtension),
                                 params);
						state.add(newAlignment);
					}
				}
			}
		}
		return state;
	}*/

  static AbstractAlignment argmax(AlignState state, Params params){
    AbstractAlignment cur = state.finalState;
    LinkedList<BackPointer> backpointers = new LinkedList<BackPointer>();
    while(cur.backpointers.size() > 0){
      BackPointer best = null;
      for(BackPointer bp : cur.backpointers){
        if(best == null || params.score(bp).maxScore > 
                           params.score(best).maxScore){
          best = bp;
        }
      }
      backpointers.addFirst(best);
      cur = best.predecessor;
    }
    AbstractAlignment ret = state.startState;
    for(BackPointer bp : backpointers){
      ret = ret.extend(bp.alpha, bp.beta, null);
    }
    return ret;
  }

  static HashMap<String, HashMap<String, Double>> counts(AlignState state, Params params){
    HashMap<String, HashMap<String, Double>> ret = new HashMap<String, HashMap<String, Double>>();
		if(state.finalState.score.totalScore == Double.NEGATIVE_INFINITY){
			LogInfo.logs("No corrections found");
			return new HashMap<String, HashMap<String, Double> >();
		}
		// make things normalize to 1.0
    state.finalState.score.backward = -state.finalState.score.totalScore;
    state.reverse(); // reverse ordering
    boolean initialized = false;
		while(!initialized || state.hasNext()){
			List<AbstractAlignment> beam;
      if(initialized){
        beam = state.next(); // TODO should not truncate here
      } else {
        initialized = true;
        beam = new ArrayList<AbstractAlignment>();
        beam.add(state.finalState);
      }
			for(AbstractAlignment alignment : beam){
        if(alignment.score.backward == Double.NEGATIVE_INFINITY) continue;
        for(BackPointer bp : alignment.backpointers){
					Double backward = alignment.score.backward + params.get(bp.alpha, bp.beta);
					bp.predecessor.score.combineBackward(backward);
					Double cnt = (Double)Util.get(ret, bp.alpha, bp.beta);
					if(cnt == null){
						cnt = 0.0;
					}
          Double bpScore = bp.predecessor.score.totalScore + backward;
					cnt = cnt + Math.exp(bpScore);
					Util.put(ret, bp.alpha, bp.beta, cnt);
					// TODO possibly want to add in logspace
        }
			}
		}
		return ret;
	}

}
