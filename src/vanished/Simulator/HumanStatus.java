package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Item.ConsumeDef;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.MoveMethod;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.DeliverRoom;
import vanished.Simulator.Structure.DeliverRoom.CallForItem;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.FactoryRoom.CallForMaker;
import vanished.Simulator.Structure.FactoryRoom.MakeResult;
import vanished.Simulator.Structure.Room;
import vanished.Simulator.Structure.RunnableRoom;
import vanished.Simulator.Structure.RunnableRoom.CallForWorker;
import vanished.Simulator.Structure.RunnableRoom.WorkResult;
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
		money = 1000;
		this.currentRoom = current;

		timeSimulationComplete = timeNow;
		timeBorn = timeNow;
		timeBecomeAdult = timeNow;
		totalTimeWork = 0;

		// TODO
		{
			for (int i = 0; i < 10; i++) {
				ItemDef fish = GlobalParameter.dm.GetItemDef("fish");
				this.utilityManager.AddUtility(fish.GetUtilities(), this.timeSimulationComplete, 0);

				ItemDef water = GlobalParameter.dm.GetItemDef("water");
				this.utilityManager.AddUtility(water.GetUtilities(), this.timeSimulationComplete, 0);
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
	public class TraderWorkResult extends TryResult {
		DeliverRoom deliverRoom;
		CallForItem callForItem;
		ShopRoom shopRoom;
		ItemCatalog itemCatalog;
		ItemDef itemDef;
		int numPick;

		public TraderWorkResult(ShopRoom shopRoom, CallForItem callForItem, DeliverRoom deliverRoom, ItemCatalog itemCatalog, ItemDef itemDef,
				int numPick, double moneyStart, double moneyEnd, long timeStart, long timeEnd, double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

			this.shopRoom = shopRoom;
			this.callForItem = callForItem;
			this.deliverRoom = deliverRoom;
			this.itemCatalog = itemCatalog;
			this.itemDef = itemDef;
			this.numPick = numPick;
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof TraderWorkResult == false) return false;
			TraderWorkResult res = (TraderWorkResult) obj;
			if (this.deliverRoom == res.deliverRoom) {
				if (this.shopRoom == res.shopRoom) {
					if (this.itemDef.GetName().equals(res.itemDef.GetName())) { return true; }
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int ret = deliverRoom.hashCode() + shopRoom.hashCode() + itemDef.GetName().hashCode();
			return ret;
		}
	}

	public TraderWorkResult TryTrader() throws Exception {
		// System.out.println("TryTrader");

		// ランダムにデリバー先を選ぶ。
		DeliverRoom deliverRoom;
		{
			ArrayList<DeliverRoom> list = mm.GetDeliverableRoomList(this.moveMethod, HumanDef.maxMoveTimeForEat, this.currentRoom);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no deliverable room who want to buy something");
			int index = OtherUtility.rand.nextInt(num);
			deliverRoom = list.get(index);
		}

		// ランダムにデリバー先が欲しがってるアイテムを選ぶ。
		CallForItem callForItem;
		{
			ArrayList<CallForItem> listOrg = deliverRoom.GetDesiredItemList();
			ArrayList<CallForItem> list = new ArrayList<CallForItem>();
			for (CallForItem cfi : listOrg) {
				if (cfi.lotmax == 0) continue;
				list.add(cfi);
			}
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no items desired by the deliverRoom");
			int index = OtherUtility.rand.nextInt(num);
			callForItem = list.get(index);
		}

		// ランダムに購入店を選ぶ。
		ShopRoom shopRoom;
		{
			ArrayList<ShopRoom> list = mm.GetShopRoomList(this.moveMethod, HumanDef.maxMoveTimeForTrade, Double.MAX_VALUE, this.currentRoom,
					callForItem.itemDef);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no shop to buy the desired items");
			int index = OtherUtility.rand.nextInt(num);
			shopRoom = list.get(index);
		}

		// 店が売っているアイテムを選ぶ。
		ItemCatalog itemCatalog;
		{
			itemCatalog = shopRoom.GetProductItem(Double.MAX_VALUE, true, callForItem.itemDef);
			if (itemCatalog == null) throw new HumanSimulationException("TryTrader : There are no desired items at the selected shop");
		}

		// 転売できる最大量を計算する。
		int numPick = Integer.MAX_VALUE;
		int numPickMaxFromMoney = (int) (money / itemCatalog.price);
		if (numPickMaxFromMoney < numPick) numPick = numPickMaxFromMoney;
		if (callForItem.lotmax < numPick) numPick = callForItem.lotmax;
		if (itemCatalog.lotmax < numPick) numPick = itemCatalog.lotmax;
		if (numPick == 0) throw new HumanSimulationException("TryTrader : numPickMax==0");

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		// 店に移動する。
		this.Move(shopRoom);

		// 購入する。
		this.Buy(itemCatalog, numPick, true);

		// デリバー先に移動する。
		this.Move(deliverRoom);

		// 売る。
		this.Sell(callForItem.itemDef, callForItem.price, numPick, true);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		TraderWorkResult result = new TraderWorkResult(shopRoom, callForItem, deliverRoom, itemCatalog, callForItem.itemDef, numPick, moneyStart,
				moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
		return result;
	}

	public void DoTrader(TraderWorkResult res) throws Exception {
		System.out.println(String.format("DoTrader : Bought %d %s with $%f at %s, and sold it with $%f to %s.", res.numPick, res.itemDef.GetName(),
				res.itemCatalog.price, res.shopRoom.GetName(), res.callForItem.price, res.deliverRoom.GetName()));

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		this.Move(res.shopRoom);

		this.Buy(res.itemCatalog, res.numPick, false);

		this.Move(res.deliverRoom);

		this.Sell(res.itemDef, res.callForItem.price, res.numPick, false);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		this.wageMovingAverage.Add(this.timeSimulationComplete, moneyEnd - moneyStart);
		this.totalTimeWork += this.timeSimulationComplete - timeStart;
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// Workerの仕事をやってみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	public class WorkerWorkResult extends TryResult {
		RunnableRoom runnableRoom;
		CallForWorker cfw;

		public WorkerWorkResult(RunnableRoom runnableRoom, CallForWorker cfw, double moneyStart, double moneyEnd, long timeStart, long timeEnd,
				double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

			this.runnableRoom = runnableRoom;
			this.cfw = cfw;
		}
	}

	public WorkerWorkResult TryWorker() throws Exception {
		// System.out.println("TryWorker");

		// ランダムに労働場所を選ぶ。
		RunnableRoom runnableRoom;
		{
			ArrayList<RunnableRoom> list = mm.GetRunnableRoomList(this.moveMethod, HumanDef.maxMoveTimeForWork, this.currentRoom);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no work place as worker");
			int index = OtherUtility.rand.nextInt(num);
			runnableRoom = list.get(index);
		}

		// 要求している労働者を調べる。
		CallForWorker cfw;
		{
			ArrayList<CallForWorker> list = runnableRoom.GetDesiredWorker();
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryTrader : There are no skill position for this human in the selected working place");
			int index = OtherUtility.rand.nextInt(num);
			cfw = list.get(index);
		}

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		this.Move(runnableRoom);

		this.Work(cfw, true);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		WorkerWorkResult wwr = new WorkerWorkResult(runnableRoom, cfw, moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);

		return wwr;
	}

	public void DoWorker(WorkerWorkResult res) throws Exception {
		System.out.println(String.format("DoWorker : worked at %s with $%f", res.runnableRoom.GetName(), res.cfw.GetWage()));

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		this.Move(res.runnableRoom);

		this.Work(res.cfw, false);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		this.wageMovingAverage.Add(this.timeSimulationComplete, moneyEnd - moneyStart);
		totalTimeWork += this.timeSimulationComplete - timeStart;
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// Makerの仕事をやってみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	public class MakerWorkResult extends TryResult {
		FactoryRoom factoryRoom;
		CallForMaker cfm;

		public MakerWorkResult(FactoryRoom factoryRoom, CallForMaker cfm, double moneyStart, double moneyEnd, long timeStart, long timeEnd,
				double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
			this.factoryRoom = factoryRoom;
			this.cfm = cfm;
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof MakerWorkResult == false) return false;

			MakerWorkResult res = (MakerWorkResult) obj;
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

	public MakerWorkResult TryMaker() throws Exception {
		// System.out.println("TryMaker");

		// ランダムに労働場所を選ぶ。
		FactoryRoom factoryRoom;
		{
			ArrayList<FactoryRoom> list = mm.GetFactoryRoomList(this.moveMethod, HumanDef.maxMoveTimeForWork, this.currentRoom);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryMaker : There are no work place as maker");
			int index = OtherUtility.rand.nextInt(num);
			factoryRoom = list.get(index);
		}

		// 要求している労働者を調べる。
		CallForMaker cfm = null;
		{
			cfm = factoryRoom.GetDesiredMaker();
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

		MakerWorkResult wwr = new MakerWorkResult(factoryRoom, cfm, moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
		return wwr;
	}

	public void DoMaker(MakerWorkResult res) throws Exception {
		System.out.println(String.format("DoMaker : Made %s with $%f at %s", res.cfm.itemDef.GetName(), res.cfm.wage, res.factoryRoom.GetName()));

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		this.Move(res.factoryRoom);

		this.Make(res.cfm, false);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		this.wageMovingAverage.Add(this.timeSimulationComplete, moneyEnd - moneyStart);
		this.totalTimeWork += this.timeSimulationComplete - timeStart;
	}

	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	// Builderの仕事をやってみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////

	public void TryBuilder() throws HumanSimulationException {
		// 建築中のビルを探す。
		Building building;
		{
			ArrayList<Building> list = mm.GetNotCompletedBuildingList(this.moveMethod, HumanDef.maxMoveTimeForWork, this.currentRoom);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryMaker : There are no work place as maker");
			int index = OtherUtility.rand.nextInt(num);
			building = list.get(index);
		}

		// 要求している労働者を調べる。
		building.GetDesiredBuilderList();
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
	// アイテムを消費してみる。
	// //////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////
	public class ConsumeResult extends TryResult {
		ShopRoom shopRoom;
		ItemCatalog itemCatalog;

		public ConsumeResult(ShopRoom shopRoom, ItemCatalog itemCatalog, double moneyStart, double moneyEnd, long timeStart, long timeEnd,
				double utilStart, double utilEnd) {
			super(moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
			this.shopRoom = shopRoom;
			this.itemCatalog = itemCatalog;
		}

		@Override
		public boolean equals(java.lang.Object obj) {
			if (obj instanceof ConsumeResult == false) return false;
			ConsumeResult res = (ConsumeResult) obj;
			if (this.shopRoom == res.shopRoom) {
				if (this.itemCatalog.itemDef.GetName().equals(res.itemCatalog.itemDef.GetName())) { return true; }
			}
			return false;
		}

		@Override
		public int hashCode() {
			int ret = this.shopRoom.hashCode() + this.itemCatalog.itemDef.GetName().hashCode();
			return ret;
		}

	}

	public ConsumeResult TryConsume() throws Exception {
		// 食べる場所を決定する。
		ShopRoom consumeRoom;
		if (true) {
			ArrayList<ShopRoom> list = mm.GetConsumableRoomList(moveMethod, HumanDef.maxMoveTimeForEat, money, timeSimulationComplete, currentRoom);
			int num = list.size();
			if (num == 0) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
			int index = OtherUtility.rand.nextInt(num);
			consumeRoom = list.get(index);
		}

		// 食べる物を決定する。
		ItemCatalog consumeItemCatalog;
		if (true) {
			consumeItemCatalog = consumeRoom.GetProductItem(money, true);
			if (consumeItemCatalog == null) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
			if (consumeItemCatalog.itemDef instanceof ConsumeDef == false) throw new HumanSimulationException(
					"TryConsume : There are no eat place to get food");
			if (consumeItemCatalog.lotmax < 1) throw new HumanSimulationException("TryConsume : There are no eat place to get food");
		}

		double moneyStart = this.money;
		long timeStart = this.timeSimulationComplete;
		double utilStart = this.ComputeUtility();

		Move(consumeRoom);

		Consume(consumeItemCatalog, true);

		double moneyEnd = this.money;
		long timeEnd = this.timeSimulationComplete;
		double utilEnd = this.ComputeUtility();

		ConsumeResult res = new ConsumeResult(consumeRoom, consumeItemCatalog, moneyStart, moneyEnd, timeStart, timeEnd, utilStart, utilEnd);
		return res;
	}

	public void DoConsume(ConsumeResult res) throws Exception {
		System.out.println(String.format("DoConsume : Consumed %s with $%f at %s", res.itemCatalog.itemDef.GetName(), res.itemCatalog.price,
				res.shopRoom.GetName()));
		Move(res.shopRoom);
		Consume(res.itemCatalog, false);
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

		long timeDelta = shopRoom.GetDurationToBuy();
		Item item = shopRoom.BuyProductItem(this.timeSimulationComplete, this.money, itemCatalog, 1, simulation);
		this.utilityManager.AddUtility(itemCatalog.itemDef.GetUtilities(), this.timeSimulationComplete, itemCatalog.price);

		money -= itemCatalog.price * item.GetQuantity();

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Buy(ItemCatalog itemCatalog, int numPick, boolean simulation) throws Exception {
		ShopRoom shopRoom = (ShopRoom) this.currentRoom;

		long timeDelta = shopRoom.GetDurationToBuy();
		Item item = shopRoom.BuyProductItem(this.timeSimulationComplete, this.money, itemCatalog, numPick, simulation);
		this.inventory.Put(item);

		money -= itemCatalog.price * item.GetQuantity();

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Sell(ItemDef itemDef, double price, int numPick, boolean simulation) throws Exception {
		DeliverRoom deliverRoom = (DeliverRoom) this.currentRoom;

		long timeDelta = deliverRoom.GetDurationToSell(itemDef);
		Item item = this.inventory.Get(itemDef, numPick);
		deliverRoom.SellItem(this.timeSimulationComplete, item, price, simulation);

		money += price * item.GetQuantity();

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Work(CallForWorker cfw, boolean simulation) throws Exception {
		RunnableRoom runnableRoom = (RunnableRoom) this.currentRoom;

		WorkResult result = runnableRoom.Work(cfw.GetSkill(), this.timeSimulationComplete, simulation);
		long timeDelta = result.duration;

		money += result.gain;

		timeSimulationComplete += timeDelta;

		this.utilityMovingAverage.Add(this.timeSimulationComplete, this.ComputeUtility());
	}

	public void Make(CallForMaker cfm, boolean simulation) throws Exception {
		FactoryRoom factoryRoom = (FactoryRoom) this.currentRoom;

		MakeResult result = factoryRoom.Make(cfm, this.timeSimulationComplete, simulation);
		long timeDelta = result.duration;

		money += result.gain;

		timeSimulationComplete += timeDelta;

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
