import java.util.*;
public abstract class TreeLike<E> {
    abstract E max(E rhs);
    abstract boolean lessThan(E rhs);
    abstract boolean equalTo(E rhs);
}
