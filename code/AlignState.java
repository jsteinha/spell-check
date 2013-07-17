import java.util.*;
public class AlignState {
	HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
	int direction;
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
		direction = 1;

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
	private boolean okay(){
		if(direction == 1) return curGrade <= maxGrade;
		else return curGrade >= 0;
	}
	private void skipEmpty(){
		while(okay() && curContexts.size() == 0){
			curGrade += direction;
      if(curGrade <= maxGrade){
			  curContexts = new LinkedList<Context>(beams[curGrade].keySet());
      }
		}
	}
	boolean hasNext(){
		skipEmpty();
		return okay();
	}
	List<PackedAlignment> next(){
		skipEmpty();
		Context c = curContexts.removeFirst();
		return new LinkedList<PackedAlignment>(beams[curGrade].get(c).keySet());
	}
	void reverse(){
		// TODO maybe add checking to make sure we don't screw up the state?
		direction = -direction;
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
