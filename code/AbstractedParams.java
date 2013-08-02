import com.google.common.base.Strings;
import fig.basic.LogInfo;
public class AbstractedParams extends Params {
	public AbstractedParams(Params params){
    super(); // just initializes weights as an empty Hashmap as of 08/01/2013

		LogInfo.begin_track("Abstracting params");
    for(String index : params.weights.keySet()){
			LogInfo.logs("%s", index);
			int splitIndex = index.indexOf("->");
			String alpha = index.substring(0, splitIndex),
						  beta = index.substring(splitIndex+2);
      for(int i = 0; i <= beta.length(); i++){
        String beta2 = Strings.repeat("*", i)+beta.substring(i);
        update(alpha, beta2, params.get(alpha, beta));
      }
    }
		LogInfo.end_track();
	}
	@Override
	protected double getDefault(String source, String target){
		return Double.NEGATIVE_INFINITY;
	}


}
