package vanished.Simulator.Structure;

import java.util.ArrayList;

import vanished.Simulator.Location;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.Rect;
import vanished.Simulator.Item.ItemDef;

public class Building {
	BuildingDef buildingDef;

	// このBuildingの建設が完了しているか否かのフラグ
	boolean buildCompletedFlag = false;

	// ビル建設のための建設部屋。建設完了したら用なし。
	FactoryRoom buildRoom;

	// 所属するRoomの一覧
	ArrayList<Room> roomList = new ArrayList<Room>();

	// 建造された時刻
	long timeStart;

	// 3次元上の位置
	Location location;

	// 方角。{0,1,2,3} = {N,E,S,W}
	int direction = 0;

	// Real/Virtualフラグ
	boolean realFlag = false;

	public Building(long timeNow, BuildingDef buildingDef, Location location, int direction, boolean realFlag) throws Exception {
		this.buildingDef = buildingDef;

		if (buildingDef.nobuildFlag == false) {
			buildCompletedFlag = false;
			// buildRoom = new FactoryRoom(this, buildingDef.buildRoomDef, true);
		} else {
			buildCompletedFlag = true;
			buildRoom = null;
		}

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

		this.location = location;
		this.direction = direction;

		this.realFlag = realFlag;
	}

	public String GetName() {
		return buildingDef.name;
	}

	public boolean IsReal() {
		return realFlag;
	}

	public Rect GetLocation() {
		if (direction == 0) {
			Rect rect = new Rect(location.x, location.x + buildingDef.width, location.y, location.y + buildingDef.height);
			return rect;
		} else if (direction == 1) {
			Rect rect = new Rect(location.x, location.x + buildingDef.height, location.y - buildingDef.width, location.y);
			return rect;
		} else if (direction == 2) {
			Rect rect = new Rect(location.x - buildingDef.width, location.x, location.y - buildingDef.height, location.y);
			return rect;
		} else if (direction == 3) {
			Rect rect = new Rect(location.x - buildingDef.height, location.x, location.y, location.y + buildingDef.width);
			return rect;
		} else {
			return null;
		}
	}

	public boolean IsBuildingCompleted() {
		return buildCompletedFlag;
	}

	public void ChangeBuildingStatus() {
		if (buildCompletedFlag == false) {
			if (buildRoom.shopStockManager.GetNumStock() >= 1) {
				for (ItemDef itemDef : buildRoom.deliverStockManager.keySet()) {
					boolean flag = true;
					if (buildRoom.deliverStockManager.get(itemDef).GetNumStock() < 0) {
						flag = false;
					}
					if (flag == false) return;
				}
				System.out.println("祝、建設完成！！！！！！！！！");
				buildRoom.DumpStatus(0);
				this.buildCompletedFlag = true;
			}
		}
	}

	public boolean IsInitialCostRecovered(long timeNow) {
		if (timeNow < timeStart + OtherUtility.durationRecoverInitialCost) return true;
		double total = 0;
		if (buildRoom != null) {
			total += buildRoom.GetMoney();
		}
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

	public void WriteLog(long timeNow) throws Exception {
		if (this.buildRoom != null) {
			this.buildRoom.WriteLog(timeNow);
		}
		for (Room room : roomList) {
			room.WriteLog(timeNow);
		}
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		if (this.buildRoom != null) {
			this.buildRoom.DiscardOldLog(timeNow);
		}
		for (Room room : roomList) {
			room.DiscardOldLog(timeNow);
		}
	}
}
