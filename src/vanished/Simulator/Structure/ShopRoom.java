package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class ShopRoom extends DeliverRoom {

	StockManager shopStockManager;

	public ShopRoom(Building building, ShopRoomDef roomDef) {
		super(building, roomDef);

		shopStockManager = new StockManager(roomDef.productStockManagerInfo);
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		ShopRoomDef roomDef = (ShopRoomDef) this.roomDef;
		System.out.println("===Shop Department===");
		System.out.println("product name : " + roomDef.productItemDef.GetName());
		System.out.println("product stock : " + this.shopStockManager.GetNumStock());
		System.out.println("product price : " + this.shopStockManager.GetPriceWithRate());
	}

	// �̔����i���擾����B
	public double GetProductItemPrice() {
		return shopStockManager.GetPriceWithRate();
	}

	public double GetProductItemStock() {
		return shopStockManager.GetNumStock();
	}

	public String GetProductItemName() {
		ShopRoomDef roomDef = (ShopRoomDef) this.roomDef;
		return roomDef.productItemDef.GetName();
	}

	public ItemDef GetProductItemDef() {
		ShopRoomDef roomDef = (ShopRoomDef) this.roomDef;
		return roomDef.productItemDef;
	}

	public class ItemCatalog {
		public ItemDef itemDef;
		public double price;
		public int priceIndex;
		public double numPick;
		public long durationToBuy;

		public ItemCatalog(ItemDef itemDef, double price, int priceIndex, double numConsume, long duration) {
			this.itemDef = itemDef;
			this.price = price;
			this.priceIndex = priceIndex;
			this.numPick = numConsume;
			this.durationToBuy = duration;
		}
	}

	// �̔����i�̈ꗗ���擾����B
	public ItemCatalog GetProductItem(double maxMoney, double maxNumPick) {
		ShopRoomDef roomDef = (ShopRoomDef) this.roomDef;

		double price = this.shopStockManager.GetPriceWithRate();
		int priceIndex = this.shopStockManager.GetPriceIndex();

		// �������߂�B
		double minNumPick = Double.MAX_VALUE;
		{
			// ��]�̔���ʂɂ�鐧��
			if (maxNumPick < minNumPick) minNumPick = maxNumPick;

			// ���i�ɂ�鐧��
			double numPickForMoney = maxMoney / price;
			if (numPickForMoney < minNumPick) minNumPick = numPickForMoney;
		}

		// �w���ɗv���鎞�Ԃ����߂�B
		// TODO:���ɔ�Ⴕ�Ă�����B��
		// long duration = (long) (shopRoomDef.durationToSell * minNumPick) + 1;
		long duration = roomDef.durationToSell;

		ItemCatalog itemCatalog = new ItemCatalog(roomDef.productItemDef, price, priceIndex, minNumPick, duration);
		return itemCatalog;
	}

	public void SetProductPriceIndex(int priceIndex) {
		this.shopStockManager.SetPriceWithIndex(priceIndex);
	}

	// ���i�𔃂�
	public Item BuyProductItem(long timeNow, ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoomDef roomDef = (ShopRoomDef) this.roomDef;

		if (itemCatalog.numPick <= 0) throw new HumanSimulationException("numPick <= 0");

		// this.Enter(timeNow, shopRoomDef.durationToSell, simulation);

		// ���i�����o���B
		Item item = this.shopStockManager.Get(timeNow, itemCatalog.numPick, simulation);

		if (simulation == false) {
			// ����グ���v�シ��B
			this.AddMoney(timeNow, itemCatalog.price * itemCatalog.numPick);
			productInputMoneyEMA.Add(timeNow, itemCatalog.price * itemCatalog.numPick);
		}

		return item;
	}

	// ���i���i�ɑ΂��ăt�H�[�h�o�b�N��^����B�����炾������No1�̑I�����ɂȂ����̂��A�eHuman���t�B�[�h�o�b�N��^����B
	public void FeedbackAboutProductPrice(int priceIndex, double quantity) {
		// System.out.println("FeedbackAboutProductPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
		this.shopStockManager.Feedback(priceIndex, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// �C�x���g�L�^�p
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage productInputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 1, true);

	public double GetProductInputStockEMA(long timeNow) {
		return this.shopStockManager.GetInputStockEMA(timeNow);
	}

	public double GetProductInputMoneyEMA(long timeNow) {
		return this.productInputMoneyEMA.GetAverage(timeNow);
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		super.DiscardOldLog(timeNow);
		// long duration = 60L * 24L * 365L * 10L;
		// this.shopStockManager.DiscardOldLog(timeNow - duration);
	}

}
