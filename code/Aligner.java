import java.util.*;
public class Aligner {
	static AlignState align(Params params, String source, Trie dictionary){
		PackedAlignment init = new PackedAlignment(source,
                                               params.modelOrder(),
																	             0,
																	             dictionary.root(),
																	             new LinkedList<Integer>(),
																               new LinkedList<Integer>());
    init.score = new Score(0.0, 0.0);
		AlignState state = new AlignState(init, source.length());
		while(state.hasNext()){
			List<PackedAlignment> beam = state.next(); // returns truncated beam
			for(PackedAlignment alignment : beam){
				// TODO: want something more efficient for test time
				List<TrieNode> extensions = alignment.targetPosition.getAllExtensions();
				for(TrieNode targetExtension : extensions){
					for(int i = alignment.sourcePosition+1; i <= source.length(); i++){
            //System.out.println(targetExtension + " - " + i);
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
	}

  static PackedAlignment argmax(AlignState state, Params params){
    PackedAlignment cur = state.finalState;
    System.out.println("sourcePosition: " + cur.sourcePosition);
    LinkedList<BackPointer> backpointers = new LinkedList<BackPointer>();
    while(cur.backpointers.size() > 0){
      System.out.println("sourcePosition: " + cur.sourcePosition);
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
    PackedAlignment ret = state.startState;
    ret.order = 9999; // TODO fix this, it modifies the state of 
                      // something that might get accessed later
    for(BackPointer bp : backpointers){
      System.out.println("a["+bp.alpha+"]->b["+bp.beta+"]");
      ret = ret.extend(bp.alpha, bp.beta, null);
    }
    return ret;
  }

  static HashMap<String, HashMap<String, Double>> counts(AlignState state, Params params){
    HashMap<String, HashMap<String, Double>> ret = new HashMap<String, HashMap<String, Double>>();
    state.reverse(); // reverse ordering
		while(state.hasNext()){
			List<PackedAlignment> beam = state.next(); // TODO should not truncate here
			for(PackedAlignment alignment : beam){
        for(BackPointer bp : alignment.backpointers){
					Double backward = alignment.score.backward + params.get(bp.alpha, bp.beta);
					bp.predecessor.score.combineBackward(backward);
					Double cnt = (Double)Util.get(ret, bp.alpha, bp.beta);
					if(cnt == null){
						cnt = 0.0;
					}
					cnt = cnt + Math.exp(bp.predecessor.score.totalScore + backward);
					Util.put(ret, bp.alpha, bp.beta, cnt);
					// TODO possibly want to add in logspace
        }
			}
		}
		return ret;
	}

  public static void main(String[] args){
    String source = "bandana";
    String target = "banana";
    Trie dictionary = new Trie();
    Params params = new Params();
    dictionary.add(target);
    AlignState state = Aligner.align(params, source, dictionary);
    PackedAlignment ans = Aligner.argmax(state, params);
    System.out.println(ans);
  }
}
