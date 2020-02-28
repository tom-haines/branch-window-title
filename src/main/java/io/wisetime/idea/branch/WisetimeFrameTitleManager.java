package io.wisetime.idea.branch;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.impl.PlatformFrameTitleBuilder;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class WisetimeFrameTitleManager extends PlatformFrameTitleBuilder {

  @Override
  public String getProjectTitle(@NotNull Project project) {
    final String currentBranch = Optional.ofNullable(ServiceManager.getService(project, BranchHelper.class))
        .map(BranchHelper::getCurrentBranchName)
        .orElse(null);
    return getProjectTitleWithBranch(project, currentBranch);
  }

  String getProjectTitleWithBranch(Project project, String branchName) {
    String projectTitle = super.getProjectTitle(project);
    if (branchName != null && !branchName.isEmpty()) {
      projectTitle = projectTitle.replaceAll("^(.*)\\[.*]$", "$1[" + branchName + "]");
    }
    return projectTitle;
  }

}
