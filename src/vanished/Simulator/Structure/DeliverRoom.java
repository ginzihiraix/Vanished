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

	// ����A�C�e���ꗗ���擾����B�ғ����Ă邩�ǂ����͋C�ɂ��Ȃ��B
	public ArrayList<CallForItem> GetDesiredItemList() {
		ArrayList<CallForItem> callForItemList = new ArrayList<CallForItem>();

		// �~�����A�C�e�����X�g��Ԃ��B
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

	// �����������擾����B�ғ����Ă邩�ǂ����͋C�ɂ��Ȃ��B
	public CallForItem GetDesiredItem(ItemDef itemDef) {
		StockManager sm = deliverStockManager.get(itemDef);
		int lotmax = sm.GetCurrentMaxLot();
		double price = sm.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		CallForItem callForItem = new CallForItem(itemDef, price, lotmax);
		return callForItem;
	}

	// �A�C�e���𔄂邽�߂ɕK�v�ȏ��v���Ԃ��擾����B
	public long GetDurationToSell(ItemDef itemDef) {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;
		return deliverRoomDef.durationForDeliver;
	}

	// �A�C�e���𔄂�B
	public void SellItem(long timeNow, Item item, double price, boolean simulation) throws HumanSimulationException {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;

		this.Greeting(timeNow, deliverRoomDef.durationForDeliver, simulation);

		// �A�C�e�����i�[����B
		StockManager sm = deliverStockManager.get(item.GetItemDef());
		sm.Put(timeNow, item, simulation);

		if (simulation == false) {
			// ���𕥂��B
			this.AddMoney(timeNow, -price * item.GetQuantity());
		}
	}

	// ���i���i�ɑ΂��ăt�H�[�h�o�b�N��^����B�����炾������No1�̑I�����ɂȂ����̂��A�eHuman���t�B�[�h�o�b�N��^����B
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
