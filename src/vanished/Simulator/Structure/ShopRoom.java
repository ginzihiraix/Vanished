package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class ShopRoom extends DeliverRoom {

	StockManager shopStockManager;

	public ShopRoom(Building building, ShopRoomDef roomDef) {
		super(building, roomDef);

		shopStockManager = new StockManager(roomDef.productItemDef, roomDef.productStockManagerInfo);
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		System.out.println("===Shop Department===");
		System.out.println("product name : " + this.shopStockManager.stockManagerInfo.itemDef.GetName());
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
		return shopStockManager.stockManagerInfo.itemDef.GetName();
	}

	public ItemDef GetProductItemDef() {
		return shopStockManager.stockManagerInfo.itemDef;
	}

	public class ItemCatalog {
		public ItemDef itemDef;
		public double price;
		public double numPick;
		public long durationToBuy;

		public ItemCatalog(ItemDef itemDef, double price, double numConsume, long duration) {
			this.itemDef = itemDef;
			this.price = price;
			this.numPick = numConsume;
			this.durationToBuy = duration;
		}
	}

	// �̔����i�̈ꗗ���擾����B
	private ItemCatalog GetProductItem(double maxMoney, double maxNumPick, double price) {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

		if (this.shopStockManager.IsOpen() == false) return null;

		// �������߂�B
		double minNumPick = Double.MAX_VALUE;
		{
			// ��]�̔���ʂɂ�鐧��
			if (maxNumPick < minNumPick) minNumPick = maxNumPick;

			// ���i�ɂ�鐧��
			double numPickForMoney = maxMoney / price;
			if (numPickForMoney < minNumPick) minNumPick = numPickForMoney;

			// ���ɉ^�э��߂�ʂɂ�鐧��
			if (this.shopStockManager.stockManagerInfo.lotmax < minNumPick) minNumPick = this.shopStockManager.stockManagerInfo.lotmax;
		}

		// �w���ɗv���鎞�Ԃ����߂�B
		// TODO:���ɔ�Ⴕ�Ă�����B��
		// long duration = (long) (shopRoomDef.durationToSell * minNumPick) + 1;
		long duration = shopRoomDef.durationToSell;

		ItemCatalog itemCatalog = new ItemCatalog(this.shopStockManager.stockManagerInfo.itemDef, price, minNumPick, duration);
		return itemCatalog;
	}

	public ItemCatalog GetProductItem(double maxMoney, double maxNumPick) {
		// ���i�����߂�B
		double price = this.shopStockManager.GetPriceWithRate();
		return this.GetProductItem(maxMoney, maxNumPick, price);
	}

	public void SetProductPriceRate(double rate) {
		this.shopStockManager.SetPriceRate(rate);
	}

	// ���i�𔃂�
	public Item BuyProductItem(long timeNow, ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

		if (this.shopStockManager.IsOpen() == false) throw new Exception("aaa");

		this.Enter(timeNow, shopRoomDef.durationToSell, simulation);

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
	public void FeedbackAboutProductPrice(double price, double quantity) {
		// System.out.println("FeedbackAboutProductPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
		this.shopStockManager.Feedback(price, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// �C�x���g�L�^�p
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage productInputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 10, true);

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
