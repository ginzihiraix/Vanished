package vanished.Simulator.Structure;

import java.util.ArrayList;

public class Building {
	BuildingDef buildingDef;

	// ����Building�̌��݂��������Ă��邩�ۂ��̃t���O
	boolean buildCompletedFlag = false;

	// �r�����݂̂��߂̌��ݕ����B���݊���������p�Ȃ��B
	FactoryRoom buildRoom;

	// ��������Room�̈ꗗ
	ArrayList<Room> roomList = new ArrayList<Room>();

	public Building(BuildingDef buildingDef) throws Exception {
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
	}

	public void CheckBuildingCompleted() {
		if (buildCompletedFlag == false) {
			if (buildRoom.shopStockManager.GetNumStock() >= 1) {
				buildRoom.DumpStatus(0);
				System.out.println("���݊���");
				this.buildCompletedFlag = true;
			}
		}
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
