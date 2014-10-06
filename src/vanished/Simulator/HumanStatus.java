package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Item.ConsumeDef;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.MoveMethod;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.DeliverRoom;
import vanished.Simulator.Structure.DeliverRoom.CallForItem;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.FactoryRoom.CallForMaker;
import vanished.Simulator.Structure.FactoryRoom.CallForMakerInKind;
import vanished.Simulator.Structure.Room;
import vanished.Simulator.Structure.ShopRoom;
import vanished.Simulator.Structure.ShopRoom.ItemCatalog;

public class HumanStatus {

	final MapManager mm;

	// fix
	final boolean sex;

	// state Year
	int status;
	Skill myskill;

	// state Daily
	UtilityManager utilityManager;
	double money;
	Room currentRoom;

	Inventory inventory = new Inventory();
	MoveMethod moveMethod;

	// 何時まで計算が完了したかの時刻
	long timeSimulationComplete = 0;
	long timeBorn = 0;
	long timeBecomeAdult = 0;
	long totalTimeWork = 0;

	// 統計情報
	ExponentialMovingAverage wageMovingAverage = new ExponentialMovingAverage(60 * 24 * 90, true);
	ExponentialMovingAverage utilityMovingAverage = new ExponentialMovingAverage(60 * 24 * 90, false);

	public HumanStatus(MapManager mm, long timeNow, Room current) throws Exception {
		this.mm = mm;

		sex = OtherUtility.rand.nextBoolean();

		status = HumanDef.status_baby;
		myskill = GlobalParameter.dm.GetSkill("noskill");

		utilityManager = new UtilityManager();
		money = 0;
		this.currentRoom = current;

		timeSimulationComplete = timeNow;
		timeBorn = timeNow;
		timeBecomeAdult = timeNow;
		totalTimeWork = 0;

		// TODO:とりあえずご飯を食う。
		if (false) {
			for (int i = 0; i < 10; i++) {
				ItemDef fish = GlobalParameter.dm.GetItemDef("fish");
				this.utilityManager.AddUtility(fish.GetUtilities(), this.timeSimulationComplete);

				ItemDef water = GlobalParameter.dm.GetItemDef("water");
				this.utilityManager.AddUtility(water.GetUtilities(), this.timeSimulationComplete);
			}
		}
	}

	public HumanStatus(MapManager mm, HumanStatus hs) {
		this.mm = mm;

		this.sex = hs.sex;

		this.status = hs.status;
		this.myskill = hs.myskill;

		this.utilityManager = new UtilityManager(hs.utilityManager);
		this.money = hs.money;
		this.currentRoom = hs.currentRoom;

		this.inventory = new Inventory(hs.inventory);
		this.moveMethod = hs.moveMethod;

		this.timeSimulationComplete = hs.timeSimulationComplete;
		this.timeBorn = hs.timeBorn;
		this.timeBecomeAdult = hs.timeBecomeAdult;
		this.totalTimeWork = hs.totalTimeWork;

		this.wageMovingAverage = new ExponentialMovingAverage(hs.wageMovingAverage);
		this.utilityMovingAverage = new ExponentialMovingAverage(hs.utilityMovingAverage);
	}

	public HumanStatus Birth() {
		HumanStatus ret = new HumanStatus(mm, this);
		ret.timeBecomeAdult = ret.timeBorn = ret.timeSimulationComplete;
		ret.totalTimeWork = 0;

		ret.money /= 2;
		this.money /= 2;
		return ret;
	}

	public void Dump() throws Exception {
		System.out.println(String.format("$%f, $%f/day", money, wageMovingAverage.GetAverage(this.timeSimulationComplete) * 60 * 24));
		System.out.println(String.format("%f[util], %f[util]", this.ComputeUtility(), utilityMovingAverage.GetAverage(this.timeSimulationComplete)));
	}

	public boolean ShouldWork() {
		long durationAdult = timeSimulationComplete - timeBecomeAdult;
		if (durationAdult < 0) return false;
		if (totalTimeWork == 0) return true;
		double temp = (double) (durationAdult / totalTimeWork);
		double rate = 1.0 / temp;
		if (rate < HumanDef.rateWork) {
			return true;
		} else {
			return false;
		}
	}

