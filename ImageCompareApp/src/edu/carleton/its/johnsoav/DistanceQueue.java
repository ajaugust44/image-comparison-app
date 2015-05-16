package edu.carleton.its.johnsoav;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

public class DistanceQueue extends AbstractQueue<DistanceNode> {

	public static void main(String[] args) {
		System.out.println("Run imageCompare instead");
	}

	/* ** INSTANCE VARIABLES ** */
	PriorityQueue<DistanceNode> queue;
	int k;
	
	
	public DistanceQueue(int k) {
		this.k = k;
		this.queue = new PriorityQueue<DistanceNode>(k + 1, Collections.reverseOrder());
	}
	
	@Override
	public boolean offer(DistanceNode node) {
		boolean res = this.queue.offer(node);
		if (this.queue.size() > this.k) {
			this.queue.poll();
		}
		return res;
	}

	@Override
	public DistanceNode poll() {
		return this.queue.poll();
	}

	@Override
	public DistanceNode peek() {
		return this.queue.peek();
	}

	@Override
	public Iterator<DistanceNode> iterator() {
		return this.queue.iterator();
	}

	@Override
	public int size() {
		return this.queue.size();
	}
	
	public DistanceNode[] toArray() {
		return this.queue.toArray(new DistanceNode[0]);
	}
	
	
}
