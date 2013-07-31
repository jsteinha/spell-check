public interface Model<E> {
    Score mu(E a, E b);
    double KL(E scope, E lhs, E rhs);
    double score(E a);
}
