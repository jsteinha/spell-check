public class StrUtils {
	// TODO this is somewhat slow
	static String join(String... args){
		String ret = "";
		for(String str : args)
			ret += str;
		return ret;
	}

	static int dist(String a, String b){
		if(a.length() > b.length()) return dist(b, a);
		int d = b.length() - a.length();
		for(int i = 0; i < a.length(); i++)
			if(a.charAt(i) != b.charAt(i))
				d++;
		return d;
	}

}
