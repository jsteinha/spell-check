import java.util.*;
import fig.basic.LogInfo;
public class PiSystem<E extends TreeLike<E>> {
    Tree<E> tree;
    public PiSystem(E root){
        tree = new Tree<E>(root);
    }
    public PiSystem(Tree<E> tree){
        this.tree = tree;
    }
    void add(E toAdd){
        tree = tree.add(toAdd);
    }

    private static <F extends TreeLike<F>> double scoreParticles(List<WithMass<F>> particles,
                                                                 Model<F> model){
        LogInfo.begin_track("Computing score against model");
        double score = 0.0, mass = 0.0;
        for(WithMass<F> particle : particles){
            double particleScore = model.score(particle.particle),
                   particleMass  = Math.exp(particle.logMassLoc);
            if(!Double.isNaN(particleScore)){
                mass += particleMass;
                score += particleScore * particleMass;
						}
        }
        double ret;
        if(mass <= 0.0)
            ret = Double.NaN;
        else
            ret = score / mass;
        LogInfo.logs("score = %f", ret);
        LogInfo.end_track();
        return ret;
    }

    static HashMap<Wrapper, Pair> memoized;
    static <F extends TreeLike<F>> Pair<F> optimalSubtree(Model<F> model, Tree<F> ancestor, Tree<F> subtree,
                                                          int childIndex, int numPlacements){

        Wrapper index = new Wrapper(ancestor, subtree, childIndex, numPlacements);
        Pair<F> ans = memoized.get(index);
        if(ans != null) return ans;
        else ans = new Pair<F>(Double.POSITIVE_INFINITY, null);
        if(childIndex == subtree.numChildren()){
            if(numPlacements > 0){
                ans = optimalSubtree(model, ancestor, subtree, childIndex, 0);
            } else {
                double score = model.KL(subtree.state, subtree.state, ancestor.state);
                for(Tree<F> child : subtree.children)
                    score -= model.KL(child.state, subtree.state, ancestor.state);
                ans = new Pair<F>(score, new ArrayList<F>());
            }
        } else {
            Tree<F> child = subtree.children.get(childIndex);
            int maxPlacements = Math.min(numPlacements, child.size());
            for(int i = 0; i <= numPlacements; i++){
                Pair<F> ansChild = optimalSubtree(model, ancestor, child, 0, i);
                Pair<F> ansRest = optimalSubtree(model, ancestor, subtree, childIndex+1, numPlacements-i);
                if(ansChild.score + ansRest.score < ans.score){
                    double ansScore = ansChild.score + ansRest.score;
                    ArrayList<F> ansList = new ArrayList<F>();
                    ansList.addAll(ansChild.list);
                    ansList.addAll(ansRest.list);
                    ans = new Pair<F>(ansScore, ansList);
                }
                if(i < numPlacements){
                    Pair<F> ansChild2 = optimalSubtree(model, child, child, 0, i);
                    Pair<F> ansRest2 = optimalSubtree(model, ancestor, subtree, childIndex+1, numPlacements-i-1);
                    if(ansChild2.score + ansRest2.score < ans.score){
                        double ansScore = ansChild2.score + ansRest2.score;
                        ArrayList<F> ansList = new ArrayList<F>();
                        ansList.add(child.state);
                        ansList.addAll(ansChild2.list);
                        ansList.addAll(ansRest2.list);
                        ans = new Pair<F>(ansScore, ansList);
                    }
                }
            }
        }
        memoized.put(index, ans);
        return ans;
    }

    static <F extends TreeLike<F>> List<F> prune(Model<F> model,
                                                 PiSystem<F> pi,
                                                 int size){
        memoized = new HashMap<Wrapper, Pair>();
        pi.tree.makeGuids(0);
        pi.tree.print();
        Pair<F> pair = optimalSubtree(model, pi.tree, pi.tree, 0, size);
        return pair.list;
    }
}

