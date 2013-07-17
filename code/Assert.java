public class Assert {
	static void assertNonNull(Object o){
		if(o == null) throw new RuntimeException("null object reference");
	}
	static void assertSubstringEquals(String target, String source, int start, int end){
		if(end < start || end > source.length() || !target.equals(source.substring(start, end))){
			throw new RuntimeException("failed substring equality: " + target + ", " + source + ", [" + start +", " + end + "]");
		}

	}


}
