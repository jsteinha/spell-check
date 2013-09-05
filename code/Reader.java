import java.util.*;
import java.io.*;
import fig.basic.LogInfo;

public class Reader {
	static ArrayList<Example> getTrain(String filename) throws Exception {
		Scanner in = new Scanner(new File(filename));
		ArrayList<Example> examples = new ArrayList<Example>();
		int count = 0;
    LogInfo.begin_track("Reading training examples");
		while(in.hasNext() && count++ < Main.maxTrain){
			Example e = new Example("^"+in.next().toLowerCase()+"$", 
                              in.next().toLowerCase());
			LogInfo.logs(e);
			if(e.source.matches("[a-z|$|^]+") && e.target.matches("[a-z]+")){
				examples.add(e);
			}
		}
    LogInfo.end_track();
		return examples;
	}

	static ArrayList<Example> getTrainChinese(String filename) throws Exception {
		Scanner in = new Scanner(new File(filename));
		ArrayList<Example> examples = new ArrayList<Example>();
		int count = 0;
		LogInfo.begin_track("Reading training examples (Chinese)");
		while(in.hasNext() && count++ < Main.maxTrain){
			String target = in.next(), source = in.next();
			Example e = new Example("^"+source+"$", target);
			LogInfo.logs(e);
			examples.add(e);
			//LogInfo.logs("source: %s", source);
			//LogInfo.logs("target: %s", target);
		}
		LogInfo.end_track();
		return examples;
	}


	static ArrayList<Example> getTest(String filename) throws Exception {
    Scanner dev = new Scanner(new File(filename));
    ArrayList<Example> examplesTest = new ArrayList<Example>();
    int count = 0;
    while(dev.hasNext() && count++ < Main.maxTest){
      Example e = new Example("^"+dev.next().toLowerCase()+"$", dev.next().toLowerCase());
			if(e.source.matches("[a-z|$|^]+") && e.target.matches("[a-z]+")){
				examplesTest.add(e);
			}
    }
    return examplesTest;
	}

	static ArrayList<Example> getTestChinese(String filename) throws Exception {
    Scanner dev = new Scanner(new File(filename));
    ArrayList<Example> examplesTest = new ArrayList<Example>();
    int count = 0;
    while(dev.hasNext() && count++ < Main.maxTest){
      String target = dev.next(), source = dev.next();
		  examplesTest.add(new Example("^"+source+"$", target));
    }
    return examplesTest;
	}

}
