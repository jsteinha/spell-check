import java.util.*;
import fig.basic.LogInfo;
// A PackedAlignment keeps aggregate statistics 
// about multiple AbstractAlignments that are 
// "equivalent" in some way.
public class PackedAlignment {
	List<BackPointer> backpointers;
	public PackedAlignment(){
		backpointers = new LinkedList<BackPointer>();
	}
	void addBP(BackPointer bp){
      backpointers.add(bp);
	}
}

