import com.google.common.base.Strings;
public class AbstractedParams extends Params {
	public AbstractedParams(Params params){
    super(); // just initializes weights as an empty Hashmap as of 08/01/2013

    for(String index : params.weights.keySet()){
      String[] alphabeta = index.split("->");
      String alpha = alphabeta[0], beta = alphabeta[1];
      for(int i = 0; i <= beta.length(); i++){
        String beta2 = Strings.repeat("*", i)+beta.substring(i);
        update(alpha, beta2, params.get(alpha, beta));
      }
    }

	}


}
