import java.util.LinkedList;
import java.util.ListIterator;

public class SortedLinkedList<T extends Comparable<T>> extends LinkedList<T> {
	
	public boolean sortedAdd(T elem){
		ListIterator<T> itr = listIterator(0);
		T next;
		while (itr.hasNext()){
			next = itr.next();
			//if next element is larger than new element:
			if (elem.compareTo(next) < 0){
				//insert new element before next
				itr.previous();
				itr.add(elem);
				return true;
			}
		}
		//if above loop finished without breaking, we add elem to end
		itr.add(elem);
		return true;
	}
}
