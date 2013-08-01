public class BackPointerTrain {
  final AlignmentTrain predecessor;
  final String alpha, beta;
  public BackPointerTrain(AlignmentTrain predecessor, String alpha, String beta){
    this.predecessor = predecessor;
    this.alpha = alpha;
    this.beta = beta;
  }
}
