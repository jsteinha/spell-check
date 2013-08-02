import java.util.*;
import fig.basic.LogInfo;
public class AlignState {
	//HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >[] beams;
	HashMap<Context, PiSystem<AbstractAlignment> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
	int direction;
  final AbstractAlignment startState;
  //final PackedAlignment finalState;
  PiSystem<AbstractAlignment>[] finalState;
  final AlignModel model;
	public AlignState(AbstractAlignment startState, int maxGrade, AlignModel model){
		LogInfo.begin_track("construct AlignState");
    this.startState = startState;
		this.maxGrade = maxGrade;
    this.model = model;

		LogInfo.logs("pack start state");
		startState.pack(model);
		//startState.intern.score.maxScore = 0.0;
		//startState.intern.score.totalScore = 0.0;

    //this.finalState = new PackedAlignment();
    finalState = new PiSystem[maxGrade+1];

		LogInfo.logs("initialize beams");
		beams = new HashMap[maxGrade+1];
		for(int i = 0; i <= maxGrade; i++)
			beams[i] = new HashMap<Context, PiSystem<AbstractAlignment> >();
		curContexts = new LinkedList<Context>();
		curGrade = -1;
		direction = 1;

		LogInfo.logs("add start state");
    add(startState);
		LogInfo.end_track();
	}
	void add(AbstractAlignment alignment){
		LogInfo.begin_track("AlignState.add");
		LogInfo.logs("pack alignment");
		alignment.pack(model); // causes interning and backpointers to happen

		LogInfo.logs("build context");
		Context c = new Context(alignment);
		int grade = c.grade();

		// TODO this should be done with a pi-system
    if(alignment.sourcePosition == alignment.source.length() &&
			 alignment.targetPosition.c == '$'){
			LogInfo.logs("add to finalState");
      if(finalState[grade] == null){
        AbstractAlignment root = new AbstractAlignment(alignment.source,
                                                       c.position,
                                                       model.getRoot(alignment.targetPosition));
        finalState[grade] = new PiSystem<AbstractAlignment>(root);
      }
      finalState[grade].add(alignment);
      return;
    }

		LogInfo.logs("get existing PiSystem");
		PiSystem<AbstractAlignment> existingMap = beams[grade].get(c);

		if(existingMap == null){
			LogInfo.begin_track("found no existing PiSystem, creating new one");
			LogInfo.logs("build root");
      AbstractAlignment root = new AbstractAlignment(alignment.source,
                                                     c.position,
																										 model.getRoot(alignment.targetPosition));
			LogInfo.logs("build PiSystem");
			existingMap = new PiSystem<AbstractAlignment>(root);
			LogInfo.logs("add to map");
			beams[grade].put(c, existingMap);
			LogInfo.end_track();
		}
		LogInfo.logs("add alignment to PiSystem");
    existingMap.add(alignment);
		LogInfo.end_track();
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
	ArrayList<AbstractAlignment> next(){
		skipEmpty();
		Context c = curContexts.removeFirst();
    PiSystem<AbstractAlignment> pi = beams[curGrade].get(c);
    ArrayList<AbstractAlignment> beam = PiSystem.prune(model, pi, Main.beamSize);
    return beam;
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
	public Context(AbstractAlignment alignment){
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
