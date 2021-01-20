/*
 * Copyright 2021 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.cvc.csar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ReleasesResolver {
    public static final String SPLIT_BY_COMMA = ",";
    public static final String SPECIAL_KEYWORD = "latest";
    public static final int NOT_FOUND = -1;
    private final String[] releaseInOrder;

    private ReleasesResolver(String[] releaseInOrder) {
        this.releaseInOrder = releaseInOrder;
    }

    public static ReleasesResolver create(String releaseInOrder) {
        return new ReleasesResolver(releaseInOrder.split(SPLIT_BY_COMMA));
    }

    public List<String> resolveWithAncestors(String release) {
        final int index = findIndexOf(release);
        if (isReleaseAvailable(index)) {
            throw new IllegalArgumentException(String.format("Release '%s' is not defined at the releases.order list in a vnfreqs.properties file!", release));
        }
        return new LinkedList<>(getElements(this.releaseInOrder, index));
    }

    private boolean isReleaseAvailable(int index) {
        return index == NOT_FOUND;
    }

    private List<String> getElements(String[] elements, int index) {
        return Arrays.asList(elements).subList(0, index + 1);
    }

    private int findIndexOf(String release) {
        if (release.equals(SPECIAL_KEYWORD)) {
            return getIndexOfLastElement();
        }
        return Arrays.asList(this.releaseInOrder).indexOf(release);
    }

    private int getIndexOfLastElement() {
        return this.releaseInOrder.length - 1;
    }
}
