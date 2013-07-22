import java.util.*;
import fig.basic.LogInfo;
public class Params {
	Map<String, Double> weights;
	public Params(){
		weights = new HashMap<String, Double>();
	}
	static String toIndex(String source, String target){
		return StrUtils.join(source, "->", target);
	}
	protected double getDefault(String source, String target){
    if(source.length() > Main.maxTransfemeSize || target.length() > Main.maxTransfemeSize){
      return Double.NEGATIVE_INFINITY;
    }
		if(source.equals(target)){
			return 0.0;
		} else {
			/*double E = 0.0;
			int delta = source.length() - target.length();
			if(delta > 1){
				//E += 1.5 * delta * (delta-1);
			}*/
			return -1.0-2.0 * StrUtils.dist(source, target); //- E;
		}
	}
	double get(String source, String target){
		String index = toIndex(source, target);
		Double wt = weights.get(index);
		if(wt == null){
			wt = getDefault(source, target);
      if(wt > Double.NEGATIVE_INFINITY){
			  weights.put(index, wt);
      }
		}
		return wt;
	}
	void update(String source, String target, double delta){
		double wt = get(source, target);
		String index = toIndex(source, target);
		weights.put(index, wt + delta);
	}
	int modelOrder(){
		return 1;
	}
  Score score(BackPointer bp){
    return bp.predecessor.score.increment(get(bp.alpha, bp.beta));
  }

	void print(){
		LogInfo.logs("size: %d", weights.size());
    LogInfo.begin_track("Params.weights");
		for(String index : weights.keySet()){
			if(weights.get(index) > -10.0){
				LogInfo.logs("%s: %f", index, weights.get(index));
			}
		}
    LogInfo.end_track();
	}
}
