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
      mp = (Map)o;
    }
    mp.put(kvs[kvs.length-2], kvs[kvs.length-1]);
  }

  static Object get(Map mp, Object... kvs){
    for(int i = 0; i < kvs.length-1; i++){
      Object o = mp.get(kvs[i]);
      if(o == null) return null;
      mp = (Map)o;
    }
    return mp.get(kvs[kvs.length-1]);
  }

	static HashMap<String, HashMap<String, Double> > normalize(HashMap<String, HashMap<String, Double>> in){
		HashMap<String, HashMap<String, Double> > out = new HashMap<String, HashMap<String, Double>>();
		for(String a : in.keySet()){
			Double total = 0.0;
			for(String b : in.get(a).keySet())
				total += in.get(a).get(b);
			total += 3.0; // normalize with pseudocounts
			//if(total <= 1e-7) total += 1e-7;
			HashMap<String, Double> out_a = new HashMap<String, Double>();
			for(String b : in.get(a).keySet())
				out_a.put(b, in.get(a).get(b)/total);
			out.put(a, out_a);
		}
		return out;
	}

	static void incrMap(Map a, Map b){
		for(Object o : b.keySet()){
			Object oa = a.get(o);
			Object ob = b.get(o);
			if(ob instanceof Map){
				if(oa == null){
					oa = new HashMap();
					a.put(o, oa);
				}
				incrMap((Map)oa, (Map)ob);
			} else {
				if(oa == null){
					a.put(o, ob);
				} else {
					a.put(o, (Double)oa + (Double)ob);
				}
			}
		}
	}

	static double logSafe(double x){
		if(x <= 0) return Double.NEGATIVE_INFINITY;
		else return Math.log(x);
	}
}
