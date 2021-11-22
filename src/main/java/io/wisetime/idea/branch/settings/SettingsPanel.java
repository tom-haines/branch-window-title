/*
 * Copyright (c) 2021 Practice Insight Pty Ltd. All Rights Reserved.
 */

package io.wisetime.idea.branch.settings;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import javax.swing.JPanel;

/**
 * @author vadym
 */
public class SettingsPanel {

  private final JBCheckBox windowTitle;
  private final JBCheckBox projectName;
  private JPanel root;

  public SettingsPanel() {
    root = new JPanel(new VerticalFlowLayout());
    windowTitle = new JBCheckBox("Append VCS branch name to window title");
    projectName = new JBCheckBox("Append VCS branch name to project name (Experimental)");
    projectName.addChangeListener(event -> {
      if (projectName.isSelected()) {
        windowTitle.setSelected(true);
      }
      windowTitle.setEnabled(!projectName.isSelected());
    });
    root.add(windowTitle);
    root.add(projectName);
  }

  public void reset() {
    SettingsState state = SettingsState.getInstance();
    windowTitle.setSelected(state.appendToWindowTitle);
    projectName.setSelected(state.appendToProjectName);
    windowTitle.setEnabled(!projectName.isSelected());
  }

  public void apply() {
    SettingsState state = SettingsState.getInstance();
    state.appendToWindowTitle = windowTitle.isSelected();
    state.appendToProjectName = projectName.isSelected();
  }

  public boolean isModified() {
    SettingsState state = SettingsState.getInstance();
    return state.appendToWindowTitle != windowTitle.isSelected()
        || state.appendToProjectName != projectName.isSelected();
  }

  public JPanel getPanel() {
    return root;
  }
}
