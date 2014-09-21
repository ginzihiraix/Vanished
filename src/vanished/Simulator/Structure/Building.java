package vanished.Simulator.Structure;

import java.util.ArrayList;

public class Building {
	BuildingDef buildingDef;

	// このBuildingの建設が完了しているか否かのフラグ
	boolean buildCompletedFlag = false;

	// ビル建設のための建設部屋。建設完了したら用なし。
	FactoryRoom buildRoom;

	// 所属するRoomの一覧
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
				System.out.println("建設完成");
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
