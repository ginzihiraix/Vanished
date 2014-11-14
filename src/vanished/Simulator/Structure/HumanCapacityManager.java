package vanished.Simulator.Structure;

public class HumanCapacityManager {

	int capacity;

	long timeBase = 0L;
	double[] counter = new double[3];

	public HumanCapacityManager(int capacity) {
		this.capacity = capacity;
	}

	public long Add(long timeStart, long duration, double prob, boolean simulation) throws Exception {

		// ‹L˜^‚·‚éêŠ‚ª–³‚¯‚ê‚ÎL‚°‚éB
		{
			long timeNow = timeStart;
			double amount = duration * prob;
			while (true) {
				if (amount <= 0) break;

				double def;
				if (timeNow < timeBase) {
					throw new Exception("HumanExistRecordManager::Add Error");
				} else if (timeNow < timeBase + counter.length) {
					int index = (int) (timeNow - timeBase);
					def = capacity - counter[index];
				} else {
					def = capacity;
				}

				double incrementFactorAtTime = def < prob ? def : prob;
				double add = incrementFactorAtTime < amount ? incrementFactorAtTime : amount;
				amount -= add;

				timeNow++;
			}

			if (simulation == true) return timeNow;

			long timeEnd = timeNow;

			if (timeEnd > timeBase + counter.length) {
				int length = (int) (timeEnd - timeBase);
				double[] counter2 = new double[length];
				System.arraycopy(this.counter, 0, counter2, 0, counter.length);
				this.counter = counter2;
			}
		}

		// ‘«‚µ‚±‚ŞB
		{
			long timeNow = timeStart;
			double amount = duration * prob;
			while (true) {
				if (amount <= 0) break;

				int index = (int) (timeNow - timeBase);
				double def = capacity - counter[index];

				double incrementFactorAtTime = def < prob ? def : prob;
				double add = incrementFactorAtTime < amount ? incrementFactorAtTime : amount;

				counter[index] += add;
				amount -= add;

				timeNow++;
			}
			return timeNow;
		}

	}

	public void DiscardOldLog(long timeOld) {

		int offset = (int) (timeOld - timeBase);
		if (offset > 0) {
			int length = (int) (timeBase + counter.length - timeOld);
			if (length > 0) {
				double[] counter2 = new double[length];
				System.arraycopy(this.counter, offset, counter2, 0, length);
				timeBase = timeOld;
			} else {
				counter = new double[10];
				timeBase = timeOld;
			}
		}
	}
}
