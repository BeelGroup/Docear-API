package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public class PropertiesTableModel extends DefaultTableModel implements TableModel {

		private static final long serialVersionUID = 1L;
		private Properties properties;
		private String[] columnName = {"Name", "Value"};
		String[] keys;
		
		
		public void load(Properties props) {
			this.properties = props;
			keys = properties.keySet().toArray(new String[]{});
			Arrays.sort(keys);
			fireTableDataChanged();
		}

		public int getRowCount() {
			if(properties == null) {
				return 0;
			}
			return properties.size();
		}



		@Override
		public int getColumnCount() {
			return 2;
		}



		@Override
		public String getColumnName(int columnIndex) {
			return columnName [columnIndex];
		}



		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}



		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}



		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0) {
				return keys[rowIndex];
			}
			return properties.get(keys[rowIndex]);
		}



		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			
		}

	}

	private JTable table;
	private PropertiesTableModel tableModel;
	public MainWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Properties-Viewer - ");
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnMain = new JMenu("Main");
		menuBar.add(mnMain);
		
		JMenuItem mntmLoad = new JMenuItem("Load");
		mntmLoad.addActionListener(new ActionListener() {			
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int resp = chooser.showOpenDialog(MainWindow.this);
				if(resp == JFileChooser.APPROVE_OPTION) {
					open(chooser.getSelectedFile());
				}
			}
		});
		mnMain.add(mntmLoad);
		
		mnMain.addSeparator();
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);	
			}
		});
		mnMain.add(mntmExit);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		JCheckBoxMenuItem chckbxmntmOrderAscending = new JCheckBoxMenuItem("Order Ascending");
		chckbxmntmOrderAscending.setSelected(true);
		mnView.add(chckbxmntmOrderAscending);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		tableModel = new PropertiesTableModel(); 
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		
		setSize(800,600);
		setVisible(true);
	}
	
	public void open(File file) {
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(file));
			tableModel.load(props);
			setTitle("Properties-Viewer - "+file);
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Could not process "+ file);
		}		
	}
	
}
