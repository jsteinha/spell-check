import java.util.*;
import java.io.*;
public class Main {
	public static void main(String[] args) throws Exception {
		Scanner in = new Scanner(new File("../data/percy/train.dat"));
		List<Example> examples = new ArrayList<Example>();
		int count = 0;
		while(in.hasNext() && count++ < 10000){
			Example e = new Example(in.next().toLowerCase()+"$", 
                              in.next().toLowerCase());
			System.out.println(e);
			if(e.source.matches("[a-z|$]+") && e.target.matches("[a-z]+")){
				examples.add(e);
			}
		}
		Params params = EMLearner.learn(examples);
		System.out.println("====================");
		System.out.println("    Final params:   ");
		System.out.println("====================");
		params.print();

    Trie dictionary = new Trie();
    Scanner dict = new Scanner(new File("../data/percy/dict.txt"));
    while(dict.hasNext()){
      String word = dict.next().toLowerCase();
      if(word.matches("[a-z]+")){
        dictionary.add(word);
      }
    }
    Scanner testS = new Scanner(new File("../data/percy/test.dat"));
    Scanner testT = new Scanner(new File("../data/percy/test.ans"));
    List<Example> examplesTest = new ArrayList<Example>();
    count = 0;
    while(testS.hasNext() && count++ < 20){
      Example e = new Example(testS.next().toLowerCase(), testT.next().toLowerCase());
			if(e.source.matches("[a-z]+") && e.target.matches("[a-z]+")){
				examplesTest.add(e);
			}
    }
    for(Example e : examplesTest){
      System.out.println("correcting " + e.source + " (target: " + e.target + ")");
      AlignState state = Aligner.align(params, e.source, dictionary);
      PackedAlignment best = Aligner.argmax(state, params);
      System.out.println("best correction: " + best);
    }
	}
}
