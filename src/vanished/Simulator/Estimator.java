package vanished.Simulator;

import java.util.ArrayList;

public class Estimator {
	int numMaxSample = 10000;
	double metric;

	ArrayList<DataSample> samples = new ArrayList<DataSample>();

	class DataSample {
		double x;
		double y;

		public DataSample(double y, double x) {
			this.x = x;
			this.y = y;
		}
	}

	public Estimator(int numMaxSample, double metric) {
		this.numMaxSample = numMaxSample;
		this.metric = metric;
	}

	public void AddSample(double y, double x) {
		DataSample ds = new DataSample(y, x);
		samples.add(ds);
		int num = samples.size();
		if (num == numMaxSample + 1) {
			samples.remove(num - 1);
		}
	}

	public double Estimate(double x) {
		double disNearent = Double.MAX_VALUE;
		double yNearest = 0;

		double yTotal = 0;
		double wTotal = 0;
		for (DataSample ds : samples) {
			double def = x - ds.x;
			double dis = def * def * metric;
			if (dis < disNearent) {
				disNearent = dis;
				yNearest = ds.y;
			}
			if (dis > 10) continue;

			double w = Math.exp(-dis);
			yTotal += ds.y * w;
			wTotal += w;
		}

		double yEst;
		if (wTotal == 0) {
			yEst = yNearest;
		} else {
			yEst = yTotal / wTotal;
		}

		return yEst;
	}

	public int GetNumSample() {
		return samples.size();
	}

}
