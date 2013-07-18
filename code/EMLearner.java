import java.util.*;
import fig.basic.LogInfo;
public class EMLearner {

  // modifies params based on the examples
  static Params learnOnce(List<Example> examples, Params paramsIn){
    HashMap<String, HashMap<String, Double>> totalCounts = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> baseCounts = new HashMap<String, Double>();
    for(Example e : examples){
			LogInfo.begin_track("Processing example[%s]", e);
      Trie localDict = new Trie();
      localDict.add(e.target);
      AlignState state = Aligner.align(paramsIn, e.source, localDict);
      PackedAlignment best = Aligner.argmax(state, paramsIn);
      LogInfo.logs("best correction: %s", best);
      Util.incrMap(totalCounts, Aligner.counts(state, paramsIn));
			// get baseline counts
			for(int i = 0; i <= e.source.length(); i++){
				for(int j = 0; j <= Main.maxTransfemeSize; j++){
					if(i+j <= e.source.length()){
						Util.update(baseCounts, e.source.substring(i,i+j), 1.0);
					}
				}
			}
			LogInfo.end_track();
    }
		totalCounts = Util.divide(totalCounts, baseCounts);
		//totalCounts = Util.normalize(totalCounts);
		Params paramsOut = new HardParams(totalCounts);
		LogInfo.begin_track("Intermediate params:");
		paramsOut.print();
		LogInfo.end_track();
		return paramsOut;
  }

	static Params learn(List<Example> examples){
		Params params = new Params();
		for(int iter = 0; iter < Main.numIter; iter++)
			params = learnOnce(examples, params);
		return params;
	}


}
