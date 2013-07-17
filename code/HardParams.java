import java.util.*;
class HardParams extends Params {
	public HardParams(Map<String, HashMap<String, Double>> in){
		super();
		for(String a : in.keySet()){
			for(String b : in.get(a).keySet()){
				weights.put(toIndex(a, b), 
					Util.logSafe(in.get(a).get(b)));
			}
		}
	}
	@Override
	protected double getDefault(String source, String taget){
		return Double.NEGATIVE_INFINITY;
	}



}
