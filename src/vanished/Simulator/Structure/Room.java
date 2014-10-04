package vanished.Simulator.Structure;

import vanished.Simulator.EventLogManager;
import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;

public class Room {

	protected RoomDef roomDef;

	// ��������r��
	private Building building;

	// �X���ۗL���Ă��邨��
	private double money = 0;

	// �ߋ�1�N�̂����̃g�����U�N�V�����̕���
	private ExponentialMovingAverage inputMoneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);
	private ExponentialMovingAverage outputMoneyMovingAverage = new ExponentialMovingAverage(60 * 24 * 365, true);

	// �����ɂ���l�̃��X�g
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
			System.out.println("realFlag : ����");
		} else {
			System.out.println("realFlag : ���z");
		}

		if (building.buildCompletedFlag) {
			System.out.println("Building Status : ���z����");
		} else {
			System.out.println("Building Status : ���z��");
		}
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
	// �C�x���g�L�^�p
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	EventLogManager numHuman = new EventLogManager(this.toString() + "_human");

	public void WriteLog(long timeNow) throws Exception {
		double num = this.humanExistRoomManager.MaxNum(timeNow, 1);
		numHuman.Put(timeNow, num);
	}

}
