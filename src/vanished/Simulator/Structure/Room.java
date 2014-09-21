package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Inventory;

public class Room {

	RoomDef roomDef;

	// 所属するビル
	Building building;

	// 店が保有しているお金
	private double money = 0;

	// 過去1年のお金のトランザクションの平均
	private ExponentialMovingAverage moneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);

	// 部屋にいる人のリスト
	HumanExistRecordManager humanExistRoomManager = new HumanExistRecordManager();

	// 部屋にあるアイテムのリスト
	Inventory itemInventory = new Inventory();

	public Room(Building building, RoomDef roomDef) {
		this.roomDef = roomDef;
		this.building = building;
	}

	public void DumpStatus(long timeNow) {
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$                            Dump                         $$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("===Room===");
		System.out.println(roomDef.name);
		System.out.println("money : " + money);
	}

	// 所属するビルを返す。
	public Building GetParentBuilding() {
		return building;
	}

	public String GetName() {
		return roomDef.name;
	}

	// 部屋に入れるかどうか調べる。
	private boolean IsEnterable(long timeStart, long duration) {
		// 部屋に入れるかどうか調べる。
		int num = humanExistRoomManager.MaxNum(timeStart, duration);
		if (num + 1 >= roomDef.capacityHuman) return false;
		return true;
	}

	public void Enter(long timeStart, long duration, boolean simulation) throws HumanSimulationException {
		if (this.IsEnterable(timeStart, duration) == false) throw new HumanSimulationException("RunnableRoom.Work : human capacity is full");
		if (simulation == false) {
			humanExistRoomManager.Add(timeStart, duration);
		}
	}

	public void AddMoney(long timeNow, double add) {
		this.money += add;
		this.moneyMovingAverage.Add(timeNow, add);
	}

	public double GetMoney() {
		return this.money;
	}

	public double GetMoneyAverage(long timeNow) {
		return this.moneyMovingAverage.GetAverage(timeNow);
	}
}
