package vanished.GUI;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import vanished.Simulator.EventLogManager.EventLog;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.Room;

public class GUI_Graph_Building {
	Building building;

	public GUI_Graph_Building(Building building) {

		this.building = building;

		try {

			int numRoom = 0;
			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					numRoom++;
				}
			}

			JPanel basePanel = new JPanel();
			basePanel.setLayout(new GridLayout(numRoom, 1));
			// basePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

			for (Room room : building.GetRoomList()) {

				if (false) {
					JLabel label = new JLabel(room.GetName());
					// label.setSize(400, 30);
					basePanel.add(label);
				}

				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					XYSeriesCollection dataset = new XYSeriesCollection();

					// Maker
					// if (false) {
					// ArrayList<EventLog> ml = factoryRoom.GetNumMakeLog(10000);
					// XYSeries series = new XYSeries("maker");
					// for (EventLog el : ml) {
					// double day = el.time / (60.0 * 24.0);
					// series.add(day - 0.001, 0);
					// series.add(day, el.value);
					// series.add(day + 0.001, 0);
					// }
					// dataset.addSeries(series);
					// }

					// Product
					// {
					// ArrayList<EventLog> ml = factoryRoom.GetNumProcutLog(10000);
					// XYSeries series = new XYSeries("product");
					// for (EventLog el : ml) {
					// double day = el.time / (60.0 * 24.0);
					// series.add(day, el.value);
					// }
					// dataset.addSeries(series);
					// }

					JFreeChart chart = ChartFactory.createXYLineChart(factoryRoom.GetProductItemName(), "time(day)", "#num of items", dataset,
							PlotOrientation.VERTICAL, true, false, false);

					// É^ÉCÉgÉãÇÃÉtÉHÉìÉgÇê›íËÇ∑ÇÈ
					chart.setTitle(new TextTitle(factoryRoom.GetProductItemName(), new Font("MS Gothic", Font.PLAIN, 12)));

					XYPlot plot = chart.getXYPlot();
					// Xé≤ÇÃçÄñ⁄ÇÃÉtÉHÉìÉgÇÃê›íË
					ValueAxis domainAxis = plot.getDomainAxis();
					domainAxis.setTickLabelFont(new Font("MS Gothic", Font.PLAIN, 12));
					domainAxis.setLabelFont(new Font("MS Gothic", Font.PLAIN, 12));

					// yé≤ÇÃçÄñ⁄ñºÇÃÉtÉHÉìÉgÇÃê›íË
					ValueAxis valueAxis = plot.getRangeAxis();
					valueAxis.setTickLabelFont(new Font("MS Gothic", Font.PLAIN, 12));
					valueAxis.setLabelFont(new Font("MS Gothic", Font.PLAIN, 12));

					// ñ}ó·ÇÃÉtÉHÉìÉgÇÃê›íË
					LegendTitle lt = chart.getLegend();
					lt.setItemFont(new Font("MS Gothic", Font.PLAIN, 12));

					ChartPanel chartPanel = new ChartPanel(chart);

					basePanel.add(chartPanel);
				}
			}

			// JScrollPane sp = new JScrollPane();
			// sp.add(p);

			String isRealStr;
			if (building.IsReal()) {
				isRealStr = "Åyé¿ëÃÅz";
			} else {
				isRealStr = "ÅyâºëzÅz";
			}

			String isBuildingCompletedStr;
			if (building.IsBuildingCompleted()) {
				isBuildingCompletedStr = "ÅyåöízäÆóπÅz";
			} else {
				isBuildingCompletedStr = "ÅyåöízíÜÅz";
			}

			JFrame jf = new JFrame(building.GetName() + isRealStr + isBuildingCompletedStr);
			jf.setBounds(0, 0, 400, 600);
			jf.add(basePanel);
			jf.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
