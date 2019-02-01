package io.wisetime.idea.branch;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.wm.impl.FrameTitleBuilder;

import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;

public class BranchNameComponent implements ApplicationComponent {

  public static final String FRAME_TITLE_BUILDER = "com.intellij.openapi.wm.impl.FrameTitleBuilder";

  @Override
  public void initComponent() {
    MutablePicoContainer picoContainer = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
    BranchNameFrameTitleBuilder.setDefaultBuilder(
        (FrameTitleBuilder) picoContainer.getComponentInstance(FRAME_TITLE_BUILDER)
    );

    picoContainer.unregisterComponent(FRAME_TITLE_BUILDER);
    picoContainer.registerComponentImplementation(FRAME_TITLE_BUILDER, BranchNameFrameTitleBuilder.class);
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return getClass().getSimpleName();
  }
}
