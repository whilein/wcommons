/*
 *    Copyright 2021 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.flow;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.Executor;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractFlowItems extends AbstractFlow implements BaseFlowItems {

    protected AbstractFlowItems(final String name) {
        super(name);
    }

    protected final void _callAsync(final Executor executor) {
        executor.execute(this::call);
    }

    @Override
    public void call() {
        try {
            run();
        } catch (final Exception e) {
            throw new RuntimeException("Error occurred whilst executing flow " + name, e);
        }
    }

}