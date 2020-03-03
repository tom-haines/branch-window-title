package io.wisetime.idea.branch;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.RootUrlInfo;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.api.Url;

public class SubversionListener implements StartupActivity {

  private final Logger logger = Logger.getInstance("SubversionListener");

  @Override
  public void runActivity(@NotNull Project project) {
    project.getMessageBus().connect().subscribe(SvnVcs.ROOTS_RELOADED, mappingChanged -> {
      final SvnVcs svnVcs = SvnVcs.getInstance(project);
      if (svnVcs != null) {
        final VirtualFile rootDir = ProjectUtil.guessProjectDir(project);
        if (rootDir != null) {
          final Url branch = SvnUtil.getUrl(svnVcs, VfsUtilCore.virtualToIoFile(rootDir));
          if (branch != null) {
            final RootUrlInfo root = svnVcs.getSvnFileUrlMapping().getWcRootForUrl(branch);
            if (root != null) {
              final String relativePath = SvnUtil.getRelativePath(root.getRepositoryUrl().getPath(), branch.getPath());
              if (relativePath != null) {
                notifyBranchChanged(project, "^/" + relativePath);
              }
            }
          }
        }
      }
    });
  }

  private void notifyBranchChanged(Project project, String branchName) {
    final BranchHelper service = ServiceManager.getService(project, BranchHelper.class);
    if (service == null) {
      logger.warn("Failed to notify branch change");
      return;
    }
    service.onBranchChanged(branchName);
  }
}
