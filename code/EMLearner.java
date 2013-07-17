import java.util.*;
public class EMLearner {
  final static int numIters = 5;
  // modifies params based on the examples
  static Params learnOnce(List<Example> examples, Params paramsIn){
    HashMap<String, HashMap<String, Double>> totalCounts = new HashMap<String, HashMap<String, Double>>();
    for(Example e : examples){
      Trie localDict = new Trie();
      localDict.add(e.target);
      AlignState state = Aligner.align(paramsIn, e.source, localDict);
      Util.incrMap(totalCounts, Aligner.counts(state, paramsIn));
			System.out.println("Done with example["+e+"]");
    }
		totalCounts = Util.normalize(totalCounts);
		Params paramsOut = new HardParams(totalCounts);
		System.out.println("====================");
		System.out.println("Intermediate params:");
		System.out.println("====================");
		paramsOut.print();
		return paramsOut;
  }

	static Params learn(List<Example> examples){
		Params params = new Params();
		for(int iter = 0; iter < numIters; iter++)
			params = learnOnce(examples, params);
		return params;
	}


}
