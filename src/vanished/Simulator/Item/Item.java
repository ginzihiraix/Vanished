package vanished.Simulator.Item;

public class Item extends Object {
	ItemDef info;

	// ƒAƒCƒeƒ€‚Ì—Ê
	int quantity;

	public Item(ItemDef itemDef) {
		this.info = itemDef;
	}

	public Item(ItemDef itemDef, int quantity) {
		this.info = itemDef;
		this.quantity = quantity;
	}

	public Item(Item item) {
		this.info = item.info;
		this.quantity = item.quantity;
	}

	public ItemDef GetItemDef() {
		return info;
	}

	public int GetQuantity() {
		return quantity;
	}

	public void SetQuantity(int q) {
		this.quantity = q;
	}

	public double GetTotalWeight() {
		return this.quantity * info.weight;
	}
}
