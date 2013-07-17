import java.util.*;
public class Params {
	Map<String, Double> weights;
	public Params(){
		weights = new HashMap<String, Double>();
	}
	static String toIndex(String source, String target){
		return StrUtils.join(source, "->", target);
	}
	protected double getDefault(String source, String target){
		if(source.equals(target)){
			return -0.1;
		} else {
			return -1.0 * StrUtils.dist(source, target);
		}
	}
	double get(String source, String target){
		String index = toIndex(source, target);
		Double wt = weights.get(index);
		if(wt == null){
			wt = getDefault(source, target);
			weights.put(index, wt);	
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

}
