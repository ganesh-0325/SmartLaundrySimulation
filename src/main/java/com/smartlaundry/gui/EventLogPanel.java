package com.smartlaundry.gui;

import com.smartlaundry.model.EventRecord;
import com.smartlaundry.service.LaundryFacility;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;

public final class EventLogPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();

    public EventLogPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(UiTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        setPreferredSize(new Dimension(1000, 155));

        JLabel title = new JLabel("EVENT LOG — THREAD-AWARE SIMULATION OUTPUT");
        title.setFont(UiTheme.SECTION);
        title.setForeground(UiTheme.TEXT);
        add(title, BorderLayout.NORTH);

        textArea.setEditable(false);
        textArea.setFont(UiTheme.MONO);
        textArea.setForeground(UiTheme.TEXT);
        textArea.setBackground(ColorFor.textAreaBg());
        textArea.setLineWrap(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public void refresh(LaundryFacility facility) {
        StringBuilder sb = new StringBuilder();
        for (EventRecord event : facility.eventSnapshots()) sb.append(event.toDisplayString()).append('\n');
        textArea.setText(sb.toString());
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    private static final class ColorFor {
        private static java.awt.Color textAreaBg() { return java.awt.Color.WHITE; }
    }
}
