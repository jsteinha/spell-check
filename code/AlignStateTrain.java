import java.util.*;
import fig.basic.LogInfo;
public class AlignStateTrain {
	HashMap<Context, HashMap<AlignmentTrain, AlignmentTrain> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
	int direction;
  final AlignmentTrain startState, finalState;
	public AlignStateTrain(AlignmentTrain startState, int maxGrade){
    this.startState = startState;
    this.finalState = new AlignmentTrain(null, 0, -1, null, null, null);
		this.maxGrade = maxGrade;
		beams = new HashMap[maxGrade+1];
		for(int i = 0; i <= maxGrade; i++)
			beams[i] = new HashMap<Context, HashMap<AlignmentTrain, AlignmentTrain> >();
		curContexts = new LinkedList<Context>();
		curGrade = -1;
		direction = 1;

    add(startState);
	}
	void add(AlignmentTrain alignment){
    if(alignment.score.maxScore == Double.NEGATIVE_INFINITY) return;
		Context c = new Context(alignment);
		int grade = c.grade();
    if(alignment.sourcePosition == alignment.source.length() &&
			 alignment.targetPosition.c == '$'){
      finalState.addBPs(alignment);
    }
		HashMap<AlignmentTrain, AlignmentTrain> existingMap = beams[grade].get(c);
		if(existingMap == null){
			existingMap = new HashMap<AlignmentTrain, AlignmentTrain>();
			beams[grade].put(c, existingMap);
		}
		AlignmentTrain existing = existingMap.get(alignment);
		if(existing == null){
			// TODO might be good to put a copy instead, to avoid pointer issues
			existingMap.put(alignment, alignment);
		} else {
			existing.addBPs(alignment);
		}
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
	List<AlignmentTrain> next(){
		skipEmpty();
		Context c = curContexts.removeFirst();
		ArrayList<AlignmentTrain> beamFull =
      new ArrayList<AlignmentTrain>(beams[curGrade].get(c).keySet());
    Collections.sort(beamFull);
    if(Main.beamSize == 0 || beamFull.size() < Main.beamSize)
      return beamFull;
    else
      return beamFull.subList(0, Main.beamSize);
	}
	void printBeams(){
    LogInfo.begin_track("printBeams");
		for(HashMap<Context, HashMap<AlignmentTrain, AlignmentTrain>> grades : beams){
			for(Context c : grades.keySet()){
				ArrayList<AlignmentTrain> beamFull = 
					new ArrayList<AlignmentTrain>(grades.get(c).keySet());
				Collections.sort(beamFull);
				LogInfo.begin_track("beam[%d][%c]", c.grade(), c.hashCode());
    		for(AlignmentTrain pa : beamFull){
      		LogInfo.logs("%s: %f", pa, pa.score.totalScore);
    		}
				LogInfo.end_track();
			}
		}
    LogInfo.end_track();
	}
	void reverse(){
		// TODO maybe add checking to make sure we don't screw up the state?
		direction = -direction;
	}
}

class Context {
	int grade, hash;
	public Context(AlignmentTrain alignment){
		grade = alignment.sourcePosition + alignment.targetPosition.depth;
		hash = alignment.sourcePosition;
	}
	int grade(){
		return grade;
	}
	@Override
	public int hashCode(){
		return hash;
	}
	@Override
	public boolean equals(Object that){
		return equals((Context)that);
	}
	boolean equals(Context that){
		return this.hashCode() == that.hashCode();
	}
}
