public class Score {
    double maxScore;
    double totalScore;
		double backward;
		int count;
    public Score(){
      maxScore = Double.NEGATIVE_INFINITY;
      totalScore = Double.NEGATIVE_INFINITY;
			count = 0;
			backward = Double.NEGATIVE_INFINITY;
    }
    public Score(double maxScore, double totalScore){
      this.maxScore = maxScore;
      this.totalScore = totalScore;
			backward = Double.NEGATIVE_INFINITY; // TODO make sure backward score also is preserved
    }
    public Score(double maxScore, double totalScore, int count){
      this(maxScore, totalScore);
      this.count = count;
    }
    Score increment(double delta){
      return new Score(maxScore + delta, totalScore + delta);
    }
    Score combine(Score other){
      return new Score(
          Math.max(maxScore, other.maxScore),
          Util.logsumexp(totalScore, other.totalScore),
					count + other.count);
    }
		void combineBackward(Double toAdd){
			backward = Util.logsumexp(backward, toAdd);
		}
}
