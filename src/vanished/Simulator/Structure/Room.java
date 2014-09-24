package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Inventory;

public class Room {

	protected RoomDef roomDef;

	// ��������r��
	private Building building;

	// �X���ۗL���Ă��邨��
	private double money = 0;

	// �ߋ�1�N�̂����̃g�����U�N�V�����̕���
	private ExponentialMovingAverage moneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);

	// �����ɂ���l�̃��X�g
	private HumanExistRecordManager humanExistRoomManager = new HumanExistRecordManager();

	// �����ɂ���A�C�e���̃��X�g
	private Inventory itemInventory = new Inventory();

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
		System.out.println("money : " + money);
	}

	// ��������r����Ԃ��B
	public Building GetParentBuilding() {
		return building;
	}

	public String GetName() {
		return roomDef.name;
	}

	// �����ɓ���邩�ǂ������ׂ�B
	private boolean IsEnterable(long timeStart, long duration) {
		// �����ɓ���邩�ǂ������ׂ�B
		double num = humanExistRoomManager.MaxNum(timeStart, duration);
		if (num + 1 >= roomDef.capacityHuman) return false;
		return true;
	}

	public void Enter(long timeStart, long duration, boolean simulation) throws HumanSimulationException {
		if (this.IsEnterable(timeStart, duration) == false) throw new HumanSimulationException("RunnableRoom.Work : human capacity is full");
		if (simulation == false) {
			humanExistRoomManager.Add(timeStart, duration, 1);
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
