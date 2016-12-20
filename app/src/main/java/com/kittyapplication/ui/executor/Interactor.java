/*
 * Copyright (c) 2016 riontech-xten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kittyapplication.ui.executor;

/**
 * Interactor to execute view task in UI thread
 * Created by @auther <b>riontech-xten</b> on 6/25/2016 3:33 PM
 * </b>vaghela.mithun@gmail.com</b>
 */
public interface Interactor {
    /**
     * Implement to listen when task has been completed
     */
    interface OnExecutionFinishListener{
        void onFinish();
    }

    /**
     * To execute task
     */
    void execute();
}
