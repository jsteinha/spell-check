import java.util.*;
import fig.basic.LogInfo;
import com.google.common.base.Strings;
public class AlignState {
	//HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >[] beams;
	HashMap<Context, PiSystem<AbstractAlignment> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
	int direction;
  final PackedAlignment finalState;
	public AlignState(AbstractAlignment startState, int maxGrade){
    PackedAlignment.cache.clear(); // TODO: dangerous, means we can only do one 
                                   // alignment at once
		startState.pack();
		startState.intern.score.maxScore = 0.0;
		startState.intern.score.totalScore = 0.0;

    this.finalState = new AbstractAlignment(null, -1, null);
		this.maxGrade = maxGrade;
		beams = new HashMap[maxGrade+1];
		for(int i = 0; i <= maxGrade; i++)
			beams[i] = new HashMap<Context, PiSystem<AbstractAlignment> >();
		curContexts = new LinkedList<Context>();
		curGrade = -1;
		direction = 1;

    add(startState);
	}
	void add(AbstractAlignment alignment){
    if(alignment.pack().score.maxScore == Double.NEGATIVE_INFINITY){
			return;
		}

    if(alignment.sourcePosition == alignment.source.length() &&
			 alignment.targetPosition.c == '$'){
			for(BackPointer bp : alignment.pack().backpointers){
      	finalState.addBP(bp);
			}
      return;
    }

		Context c = new Context(alignment);
		int grade = c.grade();
		PiSystem<AbstractAlignment> existingMap = beams[grade].get(c);
		//HashMap<PackedAlignment, PackedAlignment> existingMap = beams[grade].get(c);

		if(existingMap == null){
      // TODO next few lines are kinda hacky, should consider making 
      //      some variables global
      TrieNode trieRoot = alignment.targetPosition.root;
      String target = "^"+Strings.repeat("*", c.depth-1);
      AbstractAlignment root = new AbstractAlignment(alignment.source,
                                                     c.position,
                                                     trieRoot.getExtension(target));
			existingMap = new PiSystem<AbstractAlignment>(root);
			//existingMap = new HashMap<PackedAlignment, PackedAlignment>();
			beams[grade].put(c, existingMap);
		}
    existingMap.add(alignment);
		/*PackedAlignment existing = existingMap.get(alignment);
		if(existing == null){
			// TODO might be good to put a copy instead, to avoid pointer issues
			existingMap.put(alignment, alignment);
		} else {
			existing.addBPs(alignment);
		}*/
	}
	private boolean okay(){
		if(direction == 1) return curGrade <= maxGrade;
		else return curGrade >= 0;
	}
	private void skipEmpty(){
		while(okay() && curContexts.size() == 0){
			curGrade += direction;
      if(okay()){
			  curContexts = new LinkedList<Context>(beams[curGrade].keySet());
      }
		}
	}
	boolean hasNext(){
		skipEmpty();
		return okay();
	}
  //private static int beamSize = 0;
	ArrayList<AbstractAlignment> next(){
		skipEmpty();
		Context c = curContexts.removeFirst();
    PiSystem<AbstractAlignment> pi = beams[curGrade].get(c);
    ArrayList<AbstractAlignment> beam = PiSystem.prune(model, pi, Main.beamSize);
    return beam;
		/*ArrayList<PackedAlignment> beamFull =
      new ArrayList<PackedAlignment>(beams[curGrade].get(c).keySet());
    Collections.sort(beamFull);
    if(Main.beamSize == 0 || beamFull.size() < Main.beamSize)
      return beamFull;
    else
      return beamFull.subList(0, Main.beamSize);*/
	}

  void printBeams(){
    LogInfo.begin_track("printBeams");
    LogInfo.logs("**NOT YET IMPLEMENTED**");
    LogInfo.end_track();
  }
	/*void printBeams(){
    LogInfo.begin_track("printBeams");
		for(HashMap<Context, HashMap<PackedAlignment, PackedAlignment>> grades : beams){
			for(Context c : grades.keySet()){
				ArrayList<PackedAlignment> beamFull = 
					new ArrayList<PackedAlignment>(grades.get(c).keySet());
				Collections.sort(beamFull);
				LogInfo.begin_track("beam[%d][%c]", c.grade(), c.hashCode());
    		for(PackedAlignment pa : beamFull){
      		LogInfo.logs("%s: %f", pa, pa.score.totalScore);
    		}
				LogInfo.end_track();
			}
		}
    LogInfo.end_track();
	}*/
	void reverse(){
		// TODO maybe add checking to make sure we don't screw up the state?
		direction = -direction;
	}
}

class Context {
	int position, depth;
	//int grade, hash;
	public Context(AbstractAlignment alignment){
		/*grade = alignment.sourcePosition + alignment.targetPosition.depth;
		hash = alignment.sourcePosition;*/
		position = alignment.sourcePosition;
		depth = alignment.targetPosition.depth;
	}
	int grade(){
		return position + depth;
	}
	@Override
	public int hashCode(){
		return position;
	}
	@Override
	public boolean equals(Object that){
		return equals((Context)that);
	}
	boolean equals(Context that){
		return this.hashCode() == that.hashCode();
	}
}
