package vanished.Simulator;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.Item.Utility;
import vanished.Simulator.Item.UtilityComparator;

public class UtilityManager {

	public enum UtilityDuration {
		OneDay, OneWeek, OneMonth, ThreeMonth, SixMonth, OneYear, ThreeYear, TenYear
	}

	public enum UtilityDecay {
		one, threequarter, half, quarter, zero
	}

	TreeMap<String, CategoryUtility> categoryUtility = new TreeMap<String, CategoryUtility>();

	// TreeMap<UtilityDuration, UtilityLog> exponentialMovingAverageLog = new TreeMap<UtilityDuration, UtilityLog>();

	// public class UtilityLog {
	// ExponentialMovingAverage utilityAverage = new ExponentialMovingAverage(60 * 24 * 365, false);
	//
	// public UtilityLog() {
	// }
	//
	// public UtilityLog(UtilityLog ul) {
	// this.utilityAverage = new ExponentialMovingAverage(ul.utilityAverage);
	// }
	//
	// public void Put(long timeNow, double utility) {
	// utilityAverage.Add(timeNow, utility);
	// }
	// }

	public class EachConfigUtility {
		Utility utility;

		long timeLast;

		double x = 0;

		public EachConfigUtility(long timeNow, Utility u) {
			this.utility = u;
			timeLast = timeNow;
		}

		public EachConfigUtility(EachConfigUtility ecu) {
			this.utility = ecu.utility;
			this.timeLast = ecu.timeLast;
			this.x = ecu.x;
		}

		private void DecreaseUtility(long timeNow) throws Exception {
			long duration = timeNow - timeLast;
			if (duration < 0) throw new Exception("UtilityManager : duration<0");
			timeLast = timeNow;
			x -= utility.speed * duration;
			if (x < 0) {
				if (utility.negativeDie) {
					throw new HumanSimulationException("UtilityManager : die because negative utility");
				} else {
					x = 0;
				}
			}
		}

		public void AddUtility(long timeNow, double power) throws Exception {
			this.DecreaseUtility(timeNow);
			x += power;
		}

		public double ComputeUtility(long timeNow) throws Exception {
			this.DecreaseUtility(timeNow);
			double u = Math.pow(x, utility.p);
			return u;
		}
	}

	public class CategoryUtility {
		TreeMap<Utility, EachConfigUtility> umap = new TreeMap<Utility, EachConfigUtility>(new UtilityComparator());

		public CategoryUtility() {
		}

		public CategoryUtility(CategoryUtility cu) {
			for (Entry<Utility, EachConfigUtility> e : cu.umap.entrySet()) {
				Utility u = e.getKey();
				EachConfigUtility ecu = e.getValue();
				this.umap.put(u, new EachConfigUtility(ecu));
			}
		}

		public void AddUtility(ArrayList<Utility> utilities, double numConsume, long timeNow) throws Exception {
			for (Utility utility : utilities) {
				EachConfigUtility uec = umap.get(utility);
				if (uec == null) {
					uec = new EachConfigUtility(timeNow, utility);
					umap.put(utility, uec);
				}
				uec.AddUtility(timeNow, utility.power * numConsume);
			}
		}

		public double ComputeUtility(long timeNow) throws Exception {

			// TODO
			// if (umap.size() > 1) {
			// System.out.println("2");
			// for (Entry<Utility, EachConfigUtility> e : umap.entrySet()) {
			// Utility utility = e.getKey();
			// EachConfigUtility uec = e.getValue();
			// double u = uec.ComputeUtility(timeNow);
			// System.out.println(utility.category + ", " + utility.subcategory + ", " + u);
			// }
			// }

			double uTotal = 0;
			for (Entry<Utility, EachConfigUtility> e : umap.entrySet()) {
				EachConfigUtility uec = e.getValue();
				double u = uec.ComputeUtility(timeNow);
				uTotal += u;
			}
			return uTotal;
		}
	}

	public UtilityManager() {
	}

	public UtilityManager(UtilityManager um) {
		for (Entry<String, CategoryUtility> e : um.categoryUtility.entrySet()) {
			String category = e.getKey();
			CategoryUtility cu = e.getValue();
			this.categoryUtility.put(category, new CategoryUtility(cu));
		}

		// for (Entry<UtilityDuration, UtilityLog> e : um.exponentialMovingAverageLog.entrySet()) {
		// UtilityDuration ud = e.getKey();
		// UtilityLog ul = e.getValue();
		// this.exponentialMovingAverageLog.put(ud, new UtilityLog(ul));
		// }
	}

	public double ComputeUtility(long timeNow) throws Exception {
		if (categoryUtility.size() == 0) return 0;

		// TODO
		double p = 1;
		double total = 0;
		for (Entry<String, CategoryUtility> e : this.categoryUtility.entrySet()) {
			CategoryUtility cu = e.getValue();
			double u = cu.ComputeUtility(timeNow);
			double temp = Math.pow(u, p);
			total += temp;
		}
		return total;
	}

	public void AddUtility(ArrayList<Utility> utilities, double numConsume, long timeNow) throws Exception {

		if (utilities.size() == 0) return;

		{
			Utility utility = utilities.get(0);
			CategoryUtility cu = this.categoryUtility.get(utility.category);
			if (cu == null) {
				cu = new CategoryUtility();
				this.categoryUtility.put(utility.category, cu);
			}
			cu.AddUtility(utilities, numConsume, timeNow);
		}

		// {
		// Utility utility = utilities.get(0);
		// UtilityLog ul = this.exponentialMovingAverageLog.get(utility.duration);
		// if (ul == null) {
		// ul = new UtilityLog();
		// this.exponentialMovingAverageLog.put(utility.duration, ul);
		// }
		// double u = this.ComputeUtility(timeNow);
		// ul.Put(timeNow, u);
		// }
	}
}
