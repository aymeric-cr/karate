/*
 * The MIT License
 *
 * Copyright 2018 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.intuit.karate.core;

import com.intuit.karate.StepDefs;
import com.intuit.karate.StringUtils;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author pthomas3
 */
public class StepExecutionUnit {

    private final Step step;
    private final StepDefs actions;
    private final ExecutionContext exec;

    public StepExecutionUnit(Step step, StepDefs actions, ExecutionContext exec) {
        this.step = step;
        this.actions = actions;
        this.exec = exec;
    }

    public void submit(Consumer<StepResult> next) {
        exec.system.accept(() -> {
            if (actions.callContext.executionHook != null) {
                actions.callContext.executionHook.beforeStep(step, actions.context);
            }
            Result result = Engine.executeStep(step, actions);
            List<FeatureResult> callResults = actions.context.getCallResults();
            actions.context.setCallResults(null); // clear
            // log appender collection for each step happens here
            String stepLog = StringUtils.trimToNull(exec.appender.collect());           
            StepResult stepResult = new StepResult(step, result, stepLog, callResults);            
            if (result.isAborted()) { // we log only aborts for visibility
                actions.context.logger.debug("abort at {}", step.getDebugInfo());
            }
            if (actions.callContext.executionHook != null) {
                actions.callContext.executionHook.afterStep(stepResult, actions.context);
            }            
            next.accept(stepResult);
        });
    }

}
