package edu.carleton.its.johnsoav;


class DistanceNode implements Comparable<DistanceNode>{
	public double distance;
	public int id;
	public float[] point1, point2;
	
	public DistanceNode(int id, float[] p1, float[] p2) {
		this.id = id;
		this.point1 = p1;
		this.point2 = p2;
		this.calcDistance();
	}
	
	public int getID() {
		return this.id;
	}
	
	private void calcDistance(){
		// squared Euclidean distance
		
		this.distance = 0;
		for (int i = 0; i < point1.length; i ++) {
			distance += Math.pow((point1[i] - point2[i]), 2);
		}
	}
	
	@Override
	public int compareTo(DistanceNode other) {
		if (this.distance < other.distance) {
			return -1;
		} else if (this.distance == other.distance) {
			return 0;
		} else {
			return 1;
		}
	}
	
}