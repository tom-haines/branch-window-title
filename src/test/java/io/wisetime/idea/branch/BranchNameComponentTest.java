package io.wisetime.idea.branch;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author thomas.haines@practiceinsight.io
 */
public class BranchNameComponentTest {
  private BranchNameComponent component;
  private MutablePicoContainer mutablePicoContainer;

  @Before
  public void setUp() {
    BranchNameComponentMock extendedComponent = new BranchNameComponentMock();
    this.component = extendedComponent;
    this.mutablePicoContainer = extendedComponent.getMutable();

  }

  @Test
  public void initComponent() {
    component.initComponent();
    Mockito.verify(mutablePicoContainer, times(1)).unregisterComponent(anyString());
  }

  @Test
  public void initComponent_notMutable() {
    CastFailComponent castFail = new CastFailComponent();
    // should not throw exception
    castFail.initComponent();
  }

  static class BranchNameComponentMock extends BranchNameComponent {

    MutablePicoContainer picoContainerMock;

    BranchNameComponentMock() {
      picoContainerMock = Mockito.mock(MutablePicoContainer.class);
    }

    @NotNull
    @Override
    protected PicoContainer getPicoContainer() {
      return picoContainerMock;
    }

    MutablePicoContainer getMutable() {
      return picoContainerMock;
    }

  }

  static class CastFailComponent extends BranchNameComponent {

    PicoContainer picoContainerMock;

    CastFailComponent() {
      picoContainerMock = Mockito.mock(PicoContainer.class);
    }

    @Override
    @NotNull
    protected PicoContainer getPicoContainer() {
      return picoContainerMock;
    }

  }


}