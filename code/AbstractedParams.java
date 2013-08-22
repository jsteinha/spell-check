import com.google.common.base.Strings;
import fig.basic.LogInfo;
import java.util.*;
public class AbstractedParams extends Params {
	public AbstractedParams(Params params){
    super(); // just initializes weights as an empty Hashmap as of 08/01/2013
    HashMap<String, Double> baseCounts = params.baseCounts;
    HashMap<String, Double> totals = new HashMap<String, Double>();

    LogInfo.begin_track("Computing baseCount totals");
    for(String beta : baseCounts.keySet()){
      for(int i = 0; i <= beta.length(); i++){
        String beta2 = Strings.repeat("*", i)+beta.substring(i);
        if(i>0 && beta.charAt(0) == '^'){
          beta2 = "^" + beta2.substring(1);
        }
        if(!totals.containsKey(beta2)){
          totals.put(beta2, baseCounts.get(beta));
        } else {
          totals.put(beta2, totals.get(beta2) + baseCounts.get(beta));
        }
      }
    }
    LogInfo.end_track();

		LogInfo.begin_track("Abstracting params");
    for(String index : params.weights.keySet()){
			LogInfo.logs("%s", index);
			int splitIndex = index.indexOf("->");
			String alpha = index.substring(0, splitIndex),
						  beta = index.substring(splitIndex+2);
      for(int i = 0; i <= beta.length(); i++){
        String beta2 = Strings.repeat("*", i)+beta.substring(i);
        if(i>0 && beta.charAt(0) == '^'){
          beta2 = "^" + beta2.substring(1);
        }
        /*if(i==beta.length()){
          LogInfo.logs("adding %f to (%s->%s)", params.get(alpha,beta), alpha, beta2);
        }*/
        //update(alpha, beta2, params.get(alpha, beta));
        update(alpha, beta2, params.get(alpha, beta) + Math.log(baseCounts.get(beta)) - Math.log(totals.get(beta2)));
      }
    }

		LogInfo.end_track();
	}
	@Override
	protected double getDefault(String source, String target){
		return Double.NEGATIVE_INFINITY;
	}


}
