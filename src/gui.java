import net.sourceforge.jcalendarbutton.JCalendarButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class gui extends JFrame {
    private JTextField apiKeyField;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JComboBox assetList;
    private JComboBox assetClassBox;
    private JLabel tickerLabel;
    private JLabel assetClassLabel;
    private JLabel apiKeyLabel;
    private JTable stocksDataTable;
    private JLabel frequencyLabel;
    private JComboBox frequencyBox;
    private JScrollPane tableScrollPane;
    private JButton getDataButton;
    private JTextArea consoleArea;
    private JLabel dateRangeLabel;
    private JCalendarButton fromDatePicker;
    private JTextField searchField;
    private JPanel bottomPanel;
    private JLabel consoleLabel;
    private JCheckBox searchTicker;
    private JTextField tickerBox;
    private JButton saveDataButton;
    private JPanel datePanel;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JCalendarButton toDatePicker;
    private JScrollPane consoleScrollPane;
    private polygon poly;
    private Date startDate;
    private Date endDate;

    //    create a constructor
    public gui () {
//      basic boilerplate stuff for configuring the gui
        setContentPane(mainPanel);
        setTitle("Polygon Front");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        // Initialize the markets dropdown list with values
        assetClassBox.addItem("stocks");
        assetClassBox.addItem("otc");
        assetClassBox.addItem("crypto");
        assetClassBox.addItem("fx");
        assetClassBox.addItem("indices");

        // Initialize the frequency dropdown list with values
        frequencyBox.addItem("minute");
        frequencyBox.addItem("second");
        frequencyBox.addItem("hour");
        frequencyBox.addItem("day");
        frequencyBox.addItem("week");
        frequencyBox.addItem("month");
        frequencyBox.addItem("quarter");
        frequencyBox.addItem("year");

        // table headers
        String[] columnNames = {"Timestamp", "Open", "High", "Low", "Close", "Volume", "Weighted Vol"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        stocksDataTable.setModel(tableModel);

        // listener to see if an API key has been added - this removes need for update button
        apiKeyField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String apiKey = apiKeyField.getText();
                poly = new polygon(apiKey);
                consoleArea.append("Polygon key: " + apiKey+ "\n");
            }
        });

        // Add action listener to asset class dropdown
        assetClassBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (poly != null) {
                    String selectedAsset = (String) assetClassBox.getSelectedItem();
                    try {
                        updateAssetDetails(selectedAsset, poly, false, "Null");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(gui.this, "Error updating asset details: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(gui.this, "Please enter an API key first.");
                }
            }
        });

        // search term listener
        // a way of refining the get tickers
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (poly != null) {
                    // get values from the user input fiekds
                    String selectedAsset    = (String) assetClassBox.getSelectedItem();
                    String searchTerm       = searchField.getText();
                    // call the api request function
                    try {
                        updateAssetDetails(selectedAsset, poly, true, searchTerm);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(gui.this, "Error updating asset details: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(gui.this, "Please enter an API key first.");
                }
            }
        });

        // get data button listener
        // when clicked this will take the data from the drop downs and pass it to the polygon object to make a request
        getDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // first get the fields that we need for the request
                Boolean state = tickerBox.isEnabled();
                String ticker;
                if (state == Boolean.FALSE){
                    ticker  = (String) assetList.getSelectedItem();
                } else {
                    ticker  = tickerBox.getText();
                }
                consoleArea.append("Ticker: " + ticker + "\n");
                String freq    = (String) frequencyBox.getSelectedItem();

                // we need to have some to and from dates too
                startDate = fromDatePicker.getTargetDate();
                endDate = toDatePicker.getTargetDate();
                // conver the dates to strings
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String from = formatter.format(startDate);
                String to = formatter.format(endDate);
                // print these values
                consoleArea.append("From date: " + from + "\n");
                consoleArea.append("To date: " + to + "\n");

                // make the API request
                try {
                    tableModel.setRowCount(0); // Clear previous data
                    List<Object[]> data = poly.getData(ticker, freq, from, to);
                    for (Object[] row : data) {
                        tableModel.addRow(row);
                    }

                    // Console output - response code and message
                    consoleArea.append("Response Code: " + poly.getResponseCode() + "\n");
                    consoleArea.append("Response Message: " + poly.getResponseMessage() + "\n");

                } catch (Exception ee) {  // Catch the exception
                    JOptionPane.showMessageDialog(gui.this, "Failed to fetch data: " + ee.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    consoleArea.append("Failed to fetch data: " + ee.getMessage() + "\n");
                }

            }
        });

        searchTicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get current state
                Boolean state = tickerBox.isEnabled();
                // swap them
                if (state == Boolean.TRUE) {
                    // disable the tickerbox
                    tickerBox.disable();
                    // enable the search
                    assetClassBox.enable();
                    assetList.enable();
                    searchField.enable();
                } else {
                    // disable the tickerbox
                    tickerBox.enable();
                    // enable the search
                    assetClassBox.disable();
                    assetList.disable();
                    searchField.disable();
                }
            }
        });

        // save button listener
        // opens a file picker to save the data
        saveDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // new file choser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Table Data");
                fileChooser.setSelectedFile(new File("stockData.csv")); // Default filename

                // saving process
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try (FileWriter fw = new FileWriter(fileToSave)) {
                        TableModel model = stocksDataTable.getModel();

                        // Write column names
                        for (int i = 0; i < model.getColumnCount(); i++) {
                            fw.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "," : "\n"));
                        }

                        // Write rows
                        for (int i = 0; i < model.getRowCount(); i++) {
                            for (int j = 0; j < model.getColumnCount(); j++) {
                                fw.write(model.getValueAt(i, j).toString() + (j < model.getColumnCount() - 1 ? "," : "\n"));
                            }
                        }

                        consoleArea.append("Table data saved successfully!\n");
                    } catch (IOException ex) {
                        consoleArea.append("Error saving file: " + ex.getMessage() + "\n");
                    }
                }
            }
        });
    }

    // listener for selecting a drop down from asset classes to request the
    // list from polygon and populate the next list beneth it
    private void updateAssetDetails(String assetClass, polygon poly, Boolean search, String searchTerm) throws Exception {
        List<String> assetDetails;

        // call api
        if (search == true) {
            assetDetails = poly.fetchAssetsSearch(assetClass, searchTerm);
        } else {
            assetDetails = poly.fetchAssets(assetClass);
        }


        // Update the second dropdown
        assetList.removeAllItems();
        for (String detail : assetDetails) {
            if (assetClass !=  "stocks") {
                assetList.addItem(detail.substring(2));
            } else {
                assetList.addItem(detail);
            }
        }
    }

//      Main entry point to the class
    public static void main(String[] args) {
        new gui();
    }
}
