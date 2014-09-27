package vanished.Simulator.Structure;

import java.util.Iterator;
import java.util.LinkedList;

public class HumanExistRecordManager {

	public class HumanExistRecord {
		public long timeStart;
		public long duration;
		public double num;

		public HumanExistRecord(long timeStart, long duration, double num) {
			this.timeStart = timeStart;
			this.duration = duration;
			this.num = num;
		}
	}

	LinkedList<HumanExistRecord> recordList = new LinkedList<HumanExistRecord>();

	public HumanExistRecordManager() {

	}

	public void Add(long timeStart, long duration, double num) {
		HumanExistRecord r = new HumanExistRecord(timeStart, duration, num);
		recordList.addLast(r);
	}

	public double MaxNum(long timeStart, long duration) {
		if (recordList.size() == 0) return 0;

		int length = (int) duration;
		double[] count = new double[length];
		for (HumanExistRecord r2 : recordList) {
			long s = timeStart > r2.timeStart ? timeStart : r2.timeStart;
			for (long t = s; t < r2.timeStart + r2.duration; t++) {
				long t2 = t - timeStart;
				if (t2 >= duration) break;
				count[(int) t2] += r2.num;
			}
		}

		double max = 0;
		for (int t = 0; t < duration; t++) {
			if (count[t] > max) {
				max = count[t];
			}
		}

		return max;
	}

	public double AverageNum(long timeStart, long duration) {
		if (recordList.size() == 0) return 0;

		int length = (int) duration;
		int[] count = new int[length];
		for (HumanExistRecord r2 : recordList) {
			long s = timeStart > r2.timeStart ? timeStart : r2.timeStart;
			for (long t = s; t < r2.timeStart + r2.duration; t++) {
				long t2 = t - timeStart;
				if (t2 >= duration) break;
				count[(int) t2] += r2.num;
			}
		}

		double sum = 0;
		for (int t = 0; t < duration; t++) {
			sum += count[t];
		}

		double average = sum / duration;
		return average;
	}

	public void DiscardOldLog(long timeOld) {
		for (Iterator<HumanExistRecord> it = recordList.iterator(); it.hasNext();) {
			HumanExistRecord r = it.next();
			if (r.timeStart + r.duration < timeOld) {
				it.remove();
			}
		}
	}
}
