package com.smartlaundry.gui;

import com.smartlaundry.model.CustomerActivity;
import com.smartlaundry.service.LaundryFacility;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public final class ActivityPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Customer", "State", "Resource", "Time", "Reason"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);// conntect to the model

    public ActivityPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UiTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        setMinimumSize(new Dimension(0, 210));
        setPreferredSize(new Dimension(0, 220));

        JLabel title = new JLabel("LIVE CUSTOMER ACTIVITY");
        title.setFont(UiTheme.SECTION);
        title.setForeground(UiTheme.TEXT);
        add(title, BorderLayout.NORTH);

        table.setFillsViewportHeight(true); // scrool pannel bhitra available height acquire garxa
        table.setRowHeight(24);
        table.setFont(UiTheme.LABEL);
        table.getTableHeader().setFont(UiTheme.LABEL_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(105);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(220);
        add(new JScrollPane(table), BorderLayout.CENTER); // scroolabe areaa ma place garxa
    }

    public void refresh(LaundryFacility facility) {
        model.setRowCount(0);
        List<CustomerActivity> activities = facility.activitySnapshots();
        for (CustomerActivity activity : activities) {
            String time = activity.getEndNanos() > 0
                    ? String.format("%.1fs", activity.getRemainingSeconds())
                    : "—";
            model.addRow(new Object[]{activity.getCustomerId(), activity.getStage(), activity.getResource(), time, activity.getReason()});
        }
        if (activities.isEmpty()) {
            model.addRow(new Object[]{"—", "IDLE", "—", "—", "No active customer operations"});
        }
    }
}
