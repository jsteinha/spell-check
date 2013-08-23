import java.util.*;
import fig.basic.LogInfo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
public class Aligner {
	static AlignState align(Params params, String source, Trie dictionary){
		LogInfo.begin_track("align");
		LogInfo.logs("building model...");
		AlignModel model = new AlignModel(params, source, dictionary);
		LogInfo.logs("building initial alignment...");
		AbstractAlignment init = new AbstractAlignment(source, 0, dictionary.root());

		// HACK just assume that 99 is an upper bound on the number of grades
		LogInfo.logs("building align state...");
		AlignState state = new AlignState(init, 99, model);

		LogInfo.begin_track("starting BFS");
		while(state.hasNext()){
      //LogInfo.logs("getting next beam");
			ArrayList<AbstractAlignment> beam = state.next();
      //LogInfo.logs("beam size: %d", beam.size());
      //LogInfo.begin_track("extending alignments");
			for(AbstractAlignment alignment : beam){
				List<TrieNode> extensions = alignment.targetPosition.getAllExtensions(Main.maxTransfemeSize);
        //LogInfo.logs("%d extensions", extensions.size());
				for(TrieNode targetExtension: extensions){
					for(int i = alignment.sourcePosition; i <= source.length() && i <= alignment.sourcePosition+Main.maxTransfemeSize; i++){
						if(i == alignment.sourcePosition && targetExtension == alignment.targetPosition){
							continue; // make sure we have at least one change
						}
            String tSource = source.substring(alignment.sourcePosition, i);
            String tTarget = alignment.targetPosition.spanTo(targetExtension);
            if(params.get(tSource, tTarget) == Double.NEGATIVE_INFINITY) continue;
						AbstractAlignment newAlignment = 
							alignment.extend(tSource, tTarget, model);
						state.add(newAlignment);
					}
				}
			}
      //LogInfo.end_track();
		}
    LogInfo.end_track();
		LogInfo.end_track();
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

    HashMap<PackedAlignment, PackedAlignment> preds = new HashMap<PackedAlignment, PackedAlignment>();
    HashMap<PackedAlignment, BackPointer> predBPs = new HashMap<PackedAlignment, BackPointer>();
    HashMap<PackedAlignment, Double> dists = new HashMap<PackedAlignment, Double>();
    HashMap<PackedAlignment, Integer> inDegree = new HashMap<PackedAlignment, Integer>();
    AlignModel model = state.model;
    HashBasedTable<PackedAlignment, PackedAlignment, Edge> edges = model.edges;

    LogInfo.logs("Computing inDegrees: %d edges total", edges.size());
    for(Table.Cell<PackedAlignment, PackedAlignment, Edge> cell : edges.cellSet()){
      PackedAlignment v = cell.getColumnKey();
      Integer cur = inDegree.get(v);
      if(cur == null){
        inDegree.put(v, 1);
      } else {
        inDegree.put(v, cur+1);
      }
    }

    PackedAlignment start = state.startState.pack(model);
    LinkedList<PackedAlignment> queue = new LinkedList<PackedAlignment>();

    dists.put(start, 0.0);
    queue.addLast(start);

    /*for(WithMass<AbstractAlignment> wm : candidates){
      PackedAlignment target = wm.particle.pack(model);
      dists.put(target, 0.0);
      queue.addLast(target);
    }*/

    LogInfo.begin_track("Graph search");
    while(queue.size() > 0){
      //LogInfo.logs("size: %d", queue.size());
      PackedAlignment u = queue.removeFirst();
      //LogInfo.logs("u: %s (size=%d)", u, queue.size());
      Double dist = dists.get(u);
      for(PackedAlignment v : edges.row(u).keySet()){
        Edge e = edges.get(u,v);
        Double oldDist = dists.get(v);
        // note that edge weights are all negative
        if(oldDist == null || dist+e.weight > oldDist){
          dists.put(v, dist+e.weight);
          preds.put(v, u);
          predBPs.put(v, e.label);
          if(e.label == null){
            //LogInfo.logs("adding projection");
          } else {
            //LogInfo.logs("adding %s [%s->%s]", e.label.predecessor, e.label.alpha, e.label.beta);
          }
        }
        inDegree.put(v, inDegree.get(v)-1);
        if(inDegree.get(v) == 0){
          queue.addLast(v);
        }
      }
    }
    LogInfo.end_track();

    PackedAlignment best = null;
    Double bestDist = null;
    for(WithMass<AbstractAlignment> wm : candidates){
      PackedAlignment target = wm.particle.pack(model);
      if(dists.get(target) == null) continue;
      if(best == null || dists.get(target) > bestDist){
        best = target;
        bestDist = dists.get(target);
      }
    }
    if(bestDist == null){
      LogInfo.logs("ERROR: no path found to target");
      return null;
    }
    
		/*WithMass<AbstractAlignment> best = null;
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
    }*/

    PackedAlignment cur = best;
    LinkedList<BackPointer> bps = new LinkedList<BackPointer>();
    while(preds.get(cur) != null){
      PackedAlignment next = preds.get(cur);
      BackPointer bp = predBPs.get(cur);
      if(bp != null){
        bps.add(bp);
        LogInfo.logs("adding backpointer: %s", bp);
      }
      cur = next;
    }
    Alignment ret = new Alignment(state.startState);
    while(bps.size() > 0){
      BackPointer bp = bps.removeLast();
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
