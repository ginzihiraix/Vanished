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
				ret[index] = new FeedbackLog(log.price, log.impressionTotal, log.quantityTotal);
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

		// TODO:スムージングする。
		if (false) {
			int num = ret.length;
			if (num >= 3) {
				double[] value = new double[num];
				for (int i = 0; i < num; i++) {
					value[i] = ret[i].quantityTotal;
				}

				for (int frame = 0; frame < 1; frame++) {
					double[] value2 = new double[num];
					for (int i = 0; i < num; i++) {
						if (i == 0) {
							value2[i] = (value[i] + value[i + 1]) / 2;
						} else if (i == num - 1) {
							value2[i] = (value[i - 1] + value[i]) / 2;
						} else {
							value2[i] = (value[i - 1] + value[i] + value[i + 1]) / 3;
						}
					}
					value = value2;
				}

				for (int i = 0; i < num; i++) {
					ret[i].quantityTotal = value[i];
				}
			}
		}

		return ret;
	}
}
