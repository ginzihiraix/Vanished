package vanished.Simulator.Item;

import java.util.Comparator;

public class UtilityComparator implements Comparator<Utility> {

	@Override
	public int compare(Utility u1, Utility u2) {
		int ret;
		ret = u1.category.compareTo(u2.category);
		if (ret == 0) {
			ret = u1.subcategory.compareTo(u2.subcategory);
			if (ret == 0) {
				ret = u1.duration.compareTo(u2.duration);
				if (ret == 0) {
					ret = u1.decay.compareTo(u2.decay);
					if (ret == 0) {
						return 0;
					} else {
						return ret;
					}
				} else {
					return ret;
				}
			} else {
				return ret;
			}
		} else {
			return ret;
		}
	}
}