	public class TryResult {
		double moneyStart;
		double moneyEnd;
		long timeStart;
		long timeEnd;
		double utilStart;
		double utilEnd;

		public TryResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd) {
			this.moneyStart = moneyStart;
			this.moneyEnd = moneyEnd;
			this.timeStart = timeStart;
			this.timeEnd = timeEnd;
			this.utilStart = utilStart;
			this.utilEnd = utilEnd;
		}
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// Traderの仕事をやってみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public class TryBuyResult extends TryResult {
		ShopRoom shopRoom;
		ItemCatalog itemCatalog;

		public TryBuyResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd, ShopRoom shopRoom,
				ItemCatalog itemCatalog) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

			this.shopRoom = shopRoom;
			this.itemCatalog = itemCatalog;
		}
	}

	public class TrySellResult extends TryResult {
		DeliverRoom deliverRoom;
		CallForItem callForItem;

		public TrySellResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd,
				DeliverRoom deliverRoom, CallForItem callForItem) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

			this.deliverRoom = deliverRoom;
			this.callForItem = callForItem;
		}
	}

	public boolean TryTraderAndConsume(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		realFlag = this.TryTrader(realFlag, ret);
		realFlag = this.TryConsumeWithAllMoney(realFlag, ret);
		return realFlag;
	}

	private boolean TryTrader(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		// System.out.println("TryTrader");

		// ランダムにデリバー先を選ぶ。
		DeliverRoom deliverRoom;
		{
			ArrayList<DeliverRoom> list = mm.GetDeliverableRoomList(this.moveMethod, HumanDef.maxMoveTimeForTrade, this.currentRoom, this.money,
					Double.MAX_VALUE, realFlag == false);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no deliverable room who want to buy something");
			int index = OtherUtility.rand.nextInt(num);
			deliverRoom = list.get(index);
			if (deliverRoom.IsReal() == false) {
				realFlag = false;
			}
		}

		// ランダムにほしがってる商品情報を選ぶ。
		CallForItem callForItem;
		{
			ArrayList<CallForItem> listOrg = deliverRoom.GetDesiredItemList(Double.MAX_VALUE, Double.MAX_VALUE);
			ArrayList<CallForItem> list = new ArrayList<CallForItem>();
			for (CallForItem callForItem2 : listOrg) {
				if (callForItem2.numPick == 0) continue;
				list.add(callForItem2);
			}
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no items desired by the deliverRoom");
			int index = OtherUtility.rand.nextInt(num);
			callForItem = list.get(index);
		}

		// ランダムに購入店を選ぶ。
		ShopRoom shopRoom;
		{
			ArrayList<ShopRoom> list = mm.GetShopRoomList(this.moveMethod, HumanDef.maxMoveTimeForTrade, this.currentRoom, callForItem.itemDef,
					Double.MAX_VALUE, Double.MAX_VALUE, realFlag == false);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no shop to buy the desired items");
			int index = OtherUtility.rand.nextInt(num);
			shopRoom = list.get(index);
			if (shopRoom.IsReal() == false) {
				realFlag = false;
			}
		}

		// 商品情報を取得する。
		ItemCatalog itemCatalog = shopRoom.GetProductItemForConsumeWithNewPrice(this.money, Double.MAX_VALUE);

		// 買う
		{
			double moneyStart = this.money;
			long timeStart = this.timeSimulationComplete;
			double utilStart = this.ComputeUtility();

			this.Move(shopRoom);
			this.Buy(itemCatalog, true);

			double moneyEnd = this.money;
			long timeEnd = this.timeSimulationComplete;
			double utilEnd = this.ComputeUtility();

			ret.add(new TryBuyResult(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd, shopRoom, itemCatalog));
		}

		// 売る
		{
			double moneyStart = this.money;
			long timeStart = this.timeSimulationComplete;
			double utilStart = this.ComputeUtility();

			this.Move(deliverRoom);
			this.Sell(callForItem, true);

			double moneyEnd = this.money;
			long timeEnd = this.timeSimulationComplete;
			double utilEnd = this.ComputeUtility();

			ret.add(new TrySellResult(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd, deliverRoom, callForItem));
		}

		return realFlag;
	}

	// public void DoTrader(TraderWorkResult res) throws Exception {
	// System.out.println(String.format("DoTrader : Bought %f %s with $%f at %s, and sold it with $%f to %s.", res.callForItem.numPick,
	// res.callForItem.itemDef.GetName(), res.itemCatalog.price, res.shopRoom.GetName(), res.callForItem.price, res.deliverRoom.GetName()));
	//
	// double moneyStart = this.money;
	// long timeStart = this.timeSimulationComplete;
	//
	// this.Move(res.shopRoom);
	//
	// this.Buy(res.itemCatalog, false);
	//
	// this.Move(res.deliverRoom);
	//
	// this.Sell(res.callForItem, false);
	//
	// double moneyEnd = this.money;
	// long timeEnd = this.timeSimulationComplete;
	//
	// double gain = moneyEnd - moneyStart;
	// double duration = timeEnd - timeStart;
	//
	// this.wageMovingAverage.Add(this.timeSimulationComplete, gain);
	// this.totalTimeWork += duration;
	// }

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// Makerの仕事をやってみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	public class TryMakerResult extends TryResult {
		FactoryRoom factoryRoom;
		CallForMaker cfm;

		public TryMakerResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd,
				FactoryRoom factoryRoom, CallForMaker cfm) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
			this.factoryRoom = factoryRoom;
			this.cfm = cfm;
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof TryMakerResult == false) return false;

			TryMakerResult res = (TryMakerResult) obj;
			if (this.factoryRoom == res.factoryRoom) {
				if (this.cfm.skill.GetName().equals(res.cfm.skill.GetName())) {
					if (this.cfm.itemDef.GetName().equals(res.cfm.itemDef.GetName())) { return true; }
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int ret = factoryRoom.hashCode() + cfm.skill.GetName().hashCode() + cfm.itemDef.GetName().hashCode();
			return ret;
		}
	}

	public boolean TryMakerAndConsume(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		realFlag = this.TryMaker(realFlag, ret);
		realFlag = this.TryConsumeWithAllMoney(realFlag, ret);
		return realFlag;
	}

	private boolean TryMaker(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		// System.out.println("TryMaker");

		// ランダムに労働場所を選ぶ。
		FactoryRoom factoryRoom = null;
		{
			ArrayList<FactoryRoom> list = mm.GetFactoryRoomList(this.moveMethod, HumanDef.maxMoveTimeForWork, this.currentRoom, realFlag == false);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryMaker : There are no work place as maker");
			int index = OtherUtility.rand.nextInt(num);
			factoryRoom = list.get(index);
			if (factoryRoom.IsReal() == false) {
				realFlag = false;
			}
		}

		// 要求している労働者を調べる。
		CallForMaker cfm = null;
		{
			cfm = factoryRoom.GetDesiredMaker(Double.MAX_VALUE);
			if (cfm == null) throw new HumanSimulationException("TryMaker : There are no skill position in the selected factory.");
			// 自分のスキルで実行可能か調べる。
			if (myskill.hasAbility(cfm.skill) == false) throw new HumanSimulationException(
					"TryMaker : There are no skill position in the selected factory.");
		}

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		this.Move(factoryRoom);
		this.Make(cfm, true);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		TryMakerResult result = new TryMakerResult(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd, factoryRoom, cfm);
		ret.add(result);

		return realFlag;
	}

	// public void DoMaker(TryMakerResult res) throws Exception {
	// System.out.println(String.format("DoMaker : %sで仕事した。%sを%f個・賃金%f・時間%dで作った。", res.factoryRoom.GetName(), res.cfm.itemDef.GetName(),
	// res.cfm.numMake, res.cfm.gain, res.cfm.duration));
	//
	// double moneyStart = this.money;
	// long timeStart = this.timeSimulationComplete;
	//
	// this.Move(res.factoryRoom);
	//
	// this.Make(res.cfm, false);
	//
	// double moneyEnd = this.money;
	// long timeEnd = this.timeSimulationComplete;
	//
	// double gain = moneyEnd - moneyStart;
	// double duration = timeEnd - timeStart;
	//
	// this.wageMovingAverage.Add(this.timeSimulationComplete, gain);
	// this.totalTimeWork += duration;
	// }

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// 有り金全部Consumeしてみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public class TryConsumeResult extends TryResult {
		ShopRoom shopRoom;
		ItemCatalog itemCatalog;

		public TryConsumeResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd,
				ShopRoom shopRoom, ItemCatalog itemCatalog) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

			this.shopRoom = shopRoom;
			this.itemCatalog = itemCatalog;
		}
	}

	private boolean TryConsumeWithAllMoney(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		// System.out.println("TryConsumeWithAllMoney");

		while (true) {
			if (this.money == 0) break;

			// 消費する場所を決定する。
			ShopRoom consumeRoom;
			if (true) {
				ArrayList<ShopRoom> list = mm.GetConsumableRoomList(moveMethod, HumanDef.maxMoveTimeForConsume, currentRoom, Double.MAX_VALUE,
						Double.MAX_VALUE, realFlag == false);
				int num = list.size();
				if (num == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
				int index = OtherUtility.rand.nextInt(num);
				consumeRoom = list.get(index);

				if (consumeRoom.IsReal() == false) {
					realFlag = false;
				}
			}

			// 消費するアイテムを決定する。
			ItemCatalog consumeItemCatalog;
			if (true) {
				consumeItemCatalog = consumeRoom.GetProductItemForConsumeWithNewPrice(this.money, Double.MAX_VALUE);
				if (consumeItemCatalog == null) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
				if (consumeItemCatalog.itemDef instanceof ConsumeDef == false) throw new HumanSimulationException(
						"TryConsume : There are no eat place to get food");
				if (consumeItemCatalog.numPick == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
			}

			double moneyStart = this.money;
			long timeStart = this.timeSimulationComplete;
			double utilStart = this.ComputeUtility();

			Move(consumeRoom);
			Consume(consumeItemCatalog, true);

			double moneyEnd = this.money;
			long timeEnd = this.timeSimulationComplete;
			double utilEnd = this.ComputeUtility();

			ret.add(new TryConsumeResult(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd, consumeRoom, consumeItemCatalog));
		}

		return realFlag;
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// 何もしないで過ごしてみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	public class NopResult extends TryResult {
		public NopResult(double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof NopResult == false) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	public NopResult TryNop() throws Exception {
		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		Nop();

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		NopResult res = new NopResult(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
		return res;
	}

	public void DoNop() throws Exception {
		System.out.println("DoNop");
		Nop();
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// アイテムを消費してみる。金で買う。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// public class ConsumeResult extends TryResult {
	// ShopRoom shopRoom;
	// ItemCatalog itemCatalog;
	//
	// public ConsumeResult(ShopRoom shopRoom, ItemCatalog itemCatalog, double moneyStart, double moneyEnd, long timeStart, long timeEnd,
	// double utilStart, double utilEnd) {
	// super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
	// this.shopRoom = shopRoom;
	// this.itemCatalog = itemCatalog;
	// }
	//
	// @Override
	// public boolean equals(java.lang.Object obj) {
	// if (obj instanceof ConsumeResult == false) return false;
	// ConsumeResult res = (ConsumeResult) obj;
	// if (this.shopRoom == res.shopRoom) {
	// if (this.itemCatalog.itemDef.GetName().equals(res.itemCatalog.itemDef.GetName())) { return true; }
	// }
	// return false;
	// }
	//
	// @Override
	// public int hashCode() {
	// int ret = this.shopRoom.hashCode() + this.itemCatalog.itemDef.GetName().hashCode();
	// return ret;
	// }
	// }

	// public ConsumeResult TryConsume() throws Exception {
	// System.out.println("TryConsume");
	//
	// double moneyStart = this.money;
	// long timeStart = this.timeSimulationComplete;
	// double utilStart = this.ComputeUtility();
	//
	// // 消費する場所を決定する。
	// ShopRoom consumeRoom;
	// if (true) {
	// ArrayList<ShopRoom> list = mm.GetConsumableRoomList(moveMethod, HumanDef.maxMoveTimeForConsume, currentRoom, Double.MAX_VALUE,
	// Double.MAX_VALUE, false);
	// int num = list.size();
	// if (num == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
	// int index = OtherUtility.rand.nextInt(num);
	// consumeRoom = list.get(index);
	// }
	//
	// Move(consumeRoom);
	//
	// // 消費するアイテムを決定する。
	// ItemCatalog consumeItemCatalog;
	// if (true) {
	// consumeItemCatalog = consumeRoom.GetProductItemForConsumeWithNewPrice(this.money, 1);
	// if (consumeItemCatalog == null) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
	// if (consumeItemCatalog.itemDef instanceof ConsumeDef == false) throw new HumanSimulationException(
	// "TryConsume : There are no eat place to get food");
	// if (consumeItemCatalog.numPick == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
	// }
	//
	// Consume(consumeItemCatalog, true);
	//
	// double moneyEnd = this.money;
	// long timeEnd = this.timeSimulationComplete;
	// double utilEnd = this.ComputeUtility();
	//
	// boolean realFlag = consumeRoom.IsReal();
	//
	// ConsumeResult res = new ConsumeResult(consumeRoom, consumeItemCatalog, moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd, realFlag);
	// return res;
	// }
	//
	// public void DoConsume(ConsumeResult res) throws Exception {
	// System.out.println(String.format("DoConsume : Consumed %s with $%f at %s", res.itemCatalog.itemDef.GetName(), res.itemCatalog.price,
	// res.shopRoom.GetName()));
	// Move(res.shopRoom);
	// Consume(res.itemCatalog, false);
	// }

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// アイテムを消費してみる。労働＋現物支給。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public class TryConsumeWithWorkResult extends TryResult {
		FactoryRoom factoryRoom;
		ItemCatalog itemCatalog;
		CallForMakerInKind cfm;

		public TryConsumeWithWorkResult(FactoryRoom shopRoom, ItemCatalog itemCatalog, CallForMakerInKind cfm, double moneyStart, double moneyEnd,
				long timeStart, long timeEnd, double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
			this.factoryRoom = shopRoom;
			this.itemCatalog = itemCatalog;
			this.cfm = cfm;
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof TryConsumeWithWorkResult == false) return false;
			TryConsumeWithWorkResult res = (TryConsumeWithWorkResult) obj;
			if (this.factoryRoom == res.factoryRoom) {
				if (this.itemCatalog.itemDef.GetName().equals(res.itemCatalog.itemDef.GetName())) {
					if (this.cfm.itemDef.GetName().equals(res.cfm.itemDef.GetName())) { return true; }
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int ret = this.factoryRoom.hashCode() + this.itemCatalog.itemDef.GetName().hashCode();
			return ret;
		}
	}

	public boolean TryConsumeWithWork(boolean realFlag, ArrayList<TryResult> ret) throws Exception {
		// System.out.println("TryConsumeWithWork");

		// 消費する場所を決定する。
		FactoryRoom consumeRoom;
		if (true) {
			ArrayList<FactoryRoom> list = mm.GetConsumableAndMakableRoomList(moveMethod, HumanDef.maxMoveTimeForConsume, currentRoom,
					Double.MAX_VALUE, Double.MAX_VALUE, realFlag == false);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
			int index = OtherUtility.rand.nextInt(num);
			consumeRoom = list.get(index);
			if (consumeRoom.IsReal() == false) {
				realFlag = false;
			}
		}

		CallForMakerInKind cfm;
		{
			cfm = consumeRoom.GetDesiredMakerInKind(1);
			if (this.myskill.hasAbility(cfm.skill) == false) throw new HumanSimulationException("TryConsumeWithWork : no skill");
			if (cfm.numMake == 0) throw new HumanSimulationException("TryConsumeWithWork : no space to make products");
		}

		// 消費するアイテムを決定する。
		ItemCatalog consumeItemCatalog;
		if (true) {
			consumeItemCatalog = consumeRoom.GetProductItemForMakeInKind(cfm.numMake);
			if (consumeItemCatalog.itemDef instanceof ConsumeDef == false) throw new HumanSimulationException(
					"TryConsumeWithWork : There are no eat place to get food");
			if (consumeItemCatalog.numPick == 0) throw new HumanSimulationException("TryConsumeWithWork : There are no eat place to get food");
		}

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		Move(consumeRoom);
		ConsumeWithMake(consumeItemCatalog, cfm, true);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		TryConsumeWithWorkResult res = new TryConsumeWithWorkResult(consumeRoom, consumeItemCatalog, cfm, moneyStart, moneyEnd, timeStart, timeEnd,
				utilStart, utilEnd);
		ret.add(res);

		return realFlag;
	}

	// public void DoConsumeWithWork(ConsumeWithWorkResult res) throws Exception {
	// System.out.println(String.format("DoConsumeWithWork : Consumed %s at %s", res.itemCatalog.itemDef.GetName(), res.factoryRoom.GetName()));
	//
	// Move(res.factoryRoom);
	// ConsumeWithMake(res.itemCatalog, res.cfm, false);
	// }

	public void DoAction(ArrayList<TryResult> sequence) throws Exception {
		for (TryResult res : sequence) {
			if (res instanceof TryBuyResult) {
				TryBuyResult result = (TryBuyResult) res;
				this.Move(result.shopRoom);
				this.Buy(result.itemCatalog, false);
			} else if (res instanceof TrySellResult) {
				TrySellResult result = (TrySellResult) res;
				this.Move(result.deliverRoom);
				this.Sell(result.callForItem, false);
			} else if (res instanceof TryConsumeResult) {
				TryConsumeResult result = (TryConsumeResult) res;
				this.Move(result.shopRoom);
				this.Consume(result.itemCatalog, false);
			} else if (res instanceof TryMakerResult) {
				TryMakerResult result = (TryMakerResult) res;
				this.Move(result.factoryRoom);
				this.Make(result.cfm, false);
			} else if (res instanceof TryConsumeWithWorkResult) {
				TryConsumeWithWorkResult result = (TryConsumeWithWorkResult) res;
				this.Move(result.factoryRoom);
				this.ConsumeWithMake(result.itemCatalog, result.cfm, false);
			} else {
				throw new Exception("fatail error");
			}
		}
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// 行動最小単位
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public void Nop() throws Exception {
		long timeDelta = 60 * 8;

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Move(Room room) throws Exception {
		long timeDelta = mm.GetTravelTime(moveMethod, currentRoom, room);

		this.currentRoom = room;

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Consume(ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoom shopRoom = (ShopRoom) this.currentRoom;

		shopRoom.BuyProductItem(this.timeSimulationComplete, itemCatalog, simulation);
		timeSimulationComplete += itemCatalog.durationToBuy;
		this.utilityManager.AddUtility(itemCatalog.itemDef.GetUtilities(), this.timeSimulationComplete);
		money -= itemCatalog.price * itemCatalog.numPick;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void ConsumeWithMake(ItemCatalog itemCatalog, CallForMakerInKind cfm, boolean simulation) throws Exception {
		FactoryRoom factoryRoom = (FactoryRoom) this.currentRoom;

		Item item = factoryRoom.MakeInKind(cfm, this.timeSimulationComplete, simulation);
		this.timeSimulationComplete += cfm.duration;
		this.timeSimulationComplete += itemCatalog.durationToBuy;
		this.utilityManager.AddUtility(item.GetItemDef().GetUtilities(), this.timeSimulationComplete);

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Buy(ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoom shopRoom = (ShopRoom) this.currentRoom;

		Item item = shopRoom.BuyProductItem(this.timeSimulationComplete, itemCatalog, simulation);
		this.inventory.Put(item);

		money -= itemCatalog.price * itemCatalog.numPick;
		if (this.money < 0) throw new HumanSimulationException("TryTrader : no money");

		timeSimulationComplete += itemCatalog.durationToBuy;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Sell(CallForItem callForItem, boolean simulation) throws Exception {
		DeliverRoom deliverRoom = (DeliverRoom) this.currentRoom;

		deliverRoom.SellItem(this.timeSimulationComplete, callForItem, simulation);

		money += callForItem.price * callForItem.numPick;

		timeSimulationComplete += callForItem.durationToSell;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Make(CallForMaker cfm, boolean simulation) throws Exception {
		FactoryRoom factoryRoom = (FactoryRoom) this.currentRoom;

		factoryRoom.Make(cfm, this.timeSimulationComplete, simulation);

		money += cfm.gain;
		timeSimulationComplete += cfm.duration;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// その他の関数
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public double ComputeUtility() throws Exception {
		return utilityManager.ComputeUtility(this.timeSimulationComplete);
	}
}
