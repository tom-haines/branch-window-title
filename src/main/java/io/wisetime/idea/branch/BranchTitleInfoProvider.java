/*
 * Copyright (c) 2023 Practice Insight Pty Ltd. All Rights Reserved.
 */

package io.wisetime.idea.branch;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.impl.TitleInfoProvider;
import java.util.Optional;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

public class BranchTitleInfoProvider implements TitleInfoProvider {

  @NotNull
  @Override
  public String getBorderlessPrefix() {
    return " ";
  }

  @NotNull
  @Override
  public String getBorderlessSuffix() {
    return "";
  }

  @Override
  public void addUpdateListener(@NotNull Project project, @NotNull Disposable disposable,
      @NotNull Function1<? super TitleInfoProvider, Unit> callback) {
  }

  @NotNull
  @Override
  public String getValue(@NotNull Project project) {
    return Optional.ofNullable(project.getService(BranchHelper.class).getCurrentBranchName())
        .orElse("");
  }

  @Override
  public boolean isActive(@NotNull Project project) {
    return true;
  }

}
