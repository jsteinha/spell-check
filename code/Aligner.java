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
          bp.alignment.score.combineBackward(alignment, bp.alpha, bp.beta);
          // TODO also update "ret" here
        }
			}
		}
		return ret;
	}
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

class AlignState {
	HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
  final PackedAlignment startState, finalState;
	public AlignState(PackedAlignment startState, int maxGrade){
    this.startState = startState;
    this.finalState = new PackedAlignment(null, 0, -1, null, null, null);
		this.maxGrade = maxGrade;
		beams = new HashMap[maxGrade+1];
		for(int i = 0; i <= maxGrade; i++)
			beams[i] = new HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >();
		curContexts = new LinkedList<Context>();
		curGrade = -1;

    add(startState);
	}
	void add(PackedAlignment alignment){
		Context c = new Context(alignment);
		int grade = c.grade();
    System.out.println("adding to grade " + grade);
    if(grade == maxGrade && alignment.targetPosition.c == '$'){
      System.out.println("adding...");
      finalState.addBPs(alignment);
    }
		HashMap<PackedAlignment, PackedAlignment> existingMap = beams[grade].get(c);
		if(existingMap == null){
			existingMap = new HashMap<PackedAlignment, PackedAlignment>();
			beams[grade].put(c, existingMap);
		}
		PackedAlignment existing = existingMap.get(alignment);
		if(existing == null){
			// TODO might be good to put a copy instead, to avoid pointer issues
			existingMap.put(alignment, alignment);
		} else {
			existing.addBPs(alignment);
		}
	}
	private void skipEmpty(){
		while(curGrade <= maxGrade && curContexts.size() == 0){
			curGrade++;
      if(curGrade <= maxGrade){
			  curContexts = new LinkedList<Context>(beams[curGrade].keySet());
      }
		}
	}
	boolean hasNext(){
		skipEmpty();
		return curGrade <= maxGrade;
	}
	List<PackedAlignment> next(){
		skipEmpty();
		Context c = curContexts.removeFirst();
		return new LinkedList<PackedAlignment>(beams[curGrade].get(c).keySet());
	}
}

class Context {
	int grade;
	public Context(PackedAlignment alignment){
		grade = alignment.sourcePosition;
	}
	int grade(){
		return grade;
	}
	@Override
	public int hashCode(){
		return grade;
	}
	@Override
	public boolean equals(Object that){
		return equals((Context)that);
	}
	boolean equals(Context that){
		return this.grade == that.grade;
	}
}
