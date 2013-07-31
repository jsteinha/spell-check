public class BackPointer {
  final AbstractAlignment predecessor;
  final String alpha, beta;
  public BackPointer(AbstractAlignment predecessor, String alpha, String beta){
    this.predecessor = predecessor;
    this.alpha = alpha;
    this.beta = beta;
  }
}
