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
		int count = 0;
		LogInfo.begin_track("Reading training examples (Chinese)");
		while(in.hasNext() && count++ < Main.maxTrain){
			String source = in.next(), target = in.next();
			LogInfo.logs("source: %s", source);
			LogInfo.logs("target: %s", target);
		}
		LogInfo.end_track();
		return null;
	}







}
