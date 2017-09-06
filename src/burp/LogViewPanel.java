package burp;

import burp.dialog.ColorFilterDialog;
import burp.dialog.SavedFiltersDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by corey on 24/08/17.
 */
public class LogViewPanel extends JPanel {
    final JScrollPane logTableScrollPane;
    final LogTable logTable;
    final FilterPanel filterPanel;


    LogViewPanel(ArrayList<LogEntry> logEntries){
        this.setLayout(new GridBagLayout());

        logTable = new LogTable(logEntries);
        logTableScrollPane = new JScrollPane(logTable,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);//View
        logTableScrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                JScrollBar scrollBar = logTableScrollPane.getVerticalScrollBar();
                BurpExtender.getInstance().getLoggerPreferences().setAutoScroll(
                        scrollBar.getValue() + scrollBar.getHeight() >= scrollBar.getMaximum());
            }
        });
        logTableScrollPane.getVerticalScrollBar().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {}
            @Override
            public void mousePressed(MouseEvent mouseEvent) {}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                JScrollBar scrollBar = logTableScrollPane.getVerticalScrollBar();
                BurpExtender.getInstance().getLoggerPreferences().setAutoScroll(
                        scrollBar.getValue() + scrollBar.getHeight() >= scrollBar.getMaximum());
            }
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}
            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        filterPanel = new FilterPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        this.add(this.filterPanel, gbc);
        gbc.weighty = 999;
        gbc.gridy = 1;
        this.add(this.logTableScrollPane, gbc);
    }

    public LogTable getLogTable() {
        return logTable;
    }

    public FilterPanel getFilterPanel() {
        return filterPanel;
    }

    public JScrollPane getScrollPane() {
        return logTableScrollPane;
    }

    class FilterPanel extends JPanel {
        final private JHistoryField filterField;

        FilterPanel(){
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            this.filterField = new JHistoryField(15, "filterHistory");
            this.filterField.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if(e.getKeyChar() == KeyEvent.VK_ENTER){
                        BurpExtender.getInstance().setFilter((String) filterField.getSelectedItem());
                    }else {
                        super.keyReleased(e);
                    }
                }
            });
            this.filterField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    BurpExtender.getInstance().setFilter((String) filterField.getSelectedItem());
                }
            });

            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.weightx = gbc.weighty = 0;
            this.add(new JLabel(" Filter: "), gbc);

            gbc.gridx = 1;
            gbc.weightx = gbc.weighty = 99.0;
            this.add(filterField, gbc);

            final JButton filterButton = new JButton("Saved Filters");
            filterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    new SavedFiltersDialog().setVisible(true);
                }
            });

            gbc.gridx = 2;
            gbc.weightx = gbc.weighty = 2.0;
            this.add(filterButton, gbc);

            final JButton colorFilterButton = new JButton("Colorize");
            colorFilterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                new ColorFilterDialog(BurpExtender.getInstance().getFilterListeners()).setVisible(true);
                }
            });

            gbc.gridx = 3;
            gbc.weightx = gbc.weighty = 1.0;
            this.add(colorFilterButton, gbc);
        }

        public JHistoryField getFilterField() {
            return filterField;
        }
    }
}
