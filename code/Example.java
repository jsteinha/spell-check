public class Example {
	final String source, target;
	public Example(String source, String target){
		this.source = source;
		this.target = target;
	}
	@Override
	public String toString(){
		return source + " => " + target;
	}
}
