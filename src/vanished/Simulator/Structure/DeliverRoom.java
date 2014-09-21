package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class DeliverRoom extends RunnableRoom {

	TreeMap<ItemDef, StockManager> deliverStockManager = new TreeMap<ItemDef, StockManager>(new ItemDefComparator());

	public DeliverRoom(Building building, DeliverRoomDef roomDef) {
		super(building, roomDef);

		for (Entry<ItemDef, StockManagerInfo> e : roomDef.materialStockManagerInfo.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManagerInfo smi = e.getValue();
			StockManager sm = new StockManager(itemDef, smi);
			deliverStockManager.put(itemDef, sm);
		}
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		System.out.println("===Deliver Department===");
		for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManager sm = e.getValue();
			System.out.println("material name : " + itemDef.GetName());
			System.out.println("material stock : " + sm.GetNumStock());
			System.out.println("material price : " + sm.price);
		}
	}

	public class CallForItem {
		public ItemDef itemDef;
		public double price;
		public int lotmax;

		public CallForItem(ItemDef itemDef, double price, int lotmax) {
			this.itemDef = itemDef;
			this.price = price;
			this.lotmax = lotmax;
		}
	}

	// 買取アイテム一覧を取得する。稼動してるかどうかは気にしない。
	public ArrayList<CallForItem> GetDesiredItemList() {
		ArrayList<CallForItem> callForItemList = new ArrayList<CallForItem>();

		// 欲しいアイテムリストを返す。
		for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManager sm = e.getValue();
			int lotmax = sm.GetCurrentMaxLot();
			double price = sm.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
			CallForItem callForItem = new CallForItem(itemDef, price, lotmax);
			callForItemList.add(callForItem);
		}
		return callForItemList;
	}

	// 買い取り情報を取得する。稼動してるかどうかは気にしない。
	public CallForItem GetDesiredItem(ItemDef itemDef) {
		StockManager sm = deliverStockManager.get(itemDef);
		int lotmax = sm.GetCurrentMaxLot();
		double price = sm.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		CallForItem callForItem = new CallForItem(itemDef, price, lotmax);
		return callForItem;
	}

	// アイテムを売るために必要な所要時間を取得する。
	public long GetDurationToSell(ItemDef itemDef) {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;
		return deliverRoomDef.durationForDeliver;
	}

	// アイテムを売る。
	public void SellItem(long timeNow, Item item, double price, boolean simulation) throws HumanSimulationException {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;

		this.Greeting(timeNow, deliverRoomDef.durationForDeliver, simulation);

		// アイテムを格納する。
		StockManager sm = deliverStockManager.get(item.GetItemDef());
		sm.Put(timeNow, item, simulation);

		if (simulation == false) {
			// 金を払う。
			this.AddMoney(timeNow, -price * item.GetQuantity());
		}
	}

	// 商品価格に対してフォードバックを与える。いくらだったらNo1の選択肢になったのか、各Humanがフィードバックを与える。
	public void FeedbackAboutDeliverPrice(ItemDef itemDef, double price, double quantity) {
		StockManager sm = deliverStockManager.get(itemDef);
		sm.Feedback(price, quantity);
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Test
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Obsolete
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
}
