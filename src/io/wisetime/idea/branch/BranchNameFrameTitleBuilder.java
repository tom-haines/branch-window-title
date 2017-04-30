package io.wisetime.idea.branch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.FrameTitleBuilder;
import com.intellij.openapi.wm.impl.IdeFrameImpl;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BranchNameFrameTitleBuilder extends FrameTitleBuilder {
  private static FrameTitleBuilder defaultBuilder;

  private static final String REF_PREFIX = "refs/";
  private static final String HEAD_PREFIX = "heads/";
  private static final Pattern FIRST_LINE_PATTERN = Pattern.compile("(ref: )?(.+)[\\r\\n]*");

  public static void setDefaultBuilder(FrameTitleBuilder defaultBuilder) {
    BranchNameFrameTitleBuilder.defaultBuilder = defaultBuilder;
  }

  private Set<String> headFilesListeningTo = new HashSet<String>();

  // watch the root pom.xml for changes, if it exists
  private VirtualFile watchThisProject(Project project) {
    VirtualFile gitDir = project.getBaseDir().findChild(".git");
    if (gitDir != null) {
      VirtualFile headFile = gitDir.findChild("HEAD");
      if (headFile != null) {
        registerFileChangedListener(headFile.getCanonicalPath());
        return headFile;
      }
    }
    return null;
  }

  // this fires once when a project is opened, use this hook to watch the project's pom for changes
  @Override
  public String getProjectTitle(@NotNull Project project) {
    String branchName = null;
    try {
      VirtualFile headFile = watchThisProject(project);
      branchName = determineBranchName(headFile);
    } catch (Exception e) {
      // ignore...
    }
    return createProjectTitle(project, branchName);
  }

  private String createProjectTitle(Project project, String branchName) {
    if (branchName != null) {
      // decorate the default builder
      return defaultBuilder.getProjectTitle(project).replace(" [", " " + branchName + " - [");
    } else {
      return defaultBuilder.getProjectTitle(project);
    }
  }

  // no change
  @Override
  public String getFileTitle(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return defaultBuilder.getFileTitle(project, virtualFile);
  }

  private void registerFileChangedListener(final String headFileCanonicalPath) {
    if (!headFilesListeningTo.contains(headFileCanonicalPath)) {
      headFilesListeningTo.add(headFileCanonicalPath);
      VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
        @Override
        public void contentsChanged(@NotNull VirtualFileEvent event) {
          if (headFileCanonicalPath.equals(event.getFile().getCanonicalPath())) {
            final String branchName = determineBranchName(event.getFile());
            updateFrameTitle(getProjectForFile(event.getFile()), branchName);
          }
        }
      });
    }
  }

  private String determineBranchName(VirtualFile headFile) {
    if (headFile == null) {
      return null;
    }

    try {
      String headLinkAsString = new String(headFile.contentsToByteArray());
      Matcher matcher = FIRST_LINE_PATTERN.matcher(headLinkAsString);
      if (matcher.find() && matcher.groupCount() >= 2) {
        String branchName = matcher.group(2).trim();
        branchName = removePrefix(REF_PREFIX, branchName);
        branchName = removePrefix(HEAD_PREFIX, branchName);
        return branchName;
      }
    } catch (Exception e) {
      // ignore...
    }
    return null;
  }

  private String removePrefix(String prefix, String branchName) {
    if (branchName.startsWith(prefix) && branchName.length() > prefix.length()) {
      return branchName.substring(prefix.length());
    }
    return branchName;
  }

  private void updateFrameTitle(Project project, String branchName) {
    if (project != null) {
      String projectTitle = createProjectTitle(project, branchName);
      ((IdeFrameImpl) WindowManager.getInstance().getIdeFrame(project)).setTitle(projectTitle);
    }
  }

  private Project getProjectForFile(VirtualFile pomFile) {
    // look through all open projects and see if this file is contained in it
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      try {
        VirtualFile[] contentRootArray = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile virtualFile : contentRootArray) {
          String expectedLoc = virtualFile.getCanonicalPath() + "/.git/HEAD";
          if (expectedLoc.equals(pomFile.getCanonicalPath())) {
            return project;
          }
        }
      } catch (Exception e) {
        // ignore... directory index might not be initialized yet
      }
    }
    return null;
  }
}
