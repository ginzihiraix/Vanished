package vanished.Simulator;

public class MovingAverage {
	double total = 0;
	int num = 0;

	public void Add(double x) {
		total += x;
		num++;
	}

	public double ComputeAverage() {
		if (num == 0) return 0;
		double average = total / num;
		return average;
	}

	public double GetTotal() {
		return total;
	}

	public void Clear() {
		total = 0;
		num = 0;
	}
}