class Tree<E extends TreeLike<E>> {
    // invariant 1: y \in C(x) => y.state < x.state
    // invariant 2: y, y' \in C(x) => max(y.state,y'.state) = x.state
    E state;
    List<Tree<E> > children;
    int guid;
    final int size;

    public Tree(E state){
        this(state, new ArrayList<Tree<E> >());
    }
    public Tree(E state, List<Tree<E> > children){
        int size = 1;
        for(Tree<E> child : children)
            size += child.size;
        this.size = size;
        this.state = state;
        this.children = children;
    }

    int makeGuids(int index){
        guid = index++;
        for(Tree<E> c : children)
            index = c.makeGuids(index);
        return index;
    }
    int size(){
        return size;
    }
    int numChildren(){
        return children.size();
    }

    Tree<E> add(E toAdd){
        if(toAdd.equalTo(state)){ // then we can stop here
            return this;
        }
        else if(!toAdd.lessThan(state)){
            E parent = state.max(toAdd);
            List<Tree<E> > newChildren = new ArrayList<Tree<E> >();
            if(!parent.equalTo(state)){
                newChildren.add(this);
            }
            if(!parent.equalTo(toAdd)){
                newChildren.add(new Tree<E>(toAdd));
            }
            return new Tree<E>(parent, newChildren);
        } else {
            for(Tree<E> c : children){
                if(toAdd.max(c.state).lessThan(state)){
                    return setChild(c, c.add(toAdd));
                }
            }
            return addChild(new Tree<E>(toAdd));
        }
    }

    private Tree<E> addChild(Tree<E> newChild){
        return setChild(null, newChild);
    }

    // NOTE: oldChild passed by pointer!
    private Tree<E> setChild(Tree<E> oldChild, Tree<E> newChild){
        List<Tree<E>> newChildren = new ArrayList<Tree<E>>();
        newChildren.add(newChild);
        for(Tree<E> c : children)
            if(c != oldChild)
                newChildren.add(c);
        return new Tree<E>(state, newChildren);
    }    

    // NOTE: it's important that the first element of this be the root
    ArrayList<WithMass<E>> flatten(Model<E> model){
        ArrayList<WithMass<E>> ret = flattenHelper(model);
				double logMassTot = Double.NEGATIVE_INFINITY;
				for(WithMass<E> wm : ret){
					logMassTot = Util.logPlus(logMassTot, wm.logMassLoc);
				}
				for(WithMass<E> wm : ret){
					wm.logMassLoc -= logMassTot;
				}
				return ret;
    }
    ArrayList<WithMass<E>> flattenHelper(Model<E> model){
        ArrayList<WithMass<E>> ret = new ArrayList<WithMass<E>>();
				WithMass<E> wm = new WithMass<E>(state, model.mu(state, state));
        ret.add(wm);
        for(Tree<E> c : children){
						wm.logMassLoc = Util.logMinus(wm.logMassLoc, model.mu(state, c.state));
            ret.addAll(c.flattenHelper(model));
				}
        return ret;
    }

    void print(){
        LogInfo.begin_track("Printing tree");
        print(null);
        LogInfo.end_track();
    }
    void print(E previous){
        LogInfo.logs("%s", state);
        for(Tree<E> c : children)
            c.print(state);
    }
}

class Pair<F> {
    double score;
    ArrayList<F> list;
    public Pair(double score, ArrayList<F> list){
        this.score = score;
        this.list = list;
    }
}

class Wrapper {
    int a, b, c, d, hash;
    public Wrapper(Tree ancestor, Tree subtree, int childIndex, int numPlacements){
        a = ancestor.guid;
        b = subtree.guid;
        c = childIndex;
        d = numPlacements;
        hash = a + (b<<8) + (c<<16) + (d<<24); // WARNING: will collide if tree size is > 256
    }
    @Override
    public boolean equals(Object other){
        Wrapper w = (Wrapper)other;
        return a == w.a && b == w.b && c == w.c && d == w.d;
    }
    @Override
    public int hashCode(){
        return hash;
    }
    @Override
    public String toString(){
        return a+","+b+","+c+","+d;
    }
}
