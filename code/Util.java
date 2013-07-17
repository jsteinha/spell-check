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
		return Hashing.combineOrdered(hashedInputs);
	}
	static HashCode hashInt(int x){
		return hashFunction.hashLong((long)x);
	}

}
