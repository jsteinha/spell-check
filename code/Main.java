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
	@Option(gloss="whether to use abstract inference")
	public static boolean useAbstract = true;

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
		//ArrayList<Example> examples = Reader.getTrain("../data/percy/train.dat");
		ArrayList<Example> examples = Reader.getTrainChinese("../data/pinyin/train.dat");

		Params params = EMLearner.learn(examples);
		params = new AbstractedParams(params);
		LogInfo.begin_track("Final params");
		params.print();
		LogInfo.end_track();

    Trie dictionary = new Trie(useAbstract);
    //Scanner dict = new Scanner(new File("../data/percy/dict.txt"));
    Scanner dict = new Scanner(new File("../data/pinyin/dict.dat"));
    while(dict.hasNext()){
      String word = dict.next().toLowerCase();
      if(word.matches("[a-z]+")){
        dictionary.add(word);
      }
    }
    //Scanner dev = new Scanner(new File("../data/percy/"+evalName+".dat"));
    Scanner dev = new Scanner(new File("../data/pinyin/train.dat"));
    List<Example> examplesTest = new ArrayList<Example>();
    int count = 0;
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
			Alignment best1 = null;
			AlignmentTrain best2 = null;
			AlignState state1 = null;
			AlignStateTrain state2 = null;
			if(useAbstract){
      	state1 = Aligner.align(params, e.source, dictionary);
      	best1 = Aligner.argmax(state1);
			} else {
				state2 = AlignerTrain.align(params, e.source, dictionary);
				best2 = AlignerTrain.argmax(state2, params);
			}
			boolean correct;
			if(useAbstract){
				correct = best1 != null && ("^"+e.target+"$").equals(best1.targetPosition.toString());
			} else {
				correct = ("^"+e.target+"$").equals(best2.targetPosition.toString());
			}
      LogInfo.logs("best correction: %s (correct=%s)", useAbstract ? best1 : best2, correct);
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
			if((useAbstract? best1 : best2) == null){
				LogInfo.logs("skipping because NULL");
				bestScore = Double.NEGATIVE_INFINITY;
				numNull.add(true);
			} else {
				if(useAbstract){
					bestScore = best1.score(params);
				} else {
					bestScore = best2.score(params);
				}
				numNull.add(false);
			}
			LogInfo.end_track();
			LogInfo.begin_track("cheater stats");
			double cheaterScore = cheat.score(params);
			LogInfo.end_track();
			LogInfo.end_track();
			boolean fellOffBeam = !correct && (cheaterScore > bestScore);
			if(fellOffBeam && Main.printBeam){
				if(useAbstract){
					state1.printBeams();
				} else {
					state2.printBeams();
				}
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
