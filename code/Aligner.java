import java.util.*;
public class Aligner {
	static AlignState align(Params params, String source, Trie dictionary){
		AlignState state = new AlignState(source.length());
		state.add(new PackedAlignment(null,
																  params.modelOrder(),
																	0,
																	dictionary.root(),
																	new LinkedList<Integer>(),
																  new LinkedList<Integer>()));
		while(state.hasNext()){
			List<PackedAlignment> beam = state.next(); // returns truncated beam
			for(PackedAlignment alignment : beam){
				// TODO: want something more efficient for test time
				List<TrieNode> extensions = alignment.targetPosition.getAllExtensions();
				for(TrieNode targetExtension : extensions){
					for(int i = alignment.sourcePosition+1; i < source.length(); i++){
						PackedAlignment newAlignment = 
								alignment.extend(source.substring(alignment.sourcePosition, i),
																 alignment.targetPosition.spanTo(targetExtension));
						beam.add(newAlignment);
					}
				}
			}
		}
		return state;
	}

}

class AlignState {
	HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >[] beams;
	private int curGrade;
	private LinkedList<Context> curContexts;
	final int maxGrade;
	public AlignState(int maxGrade){
		this.maxGrade = maxGrade;
		beams = new HashMap[maxGrade+1];
		for(int i = 0; i <= maxGrade; i++)
			beams[i] = new HashMap<Context, HashMap<PackedAlignment, PackedAlignment> >();
		curContexts = new LinkedList<Context>();
		curGrade = -1;
	}
	void add(PackedAlignment alignment){
		Context c = new Context(alignment);
		int grade = c.grade();
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
			existing.addPredecessors(alignment.predecessors);
		}
	}
	private void skipEmpty(){
		while(curGrade < maxGrade && curContexts.size() == 0){
			curGrade++;
			curContexts = new LinkedList<Context>(beams[curGrade].keySet());
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
