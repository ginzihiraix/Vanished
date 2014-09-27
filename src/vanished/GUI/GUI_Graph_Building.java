package vanished.GUI;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.Room;

public class GUI_Graph_Building {
	Building building;

	public GUI_Graph_Building(Building building) {

		this.building = building;

		try {
			String aaa;
			if (building.IsReal()) {
				aaa = "Åyé¿ëÃÅz";
			} else {
				aaa = "ÅyâºëzÅz";
			}

			String bbb;
			if (building.IsBuildingCompleted()) {
				bbb = "ÅyåöízäÆóπÅz";
			} else {
				bbb = "ÅyåöízíÜÅz";
			}

			JPanel p = new JPanel();
			p.setLayout(new GridLayout(3, 1));

			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					// JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset);
					XYSeries series = new XYSeries("Material A");
					series.add(0, 0);
					series.add(10, 1);
					series.add(5.5, 5);
					XYSeriesCollection dataset = new XYSeriesCollection(series);
					JFreeChart chart = ChartFactory.createXYLineChart("aaa", "bbb", "ccc", dataset, PlotOrientation.VERTICAL, true, false, false);
					p.add(new ChartPanel(chart));

					JFreeChart chart2 = ChartFactory.createXYLineChart("ddd", "ffff", "dddkk", dataset, PlotOrientation.VERTICAL, true, false, false);
					p.add(new ChartPanel(chart2));
				}
			}

			// JScrollPane sp = new JScrollPane();
			// sp.add(p);

			JFrame jf = new JFrame(building.GetName() + aaa + bbb);
			jf.setBounds(0, 0, 400, 600);
			jf.add(p);
			jf.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
