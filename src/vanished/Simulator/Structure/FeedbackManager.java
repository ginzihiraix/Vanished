package vanished.Simulator.Structure;

import java.util.Map.Entry;
import java.util.TreeMap;

public class FeedbackManager {

	private TreeMap<Double, FeedbackLog> logs = new TreeMap<Double, FeedbackLog>();

	public void ResetStatisticalParameters() {
		logs.clear();
	}

	public void Add(double price, double quantity) {
		FeedbackLog log = logs.get(price);
		if (log == null) {
			log = new FeedbackLog(price);
			logs.put(price, log);
		}
		log.impressionTotal += 1;
		log.quantityTotal += quantity;
	}

	public FeedbackLog[] CollectResult() {
		int num = logs.size();

		FeedbackLog[] ret = new FeedbackLog[num];
		{
			int index = 0;
			for (Entry<Double, FeedbackLog> e : logs.entrySet()) {
				FeedbackLog log = e.getValue();
				ret[index] = log;
				index++;
			}
		}
		return ret;
	}

	public FeedbackLog[] CollectResultWithEqualImpressionAdjust() {
		FeedbackLog[] ret = this.CollectResult();

		if (ret.length == 0) return ret;

		double totalImp = 0;
		for (FeedbackLog log : ret) {
			totalImp += log.impressionTotal;
		}

		for (FeedbackLog log : ret) {
			log.quantityTotal = log.quantityTotal / log.impressionTotal * totalImp / ret.length;
			log.impressionTotal = totalImp / ret.length;
		}
		return ret;
	}
}
