package vanished.Simulator.Structure;

import vanished.Simulator.EventLogManager;
import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;

public class Room {

	protected RoomDef roomDef;

	// 所属するビル
	private Building building;

	// 店が保有しているお金
	private double money = 0;

	// 過去1年のお金のトランザクションの平均
	private ExponentialMovingAverage inputMoneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);
	private ExponentialMovingAverage outputMoneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);

	// 部屋にいる人のリスト
	private HumanExistRecordManager humanExistRoomManager = new HumanExistRecordManager();

	public Room(Building building, RoomDef roomDef) {
		this.roomDef = roomDef;
		this.building = building;
	}

	public boolean IsReal() {
		return this.building.realFlag;
	}

	public void DumpStatus(long timeNow) {
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$                            Dump                         $$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("===Room===");
		System.out.println(roomDef.name);
		if (building.realFlag) {
			System.out.println("realFlag : 実体");
		} else {
			System.out.println("realFlag : 仮想");
		}

		if (building.buildCompletedFlag) {
			System.out.println("Building Status : 建築完了");
		} else {
			System.out.println("Building Status : 建築中");
		}
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
		double num = humanExistRoomManager.MaxNum(timeStart, duration);
		if (num + 1 > roomDef.capacityHuman) return false;
		return true;
	}

	public void Enter(long timeNow, long duration, boolean simulation) throws HumanSimulationException {
		if (this.IsEnterable(timeNow, duration) == false) throw new HumanSimulationException("RunnableRoom.Work : human capacity is full");
		if (simulation == false) {
			humanExistRoomManager.Add(timeNow, duration, 1);
		}
	}

	public void AddMoney(long timeNow, double add) {
		this.money += add;
		if (add > 0) {
			this.inputMoneyMovingAverage.Add(timeNow, add);
		} else {
			this.outputMoneyMovingAverage.Add(timeNow, -add);
		}
	}

	public double GetMoney() {
		return this.money;
	}

	public double GetInputMoneyMovingAverage(long timeNow) {
		return this.inputMoneyMovingAverage.GetAverage(timeNow);
	}

	public double GetOutputMoneyMovingAverage(long timeNow) {
		return this.outputMoneyMovingAverage.GetAverage(timeNow);
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		this.humanExistRoomManager.DiscardOldLog(timeNow);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	EventLogManager numHuman = new EventLogManager(this.toString() + "_human");

	public void WriteLog(long timeNow) throws Exception {
		double num = this.humanExistRoomManager.MaxNum(timeNow, 1);
		numHuman.Put(timeNow, num);
	}

}
