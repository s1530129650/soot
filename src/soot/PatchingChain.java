package soot;

import java.util.*;
import soot.util.*;

public class PatchingChain extends AbstractCollection implements Chain {


    private Chain innerChain;
    
    public PatchingChain(Chain aChain)
    {
        innerChain = aChain;
    }

    public boolean add(Object o)
    {
        return innerChain.add(o);
    }

    public void swapWith(Object out, Object in)
    {
        insertBefore(in, out);
        remove(out);
    }

    public void insertAfter(Object toInsert, Object point)
    {
        innerChain.insertAfter(toInsert, point);
    }


    public void insertBefore(Object toInsert, Object point)
    {
        ((Unit) point).redirectJumpsToThisTo((Unit) toInsert);
        innerChain.insertBefore(toInsert, point);
    }

    public boolean follows(Object a, Object b)
    {
        return innerChain.follows(a,b);
    }

    public boolean remove(Object obj)
    {
        boolean res = false;

        if(contains(obj))
        {
            Unit successor;
            
            if((successor = (Unit)getSuccOf(obj)) == null)
                successor = (Unit)getPredOf(obj);
            
            res = innerChain.remove(obj);

            ((Unit)obj).redirectJumpsToThisTo(successor);
        }

        return res;        
    }

    public void addFirst(Object u)
    {
        insertBefore(u, innerChain.getFirst());
    }
    

    public void addLast(Object u)
    {
        innerChain.addLast(u);
    }
    
    public void removeFirst() 
    {
        remove(innerChain.getFirst());
    }
    
    public void removeLast()
    {
        remove(innerChain.getLast());
    }
    
    public Object getFirst(){ return innerChain.getFirst();}
    public Object getLast(){return innerChain.getLast();}
    
    public Object getSuccOf(Object point){return innerChain.getSuccOf(point);}
    public Object getPredOf(Object point){return innerChain.getPredOf(point);}

    private class PatchingIterator implements Iterator
    {
        Iterator innerIterator = null;
        Object lastObject;
        boolean state = false;

        PatchingIterator (Chain innerChain) { innerIterator = innerChain.iterator(); }
        PatchingIterator (Chain innerChain, Object u) { innerIterator = innerChain.iterator(u); }
        PatchingIterator (Chain innerChain, Object head, Object tail) { innerIterator = innerChain.iterator(head, tail); }

        public boolean hasNext() { return innerIterator.hasNext(); }
        public Object next() { lastObject = innerIterator.next(); state = true; return lastObject; }
        public void remove() 
        { 
            if (!state)
                throw new IllegalStateException("remove called before first next() call");

            Unit successor;
            
              if((successor = (Unit)getSuccOf(lastObject)) == null)
                  successor = (Unit)getPredOf(lastObject);
            
            innerIterator.remove();

            ((Unit)lastObject).redirectJumpsToThisTo(successor);
        }
    }

    public Iterator snapshotIterator() 
    {
        List l = new ArrayList(); l.addAll(this);
        return l.iterator();
    }
   
    public Iterator iterator() { return new PatchingIterator(innerChain); }
    public Iterator iterator(Object u) { return new PatchingIterator(innerChain, u); }
    public Iterator iterator(Object head, Object tail) { return new PatchingIterator(innerChain, head, tail); }
    public int size(){return innerChain.size();}       


}
