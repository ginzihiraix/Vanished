package vanished.Simulator.Structure;

import java.util.Iterator;
import java.util.LinkedList;

public class HumanExistRecordManager {

	public class HumanExistRecord {
		public long timeStart;
		public long duration;

		public HumanExistRecord(long timeStart, long duration) {
			this.timeStart = timeStart;
			this.duration = duration;
		}

	}

	LinkedList<HumanExistRecord> recordList = new LinkedList<HumanExistRecord>();

	public HumanExistRecordManager() {

	}

	public void Add(long timeStart, long duration) {
		HumanExistRecord r = new HumanExistRecord(timeStart, duration);
		recordList.addLast(r);
	}

	public int MaxNum(long timeStart, long duration) {
		if (recordList.size() == 0) return 0;

		HumanExistRecord r = new HumanExistRecord(timeStart, duration);
		int length = (int) r.duration;
		int[] count = new int[length];
		for (HumanExistRecord r2 : recordList) {
			long s = r.timeStart > r2.timeStart ? r.timeStart : r2.timeStart;
			for (long t = s; t < r2.timeStart + r2.duration; t++) {
				long t2 = t - r.timeStart;
				if (t2 >= r.duration) break;
				count[(int) t2]++;
			}
		}

		int max = 0;
		for (int t = 0; t < r.duration; t++) {
			if (count[t] > max) {
				max = count[t];
			}
		}

		return max;
	}

	public double AverageNum(long timeStart, long duration) {
		if (recordList.size() == 0) return 0;

		HumanExistRecord r = new HumanExistRecord(timeStart, duration);
		int length = (int) r.duration;
		int[] count = new int[length];
		for (HumanExistRecord r2 : recordList) {
			long s = r.timeStart > r2.timeStart ? r.timeStart : r2.timeStart;
			for (long t = s; t < r2.timeStart + r2.duration; t++) {
				long t2 = t - r.timeStart;
				if (t2 >= r.duration) break;
				count[(int) t2]++;
			}
		}

		double sum = 0;
		for (int t = 0; t < r.duration; t++) {
			sum += count[t];
		}

		double average = sum / r.duration;
		return average;
	}

	public void RemoveOld(long timeOld) {
		for (Iterator<HumanExistRecord> it = recordList.iterator(); it.hasNext();) {
			HumanExistRecord r = it.next();
			if (r.timeStart + r.duration < timeOld) {
				it.remove();
			}
		}
	}
}
