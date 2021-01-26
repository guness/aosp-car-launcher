/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.carlauncher;

import static android.app.WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW;

import static com.android.wm.shell.ShellTaskOrganizer.TASK_LISTENER_TYPE_FULLSCREEN;

import android.annotation.UiContext;
import android.app.ActivityTaskManager;
import android.app.Application;
import android.app.TaskInfo;
import android.content.Context;
import android.util.Slog;
import android.window.TaskAppearedInfo;

import com.android.car.internal.common.UserHelperLite;
import com.android.wm.shell.FullscreenTaskListener;
import com.android.wm.shell.ShellTaskOrganizer;
import com.android.wm.shell.TaskView;
import com.android.wm.shell.TaskViewFactory;
import com.android.wm.shell.TaskViewFactoryController;
import com.android.wm.shell.common.HandlerExecutor;
import com.android.wm.shell.common.SyncTransactionQueue;
import com.android.wm.shell.common.TransactionPool;

import java.util.List;
import java.util.function.Consumer;

public class CarLauncherApplication extends Application {
    private static final String TAG = "CarLauncher";
    private static final boolean DBG = false;

    private TaskViewFactory mTaskViewFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!UserHelperLite.isHeadlessSystemUser(getUserId())) {
            initWmShell();
        }
    }

    private void initWmShell() {
        HandlerExecutor mainExecutor = new HandlerExecutor(getMainThreadHandler());
        ShellTaskOrganizer taskOrganizer = new ShellTaskOrganizer(mainExecutor, this);
        FullscreenTaskListener fullscreenTaskListener =
                new FullscreenTaskListener(
                        new SyncTransactionQueue(new TransactionPool(), mainExecutor));
        taskOrganizer.addListenerForType(fullscreenTaskListener, TASK_LISTENER_TYPE_FULLSCREEN);
        List<TaskAppearedInfo> taskAppearedInfos = taskOrganizer.registerOrganizer();
        cleanUpExistingTaskViewTasks(taskAppearedInfos);

        mTaskViewFactory = new TaskViewFactoryController(taskOrganizer, mainExecutor)
                .getTaskViewFactory();
    }

    void createTaskView(@UiContext Context context, Consumer<TaskView> onCreate) {
        mTaskViewFactory.create(context, getMainExecutor(), onCreate);
    }

    private static void cleanUpExistingTaskViewTasks(List<TaskAppearedInfo> taskAppearedInfos) {
        ActivityTaskManager atm = ActivityTaskManager.getInstance();
        for (TaskAppearedInfo taskAppearedInfo : taskAppearedInfos) {
            TaskInfo taskInfo = taskAppearedInfo.getTaskInfo();
            // Only TaskView tasks have WINDOWING_MODE_MULTI_WINDOW.
            if (taskInfo.getWindowingMode() == WINDOWING_MODE_MULTI_WINDOW) {
                if (DBG) Slog.d(TAG, "Found the dangling task, removing: " + taskInfo.taskId);
                atm.removeTask(taskInfo.taskId);
            }
        }
    }
}