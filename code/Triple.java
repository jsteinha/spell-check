import com.google.common.base.Objects;
public class Triple<A, B, C> {
  A a;
  B b;
  C c;
  public Triple(A a, B b, C c){
    this.a = a;
    this.b = b;
    this.c = c;
  }
  static <A, B, C> Triple<A, B, C> makeTriple(A a, B b, C c){
    return new Triple<A, B, C>(a, b, c);
  }
  @Override
  public int hashCode(){
    return Objects.hashCode(a, b, c);
  }
}
