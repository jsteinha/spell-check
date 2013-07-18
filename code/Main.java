import java.util.*;
import java.io.*;
import fig.basic.Option;
import fig.exec.Execution;
import fig.basic.LogInfo;
import fig.basic.StatFig;
public class Main implements Runnable {
	@Option(gloss="experiment name (for easier tracking")
	public static String experimentName = "NONE";
	@Option(gloss="number of EM iterations")
	public static int numIter = 1;
	@Option(gloss="beam size (0 = infinite beam)")
	public static int beamSize = 0;
	@Option(gloss="max transfeme size")
	public static int maxTransfemeSize = 2;
	@Option(gloss="maximum number of training examples")
	public static int maxTrain = 999999;
	@Option(gloss="maximum number of test examples")
	public static int maxTest = 999999;

	public static void main(String[] args){
		Execution.run(args, new Main());
	}

	public void run(){ 
		try {
			runWithException();
		} catch(Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	public void runWithException() throws Exception {
		Scanner in = new Scanner(new File("../data/percy/train.dat"));
		List<Example> examples = new ArrayList<Example>();
		int count = 0;
    LogInfo.begin_track("Reading training examples");
		while(in.hasNext() && count++ < maxTrain){
			Example e = new Example(in.next().toLowerCase()+"$", 
                              in.next().toLowerCase());
			LogInfo.logs(e);
			if(e.source.matches("[a-z|$]+") && e.target.matches("[a-z]+")){
				examples.add(e);
			}
		}
    LogInfo.end_track();

		Params params = EMLearner.learn(examples);
		LogInfo.begin_track("Final params");
		params.print();
		LogInfo.end_track();

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
    while(testS.hasNext() && count++ < maxTest){
      Example e = new Example(testS.next().toLowerCase()+"$", testT.next().toLowerCase());
			if(e.source.matches("[a-z|$]+") && e.target.matches("[a-z]+")){
				examplesTest.add(e);
			}
    }
		StatFig accuracy = new StatFig();
    for(Example e : examplesTest){
      LogInfo.logs("correcting %s (target: %s)", e.source, e.target);
      AlignState state = Aligner.align(params, e.source, dictionary);
      PackedAlignment best = Aligner.argmax(state, params);
			boolean correct = (e.target+"$").equals(best.targetPosition.toString());
      LogInfo.logs("best correction: %s (correct=%s)", best, correct);
			accuracy.add(correct);
    }
		LogInfo.logs("accuracy: %s", accuracy);
	}
}
