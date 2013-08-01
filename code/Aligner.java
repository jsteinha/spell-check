import java.util.*;
import fig.basic.LogInfo;
public class Aligner {
	static AlignState align(Params params, String source, Trie dictionary){
		AlignModel model = new AlignModel(params, source, dictionary);
		AbstractAlignment init = new AbstractAlignment(source, 0, dictionary.root());

		// HACK just assume that 99 is an upper bound on the number of grades
		AlignState state = new AlignState(init, 99, model);

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
														   model);
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

  static Alignment argmax(AlignState state){
		ArrayList<WithMass<AbstractAlignment> > candidates = 
			new ArrayList<WithMass<AbstractAlignment>>();
		for(int i = 0; i <= state.maxGrade; i++){
			if(state.finalState[i] != null){
				candidates.addAll(state.finalState[i].tree.flatten(state.model, false));
			}
		}
		WithMass<AbstractAlignment> best = null;
		for(WithMass<AbstractAlignment> wm : candidates){
			if(best == null || wm.logMassLoc > best.logMassLoc){
				best = wm;
			}
		}
		AbstractAlignment cur = best.particle;
    LinkedList<BackPointer> backpointers = new LinkedList<BackPointer>();
    while(cur.pack(state.model).backpointers.size() > 0){
      BackPointer bestBP = null;
			double bestScore = Double.NaN;
      for(BackPointer bp : cur.pack(state.model).backpointers){
				double curScore = state.model.mu(bp.predecessor, bp.predecessor).maxScore
													+ state.model.params.get(bp.alpha, bp.beta);
				if(bestBP == null || curScore > bestScore){
          bestBP = bp;
					bestScore = curScore;
        }
      }
      backpointers.addFirst(bestBP);
      cur = bestBP.predecessor;
    }
    Alignment ret = new Alignment(state.startState);
    for(BackPointer bp : backpointers){
      ret = ret.extend(bp.alpha, bp.beta);
    }
    return ret;
  }

  /*
  static HashMap<String, HashMap<String, Double>> counts(AlignState state, Params params){
    HashMap<String, HashMap<String, Double>> ret = new HashMap<String, HashMap<String, Double>>();
		ArrayList<WithMass<AbstractAlignment> > candidates = 
			new ArrayList<WithMass<AbstractAlignment>>();
		for(int i = 0; i <= state.maxGrade; i++){
			if(state.finalState[i] != null){
				candidates.addAll(state.finalState[i].tree.flatten(state.model, false));
			}
		}
		if(candidates.size() == 0){
			LogInfo.logs("No corrections found");
			return new HashMap<String, HashMap<String, Double> >();
		}
		// make things normalize to 1.0
		double logMassTot = Double.NEGATIVE_INFINITY;
		for(Withmass wm : candidates){
			logMassTot = Util.logPlus(logMassTot, wm.logMassLoc);
		}
		for(WithMass wm : candidates){
			wm.logMassLoc -= logMassTot;
		}

    state.reverse(); // reverse ordering
    boolean initialized = false;
		while(!initialized || state.hasNext()){
			List<WithMass<AbstractAlignment>> beam;
      if(initialized){
        List<AbstractAlignment> beamTmp = state.next();
        beam = new ArrayList<PackedAlignment>();
        for(AbstractAlignment alignment : beamTmp){
          beam.add(alignment.pack());
        }
        //beam = state.next(); // TODO should not truncate here
      } else {
        initialized = true;
        beam = new ArrayList<PackedAlignment>();
        beam.add(state.finalState);
      }
			for(PackedAlignment alignment : beam){
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
 */

}
