public class BackPointer {
  final PackedAlignment predecessor;
  final String alpha, beta;
  public BackPointer(PackedAlignment predecessor, String alpha, String beta){
    this.predecessor = predecessor;
    this.alpha = alpha;
    this.beta = beta;
  }
}
