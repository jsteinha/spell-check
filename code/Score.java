public class Score {
    double maxScore;
    double totalScore;
    public Score(){
      maxScore = Double.NEGATIVE_INFINITY;
      totalScore = Double.NEGATIVE_INFINITY;
    }
    public Score(double maxScore, double totalScore){
      this.maxScore = maxScore;
      this.totalScore = totalScore;
    }
    Score increment(double delta){
      return new Score(maxScore + delta, totalScore + delta);
    }
    Score combine(Score other){
      return new Score(
          Math.max(maxScore, other.maxScore),
          Util.logsumexp(totalScore, other.totalScore));
    }
}
