package io.wisetime.idea.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author thomas.haines
 */
public class BranchNameFrameTitleBuilderTest {

  @Test
  public void testLogMsgCreate() {
    final VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
    final String pathInfo = "/some/path";
    when(virtualFile.getPath())
        .thenReturn(pathInfo);

    assertThat(BranchNameFrameTitleBuilder.createLogMsg(virtualFile))
        .contains(pathInfo);
  }

}
