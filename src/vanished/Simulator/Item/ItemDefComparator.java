package vanished.Simulator.Item;

import java.util.Comparator;

public class ItemDefComparator implements Comparator<ItemDef> {

	@Override
	public int compare(ItemDef o1, ItemDef o2) {
		return o1.GetName().compareTo(o2.GetName());
	}

}
