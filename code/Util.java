import java.util.*;
import com.google.common.hash.*;
public class Util {
	static HashFunction hashFunction = Hashing.goodFastHash(32);

	static boolean listEquals(List<Integer> lhs, List<Integer> rhs){
		return lhs.equals(rhs);
	}

	static HashCode hashList(HashCode... inputs){
		return Hashing.combineOrdered(Arrays.asList(inputs));
	}
	static HashCode hashList(List<Integer> inputs){
		List<HashCode> hashedInputs = new LinkedList<HashCode>();
		for(Integer x : inputs)
			hashedInputs.add(hashInt(x));
    hashedInputs.add(hashInt(0)); // make sure we have at least one element
		return Hashing.combineOrdered(hashedInputs);
	}
	static HashCode hashInt(int x){
		return hashFunction.hashLong((long)x);
	}

  static double logsumexp(double x, double y){
    if(x < y) return logsumexp(y, x);
    if(y == Double.NEGATIVE_INFINITY) return x;
    return x + Math.log(1.0 + Math.exp(y-x));
  }

  static void put(Map mp, Object... kvs){
    for(int i = 0; i < kvs.length-2; i++){
      Object o = mp.get(kvs[i]);
      if(o == null){
        o = new HashMap();
        mp.put(kvs[i], o);
      }
      mp = o;
    }
    mp.put(kvs[kvs.length-2], kvs[kvs.length-1]);
  }

  static Object get(Map mp, Object... kvs){
    for(int i = 0; i < kvs.length-1; i++){
      Object o = mp.get(kvs[i]);
      if(o == null) return null;
      mp = o;
    }
    return mp.get(kvs[kvs.length-1]);
  }
}
