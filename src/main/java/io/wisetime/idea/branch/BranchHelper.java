package io.wisetime.idea.branch;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;

@Service(Level.PROJECT)
public final class BranchHelper {

  private String currentBranchName;

  public String getCurrentBranchName() {
    return currentBranchName;
  }

  public void onBranchChanged(String branchName) {
    currentBranchName = branchName;
  }

}
