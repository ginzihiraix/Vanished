package vanished.Simulator.Structure;

import java.util.ArrayList;

import vanished.Simulator.OtherUtility;

public class Building {
	BuildingDef buildingDef;

	// ����Building�̌��݂��������Ă��邩�ۂ��̃t���O
	boolean buildCompletedFlag = false;

	// �r�����݂̂��߂̌��ݕ����B���݊���������p�Ȃ��B
	FactoryRoom buildRoom;

	// ��������Room�̈ꗗ
	ArrayList<Room> roomList = new ArrayList<Room>();

	long timeStart;

	public Building(long timeNow, BuildingDef buildingDef) throws Exception {
		this.buildingDef = buildingDef;

		buildRoom = new FactoryRoom(this, buildingDef.buildRoomDef, true);

		for (RoomDef roomDef : buildingDef.roomdefList) {
			Room room;
			if (roomDef instanceof FactoryRoomDef) {
				room = new FactoryRoom(this, (FactoryRoomDef) roomDef, false);
			} else {
				throw new Exception("faefaefawfwae");
			}
			roomList.add(room);
		}

		this.timeStart = timeNow;
	}

	public void CheckBuildingCompleted() {
		if (buildCompletedFlag == false) {
			if (buildRoom.shopStockManager.GetNumStock() >= 1) {
				System.out.println("�j�A���݊����I�I�I�I�I�I�I�I�I");
				buildRoom.DumpStatus(0);
				this.buildCompletedFlag = true;
			}
		}
	}

	public boolean CheckRecoverInitialCost(long timeNow) {
		if (timeNow < timeStart + OtherUtility.durationRecoverInitialCost) return true;
		double total = buildRoom.GetMoney();
		for (Room room : roomList) {
			total += room.GetMoney();
		}
		if (total < 0) return false;
		return true;
	}

	public ArrayList<Room> GetRoomList() {
		if (buildCompletedFlag == false) {
			ArrayList<Room> ret = new ArrayList<Room>();
			ret.add(buildRoom);
			return ret;
		} else {
			return roomList;
		}
	}
}
