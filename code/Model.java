public interface Model<E> {
    double mu(E a, E b);
    double KL(E scope, E lhs, E rhs);
    double score(E a);
    int T();
    void init_t();
    void increment_t();
}
