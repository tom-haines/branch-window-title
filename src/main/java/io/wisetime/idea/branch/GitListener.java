package io.wisetime.idea.branch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import git4idea.GitLocalBranch;
import git4idea.GitReference;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class GitListener implements StartupActivity {

  private final Logger logger = Logger.getInstance("GitListener");

  @Override
  public void runActivity(@NotNull Project project) {
    project.getMessageBus().connect().subscribe(GitRepository.GIT_REPO_CHANGE, repository -> {
      GitLocalBranch currentBranch = repository.getCurrentBranch();
      if (currentBranch != null) {
        notifyBranchChanged(repository.getProject(), currentBranch.getName());
      }
    });
    Optional.ofNullable(GitBranchUtil.getCurrentRepository(project))
        .map(GitRepository::getCurrentBranch)
        .map(GitReference::getName)
        .ifPresent(branchName -> {
          notifyBranchChanged(project, branchName);
        });
  }

  private void notifyBranchChanged(Project project, String branchName) {
    final BranchHelper service = project.getService(BranchHelper.class);
    if (service == null) {
      logger.warn("Failed to notify branch change");
      return;
    }
    service.onBranchChanged(branchName);
  }
}
