public class Score {
    double maxScore;
    double totalScore;
		double backward;
    public Score(){
      maxScore = Double.NEGATIVE_INFINITY;
      totalScore = Double.NEGATIVE_INFINITY;
			backward = Double.NEGATIVE_INFINITY;
    }
    public Score(double maxScore, double totalScore){
      this.maxScore = maxScore;
      this.totalScore = totalScore;
			backward = Double.NEGATIVE_INFINITY; // TODO make sure backward score also is preserved
    }
    Score increment(double delta){
      return new Score(maxScore + delta, totalScore + delta);
    }
    Score combine(Score other){
      return new Score(
          Math.max(maxScore, other.maxScore),
          Util.logsumexp(totalScore, other.totalScore));
    }
		void combineBackward(Double toAdd){
			backward = Util.logsumexp(backward, toAdd);
		}
}
