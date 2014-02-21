/*
 * Copyright 2014 JBoss Inc
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

package org.optaplanner.core.impl.heuristic.selector.value.decorator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.optaplanner.core.impl.domain.variable.descriptor.PlanningVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

public class SelectedCountLimitValueSelector extends AbstractValueSelector implements EntityIndependentValueSelector {

    protected final ValueSelector childValueSelector;
    protected final long selectedCountLimit;

    /**
     * Unlike most of the other {@link ValueSelector} decorations,
     * this one works for an entity dependent {@link ValueSelector} too.
     * @param childValueSelector never null, if any of the {@link EntityIndependentValueSelector} specific methods
     * are going to be used, this parameter must also implement that interface
     * @param selectedCountLimit at least 0
     */
    public SelectedCountLimitValueSelector(ValueSelector childValueSelector, long selectedCountLimit) {
        this.childValueSelector = childValueSelector;
        this.selectedCountLimit = selectedCountLimit;
        if (selectedCountLimit < 0L) {
            throw new IllegalArgumentException("The selector (" + this
                    + ") has a negative selectedCountLimit (" + selectedCountLimit + ").");
        }
        solverPhaseLifecycleSupport.addEventListener(childValueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public PlanningVariableDescriptor getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    public boolean isCountable() {
        return true;
    }

    public boolean isNeverEnding() {
        return false;
    }

    public long getSize(Object entity) {
        long childSize = childValueSelector.getSize(entity);
        return Math.min(selectedCountLimit, childSize);
    }

    public long getSize() {
        long childSize = ((EntityIndependentValueSelector) childValueSelector).getSize();
        return Math.min(selectedCountLimit, childSize);
    }

    public Iterator<Object> iterator(Object entity) {
        return new SelectedCountLimitValueIterator(childValueSelector.iterator(entity));
    }

    public Iterator<Object> iterator() {
        return new SelectedCountLimitValueIterator(((EntityIndependentValueSelector) childValueSelector).iterator());
    }

    private class SelectedCountLimitValueIterator extends SelectionIterator<Object> {

        private final Iterator<Object> childValueIterator;
        private long selectedSize;

        public SelectedCountLimitValueIterator(Iterator<Object> childValueIterator) {
            this.childValueIterator = childValueIterator;
            selectedSize = 0L;
        }

        @Override
        public boolean hasNext() {
            return selectedSize < selectedCountLimit && childValueIterator.hasNext();
        }

        @Override
        public Object next() {
            if (selectedSize >= selectedCountLimit) {
                throw new NoSuchElementException();
            }
            selectedSize++;
            return childValueIterator.next();
        }

    }

    @Override
    public String toString() {
        return "SelectedCountLimit(" + childValueSelector + ")";
    }

}
