package io.wisetime.idea.branch;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.impl.FrameTitleBuilder;
import com.intellij.openapi.wm.impl.PlatformFrameTitleBuilder;

import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * The default intellij implementation of {@link FrameTitleBuilder} is {@link PlatformFrameTitleBuilder}.  This component
 * overrides {@link FrameTitleBuilder} with plugin implementation {@link BranchNameFrameTitleBuilder}.
 */
public class BranchNameComponent implements BaseComponent {
  private final Logger logger = Logger.getInstance("BranchNameComponent");

  @Override
  public void initComponent() {
    PicoContainer picoContainerObj = getPicoContainer();
    if (picoContainerObj instanceof MutablePicoContainer) {
      MutablePicoContainer picoContainer = (MutablePicoContainer) picoContainerObj;
      BranchNameFrameTitleBuilder.setDefaultBuilder(
          (FrameTitleBuilder) picoContainer.getComponentInstance(FrameTitleBuilder.class.getName())
      );

      picoContainer.unregisterComponent(FrameTitleBuilder.class.getName());
      picoContainer.registerComponentImplementation(FrameTitleBuilder.class.getName(), BranchNameFrameTitleBuilder.class);
    } else {
      logger.warn("unable to obtain MutablePicoContainer");
    }
  }

  @NotNull
  protected PicoContainer getPicoContainer() {
    return ApplicationManager.getApplication().getPicoContainer();
  }

}
