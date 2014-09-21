package vanished.Simulator;

import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class Inventory {

	double capacity = Double.MAX_VALUE;

	TreeMap<ItemDef, Item> storage = new TreeMap<ItemDef, Item>(new ItemDefComparator());

	double weightTotal = 0;

	public Inventory() {
	}

	public Inventory(double capacity) {
		this.capacity = capacity;
	}

	public Inventory(Inventory inventory) {
		this.capacity = inventory.capacity;

		for (Entry<ItemDef, Item> e : inventory.storage.entrySet()) {
			ItemDef itemDef = e.getKey();
			Item item = e.getValue();

			Item item2 = new Item(item);

			storage.put(itemDef, item2);
		}
	}

	public double GetCapacity() {
		return this.capacity;
	}

	public double GetTotalWeight() {
		return this.weightTotal;
	}

	public void Put(Item item) throws HumanSimulationException {
		if (item.GetQuantity() == 0) return;

		double weight = item.GetTotalWeight();
		if (weightTotal + weight > capacity) throw new HumanSimulationException("Inventry : weightTotal > capacity");

		weightTotal += weight;

		ItemDef itemDef = item.GetItemDef();

		Item item2 = storage.get(itemDef);
		if (item2 == null) {
			item2 = new Item(itemDef);
			storage.put(itemDef, item2);
		}

		int q = item2.GetQuantity() + item.GetQuantity();
		item2.SetQuantity(q);
	}

	public void PutTest(Item item) throws HumanSimulationException {
		if (item.GetQuantity() == 0) return;

		double weight = item.GetTotalWeight();
		if (weightTotal + weight > capacity) throw new HumanSimulationException("Inventry : weightTotal > capacity");
	}

	public int FindSpace(ItemDef itemDef) {
		double space = (this.capacity - this.weightTotal) / itemDef.GetWeight();
		return (int) space;
	}

	// public void Put(ArrayList<Item> items) throws HumanSimulationException {
	// for (Item item : items) {
	// Put(item);
	// }
	// }

	public Item Get(ItemDef itemDef, int numPick) throws HumanSimulationException {
		Item item = storage.get(itemDef);
		if (item == null) return new Item(itemDef);

		int numStock = item.GetQuantity();
		if (numStock < numPick) throw new HumanSimulationException("Inventory : numStock < numPick");

		numStock -= numPick;

		if (numStock == 0) {
			storage.remove(itemDef);
		} else {
			item.SetQuantity(numStock);
		}

		Item ret = new Item(itemDef, numPick);
		weightTotal -= ret.GetTotalWeight();
		return ret;
	}

	public Item Peek(ItemDef itemDef, int numPick) throws HumanSimulationException {
		Item item = storage.get(itemDef);
		if (item == null) return new Item(itemDef);

		int numStock = item.GetQuantity();
		if (numStock < numPick) throw new HumanSimulationException("Inventory : numStock < numPick");

		return new Item(itemDef, numPick);
	}

	public int GetNumStock(ItemDef itemDef) {
		Item item = storage.get(itemDef);
		if (item == null) return 0;
		return item.GetQuantity();
	}
}
