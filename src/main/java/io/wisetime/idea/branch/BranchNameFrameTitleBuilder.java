package io.wisetime.idea.branch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.FrameTitleBuilder;
import com.intellij.openapi.wm.impl.IdeFrameImpl;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BranchNameFrameTitleBuilder extends FrameTitleBuilder {
  private static FrameTitleBuilder defaultBuilder;

  // static singleton of thread-safe Pattern object
  private static final Pattern FIRST_LINE_PATTERN = Pattern.compile("(ref: )?(.+)[\\r\\n]*");

  private static final Pattern GIT_DIR_LINE_PATTERN = Pattern.compile("(gitdir: )?(.+)[\\r\\n]*");

  // maintains list of unique .git/HEAD file paths being watched
  private final Set<String> gitHeadFileWatchSet;

  private final Map<String, String> gitHeadFile2ProjectName;

  public BranchNameFrameTitleBuilder() {
    gitHeadFileWatchSet = new HashSet<String>();
    gitHeadFile2ProjectName = new HashMap<>();
  }

  static void setDefaultBuilder(FrameTitleBuilder defaultBuilder) {
    BranchNameFrameTitleBuilder.defaultBuilder = defaultBuilder;
  }

  /**
   * watch the root .git/HEAD or .git/worktrees/%worktree_name%/HEAD for changes (if it exists)
   *
   * @return gitHeadFile if exists or null otherwise.
   */
  private VirtualFile watchThisProject(Project project) {
    VirtualFile gitFile = project.getBaseDir().findChild(".git");
    if (gitFile != null) {
      VirtualFile file = processGitFile(gitFile);
      if (file != null) {
        gitHeadFile2ProjectName.put(file.getCanonicalPath(), project.getName());
      }
      return file;
    }
    return null;
  }

  private VirtualFile processGitFile(VirtualFile gitFile) {
    VirtualFile gitHeadSymRefFile = gitFile.findChild("HEAD");
    if (gitHeadSymRefFile != null) {
      registerFileChangedListener(gitHeadSymRefFile.getCanonicalPath());
      return gitHeadSymRefFile;
    } else {
      // Try to check if git worktree is present
      VirtualFile dirDir = determineGitDir(gitFile);
      if (dirDir != null) {
        return processGitFile(dirDir);
      }
    }
    return null;
  }

  /**
   * Maintain watch on gitHeadFile via {@link VirtualFileManager#addVirtualFileListener(VirtualFileListener)}.
   */
  private void registerFileChangedListener(final String gitHeadFilePath) {
    if (!gitHeadFileWatchSet.contains(gitHeadFilePath)) {
      gitHeadFileWatchSet.add(gitHeadFilePath);
      VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
        @Override
        public void contentsChanged(@NotNull VirtualFileEvent event) {
          if (gitHeadFilePath.equals(event.getFile().getCanonicalPath())) {
            final String branchName = determineBranchName(event.getFile());
            updateFrameTitle(getProjectForFile(event.getFile()), branchName);
          }
        }
      });
    }
  }

  /**
   * This fires once when a project is opened.  The hook is used to setup a watch for .git/HEAD changes.
   *
   * @return The project title to use for the given project.
   */
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

  /**
   * Given a branchName and a project, create a modified project name.
   */
  private String createProjectTitle(@NotNull Project project, String branchName) {
    if (branchName != null) {
      // decorate the default builder
      return defaultBuilder.getProjectTitle(project).replace(" [", " " + branchName + " - [");
    } else {
      return defaultBuilder.getProjectTitle(project);
    }
  }

  /**
   * Use default builder implementation (no change).
   */
  @Override
  public String getFileTitle(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return defaultBuilder.getFileTitle(project, virtualFile);
  }

  private String determineBranchName(VirtualFile headFile) {
    if (headFile != null) {
      try {
        String headLinkAsString = new String(headFile.contentsToByteArray());
        Matcher matcher = FIRST_LINE_PATTERN.matcher(headLinkAsString);
        if (matcher.find() && matcher.groupCount() >= 2) {
          String branchName = matcher.group(2).trim();
          branchName = removePrefix("refs/", branchName);
          branchName = removePrefix("heads/", branchName);
          return branchName;
        }
      } catch (Exception e) {
        // ignore
      }
    }

    // could not determine branch name, returning null
    return null;
  }

  private VirtualFile determineGitDir(VirtualFile gitFile) {
    try {
      String gitFileAsString = new String(gitFile.contentsToByteArray(), Charset.forName("UTF-8"));
      Matcher matcher = GIT_DIR_LINE_PATTERN.matcher(gitFileAsString);
      if (matcher.find() && matcher.groupCount() >= 2) {
        String gitDir = matcher.group(2).trim();
        return LocalFileSystem.getInstance().findFileByPath(gitDir);
      }
    } catch (Exception e) {
      // ignore
    }

    // could not determine git directory, returning null
    return null;
  }

  private void updateFrameTitle(Project project, String branchName) {
    if (project != null) {
      String projectTitle = createProjectTitle(project, branchName);
      ((IdeFrameImpl) WindowManager.getInstance().getIdeFrame(project)).setTitle(projectTitle);
    }
  }

  /**
   * Look through all open projects and see if git head symlink file is contained in it.
   */
  private Project getProjectForFile(VirtualFile gitHeadFile) {
    String gitHeadFilePath = gitHeadFile.getCanonicalPath();
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      try {
        VirtualFile[] contentRootArray = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile virtualFile : contentRootArray) {
          String expectedLoc = virtualFile.getCanonicalPath() + "/.git/HEAD";
          if (expectedLoc.equals(gitHeadFilePath)) {
            return project;
          } else if (gitHeadFile2ProjectName.containsKey(gitHeadFilePath)) {
            if (gitHeadFile2ProjectName.get(gitHeadFilePath).equals(project.getName())) {
              return project;
            }
          }
        }
      } catch (Exception e) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Utility function to tidy-up common repetition of sym-link path to current branch.
   */
  private String removePrefix(String prefix, String branchName) {
    if (branchName.startsWith(prefix) && branchName.length() > prefix.length()) {
      return branchName.substring(prefix.length());
    }
    return branchName;
  }

}
