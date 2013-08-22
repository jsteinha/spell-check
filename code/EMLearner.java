import java.util.*;
import fig.basic.LogInfo;
public class EMLearner {

  // modifies params based on the examples
  static Params learnOnce(List<Example> examples, Params paramsIn){
    HashMap<String, HashMap<String, Double>> totalCounts = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> baseCounts = new HashMap<String, Double>();
    for(Example e : examples){
			LogInfo.begin_track("Processing example[%s]", e);
      Trie localDict = new Trie(false);
      localDict.add(e.target);
      AlignStateTrain state = AlignerTrain.align(paramsIn, e.source, localDict);
      AlignmentTrain best = AlignerTrain.argmax(state, paramsIn);
      LogInfo.logs("best correction: %s", best);
      HashMap<String, HashMap<String, Double>> counts = AlignerTrain.counts(state, paramsIn);
      /*LogInfo.begin_track("count updates");
      Util.printMap(counts);
      LogInfo.end_track();*/
      Util.incrMap(totalCounts, counts); //Aligner.counts(state, paramsIn));
			// get baseline counts

      //String baseStr = e.source;
			String baseStr = "^"+e.target+"$";

			for(int i = 0; i <= baseStr.length(); i++){
				for(int j = 0; j <= Main.maxTransfemeSize; j++){
					if(i+j <= baseStr.length()){
						Util.update(baseCounts, baseStr.substring(i,i+j), 1.0);
					}
				}
			}
			LogInfo.end_track();
    }
		totalCounts = Util.divide2(totalCounts, baseCounts);
		//totalCounts = Util.normalize(totalCounts);
		Params paramsOut = new HardParams(totalCounts, baseCounts);
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
