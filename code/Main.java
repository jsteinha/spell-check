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
  @Option
  public static boolean printBeam = false;
	@Option(gloss="max transfeme size")
	public static int maxTransfemeSize = 2;
	@Option(gloss="maximum number of training examples")
	public static int maxTrain = 999999;
	@Option(gloss="maximum number of test examples")
	public static int maxTest = 999999;
	@Option(gloss="evaluation file name (train, dev)")
	public static String evalName = "dev";
	@Option(gloss="graph search regularization")
	public static double graphReg = 0.7;

	public static void main(String[] args){
		Execution.run(args, new Main());
	}

	public void run(){ 
		try {
			runWithException();
		} catch(Exception e) {
      e.printStackTrace();
			throw new RuntimeException(e.toString());
		}
	}

	public void runWithException() throws Exception {
		Scanner in = new Scanner(new File("../data/percy/train.dat"));
		List<Example> examples = new ArrayList<Example>();
		int count = 0;
    LogInfo.begin_track("Reading training examples");
		while(in.hasNext() && count++ < maxTrain){
			Example e = new Example("^"+in.next().toLowerCase()+"$", 
                              in.next().toLowerCase());
			LogInfo.logs(e);
			if(e.source.matches("[a-z|$|^]+") && e.target.matches("[a-z]+")){
				examples.add(e);
			}
		}
    LogInfo.end_track();

		Params params = EMLearner.learn(examples);
		params = new AbstractedParams(params);
		LogInfo.begin_track("Final params");
		params.print();
		LogInfo.end_track();

    Trie dictionary = new Trie(true);
    Scanner dict = new Scanner(new File("../data/percy/dict.txt"));
    while(dict.hasNext()){
      String word = dict.next().toLowerCase();
      if(word.matches("[a-z]+")){
        dictionary.add(word);
      }
    }
    Scanner dev = new Scanner(new File("../data/percy/"+evalName+".dat"));
    List<Example> examplesTest = new ArrayList<Example>();
    count = 0;
    while(dev.hasNext() && count++ < maxTest){
      Example e = new Example("^"+dev.next().toLowerCase()+"$", dev.next().toLowerCase());
			if(e.source.matches("[a-z|$|^]+") && e.target.matches("[a-z]+")){
				examplesTest.add(e);
			}
    }
		StatFig accuracy = new StatFig();
    StatFig fallOffBeam = new StatFig();
		StatFig numNull = new StatFig();
		int counter = 0;
    for(Example e : examplesTest){
      LogInfo.logs("correcting %s (target: %s)", e.source, e.target);
      AlignState state = Aligner.align(params, e.source, dictionary);
      Alignment best = Aligner.argmax(state);
			boolean correct = best != null && ("^"+e.target+"$").equals(best.targetPosition.toString());
      LogInfo.logs("best correction: %s (correct=%s)", best, correct);
			accuracy.add(correct);

			// additional info about whether beam search / scoring is working
			LogInfo.begin_track("Comparing to cheater");
			Trie dictCheat = new Trie(false);
			dictCheat.add(e.target);
			AlignStateTrain stateCheat = AlignerTrain.align(params, e.source, dictCheat);
		  AlignmentTrain cheat = AlignerTrain.argmax(stateCheat, params);
			LogInfo.logs("cheater correction: %s", cheat);
			LogInfo.begin_track("best stats");
			double bestScore;
			if(best == null){
				LogInfo.logs("skipping because NULL");
				bestScore = Double.NEGATIVE_INFINITY;
				numNull.add(true);
			} else {
				bestScore = best.score(params);
				numNull.add(false);
			}
			LogInfo.end_track();
			LogInfo.begin_track("cheater stats");
			double cheaterScore = cheat.score(params);
			LogInfo.end_track();
			LogInfo.end_track();
			boolean fellOffBeam = !correct && (cheaterScore > bestScore);
			if(fellOffBeam && Main.printBeam){
				state.printBeams();
			}
      fallOffBeam.add(!correct && (cheaterScore > bestScore));

      // periodically print logging output
			if(++counter % 100 == 0){
				LogInfo.logs("accuracy: %s", accuracy);
        LogInfo.logs("fall off beam: %s", fallOffBeam);
				LogInfo.logs("num null: %s", numNull);
			}
    }
		LogInfo.logs("final accuracy: %s", accuracy);
    LogInfo.logs("final fall off beam: %s", fallOffBeam);
		LogInfo.logs("final num null: %s", numNull);
	}
}
