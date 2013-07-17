import java.util.*;
public class EMLearner {
  final static int numIters = 1;
  // modifies params based on the examples
  static Params learn(List<Example> examples, Params paramsIn){
    Params paramsOut = new HardParams();
    HashMap<String, HashMap<String, Double>> totalCounts = new HashMap<String, HashMap<String, Double>>();
    for(Example e : examples){
      Trie localDict = new Trie();
      localDict.add(e.target);
      AlignState state = Aligner.align(e.source, localDict, paramsIn);
      Util.incrMap(totalCounts, Aligner.counts(state, paramsIn);
    }
    Util.normalize(totalCounts);
    params
  }




}
