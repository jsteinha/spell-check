import java.util.*;
import fig.basic.LogInfo;
public class PiSystem<E extends TreeLike<E>> {
    Tree<E> tree;
    public PiSystem(Model<E> model, E root){
        tree = new Tree<E>(model, root);
    }
    public PiSystem(Tree<E> tree){
        this.tree = tree;
    }
    void add(E toAdd){
        tree = tree.add(toAdd);
    }

    List<E> getRefinements(){
        List<WithMass<E>> currentStates = tree.flatten();
        List<E> refinements = new ArrayList<E>();
        for(WithMass<E> state : currentStates)
            refinements.addAll(state.particle.getRefinements());
        return refinements;
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
    static <F extends TreeLike<F>> Pair<F> optimalSubtree(Tree<F> ancestor, Tree<F> subtree,
                                                          int childIndex, int numPlacements){

        Wrapper index = new Wrapper(ancestor, subtree, childIndex, numPlacements);
        Pair<F> ans = memoized.get(index);
        if(ans != null) return ans;
        else ans = new Pair<F>(Double.POSITIVE_INFINITY, null);
        if(childIndex == subtree.numChildren()){
            if(numPlacements > 0){
                ans = optimalSubtree(ancestor, subtree, childIndex, 0);
            } else {
                double score = ancestor.model.KL(subtree.state, subtree.state, ancestor.state);
                for(Tree<F> child : subtree.children)
                    score -= ancestor.model.KL(child.state, subtree.state, ancestor.state);
                ans = new Pair<F>(score, new ArrayList<F>());
            }
        } else {
            Tree<F> child = subtree.children.get(childIndex);
            int maxPlacements = Math.min(numPlacements, child.size());
            for(int i = 0; i <= numPlacements; i++){
                Pair<F> ansChild = optimalSubtree(ancestor, child, 0, i);
                Pair<F> ansRest = optimalSubtree(ancestor, subtree, childIndex+1, numPlacements-i);
                if(ansChild.score + ansRest.score < ans.score){
                    double ansScore = ansChild.score + ansRest.score;
                    ArrayList<F> ansList = new ArrayList<F>();
                    ansList.addAll(ansChild.list);
                    ansList.addAll(ansRest.list);
                    ans = new Pair<F>(ansScore, ansList);
                }
                if(i < numPlacements){
                    Pair<F> ansChild2 = optimalSubtree(child, child, 0, i);
                    Pair<F> ansRest2 = optimalSubtree(ancestor, subtree, childIndex+1, numPlacements-i-1);
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

    static <F extends TreeLike<F>> List<Double> inferNew(Model<F> model,
                                                      F root, int numParticles){
        LogInfo.begin_track("PiSystem inference");
        int T = model.T();
        model.init_t();
        List<Double> ret = new ArrayList<Double>();

        LogInfo.begin_track("t=0");
        PiSystem<F> piAll = new PiSystem<F>(model, root);
        for(F state : root.initStates()){
            piAll.add(state);
        }
        ret.add(scoreParticles(piAll.tree.flatten(), model));
        memoized = new HashMap<Wrapper, Pair>();
        piAll.tree.makeGuids(0);
        piAll.tree.print();
        Pair<F> treeAndScore = optimalSubtree(piAll.tree, piAll.tree, 0, numParticles);
        LogInfo.logs("Score after pruning: %.4f", treeAndScore.score);
        PiSystem<F> pi = new PiSystem<F>(model, root);
				// TODO: have a global cache of state->score, so that we don't lose alignments when we 
				// 			 rebuild the tree
        for(F state : treeAndScore.list)
            pi.add(state);
        pi.tree.print();
        LogInfo.end_track();

        for(int t = 1; t < T; t++){
            LogInfo.begin_track("t=%d", t);
            model.increment_t();
            root = root.nextRoot();
            piAll = new PiSystem<F>(model, root);
            for(F state : pi.getRefinements())
                piAll.add(state);
            ret.add(scoreParticles(piAll.tree.flatten(), model));
            memoized = new HashMap<Wrapper, Pair>();
            piAll.tree.makeGuids(0);
            piAll.tree.print();
            treeAndScore = optimalSubtree(piAll.tree, piAll.tree, 0, numParticles);
            pi = new PiSystem<F>(model, root);
            for(F state : treeAndScore.list)
                pi.add(state);
            LogInfo.logs("Score after pruning: %.4f", treeAndScore.score);
            pi.tree.print();
            LogInfo.end_track();
        }
        LogInfo.end_track();
        return ret;
    }
}

class Tree<E extends TreeLike<E>> {
    // invariant 1: y \in C(x) => y.state < x.state
    // invariant 2: y, y' \in C(x) => max(y.state,y'.state) = x.state
    Model<E> model;
    E state;
    List<Tree<E> > children;
    int guid;
    final int size;

    public Tree(Model<E> model, E state){
        this(model, state, new ArrayList<Tree<E> >());
    }
    public Tree(Model<E> model, E state, List<Tree<E> > children){
        int size = 1;
        for(Tree<E> child : children)
            size += child.size;
        this.size = size;
        this.model = model;
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
            //but we might need to update the mass
						//TODO global state cache so we don't have to worry about this
            if(toAdd.massIsCached())
                return setMass(toAdd);
            else
                return this;
        }
        else if(!toAdd.lessThan(state)){
            E parent = state.max(toAdd);
            List<Tree<E> > newChildren = new ArrayList<Tree<E> >();
            if(!parent.equalTo(state)){
                newChildren.add(this);
            } else {
								//TODO figure out what this is doing and possible roll into global cache
                if(state.massIsCached())
                    parent.setLogMass(state.getLogMassLoc(),
                                      state.getLogMassTot());
            }
            if(!parent.equalTo(toAdd)){
                newChildren.add(new Tree<E>(model, toAdd));
            } else {
								//TODO figure out what this is doing and possible roll into global cache
                if(toAdd.massIsCached())
                    parent.setLogMass(toAdd.getLogMassLoc(),
                                      toAdd.getLogMassTot());
            }
            return new Tree<E>(model, parent, newChildren);
        } else {
            for(Tree<E> c : children){
                if(toAdd.max(c.state).lessThan(state)){
                    return setChild(c, c.add(toAdd));
                }
            }
            return addChild(new Tree<E>(model, toAdd));
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
        return new Tree<E>(model, state, newChildren);
    }    

    // NOTE: it's important that the first element of this be the root
    ArrayList<WithMass<E>> flatten(){
        ArrayList<WithMass<E>> ret = flattenHelper();
				double logMassTot = Double.NEGATIVE_INFINITY;
				for(WithMass<E> wm : ret){
					logMassTot = Util.logPlus(logMassTot, wm.logMassLoc);
				}
				for(WithMass<E> wm : ret){
					wm.logMassLoc -= logMassTot;
				}
				return ret;
    }
    ArrayList<WithMass<E>> flattenHelper(){
        ArrayList<WithMass<E>> ret = new ArrayList<WithMass<E>>();
				WithMass<E> wm = new WithMass<E>(state, model.mu(state, state));
        ret.add(wm);
        for(Tree<E> c : children){
						wm.logMassLoc = Util.logMinus(wm.logMassLoc, model.mu(state, c.state));
            ret.addAll(c.flattenHelper());
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
