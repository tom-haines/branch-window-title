/*
 * Copyright (c) 2021 Practice Insight Pty Ltd. All Rights Reserved.
 */

package io.wisetime.idea.branch.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts.ConfigurableName;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

/**
 * @author vadym
 */
public class SettingsConfigurable implements Configurable {

  private SettingsPanel settingsPanel;

  @Override
  public @ConfigurableName String getDisplayName() {
    return "Branch Name Settings";
  }

  @Override
  public @Nullable JComponent createComponent() {
    settingsPanel = new SettingsPanel();
    return settingsPanel.getPanel();
  }

  @Override
  public boolean isModified() {
    return settingsPanel != null && settingsPanel.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    settingsPanel.apply();
  }

  @Override
  public void disposeUIResources() {
    settingsPanel = null;
  }

  @Override
  public void reset() {
    settingsPanel.reset();
  }
}
