import java.util.TreeMap;

public class Glass {

	private int row;
	private int column;
	private TreeMap<Double, Double> timesAndFlows;

	public Glass(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public Glass(int row, int column, TreeMap<Double, Double> timesAndFlows) {
		this.row = row;
		this.column = column;
		this.timesAndFlows = timesAndFlows;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public TreeMap<Double, Double> getTimesAndFlows() {
		return timesAndFlows;
	}

	public void setTimesAndFlows(TreeMap<Double, Double> timesAndFlows) {
		this.timesAndFlows = timesAndFlows;
	}

}
