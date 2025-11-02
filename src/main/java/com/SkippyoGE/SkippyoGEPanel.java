package com.SkippyoGE;

import net.runelite.client.ui.PluginPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SkippyoGEPanel extends PluginPanel
{
    // You can add buttons, text, and other UI elements here later.
    // For now, we'll just add a welcome message.
    public SkippyoGEPanel()
    {
        super();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("SkippyoGE Panel");
        title.setHorizontalAlignment(JLabel.CENTER);

        titlePanel.add(title, BorderLayout.NORTH);

        add(titlePanel, BorderLayout.NORTH);
    }
}