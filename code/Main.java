import java.util.*;
import java.io.*;
public class Main {
	public static void main(String[] args) throws Exception {
		Scanner in = new Scanner(new File("../data/percy/train.dat"));
		List<Example> examples = new ArrayList<Example>();
		int count = 0;
		while(in.hasNext()){ // && count++ < 1000){
			Example e = new Example(in.next().toLowerCase(), in.next().toLowerCase());
			System.out.println(e);
			if(e.source.matches("[a-z]+") && e.target.matches("[a-z]+")){
				examples.add(e);
			}
		}
		Params params = EMLearner.learn(examples);
		System.out.println("====================");
		System.out.println("    Final params:   ");
		System.out.println("====================");
		params.print();
	}
}
