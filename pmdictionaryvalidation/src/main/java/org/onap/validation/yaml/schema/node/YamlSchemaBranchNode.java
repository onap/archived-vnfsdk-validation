/*
 * Copyright 2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.validation.yaml.schema.node;

import org.onap.validation.yaml.exception.YamlProcessingException;
import org.onap.validation.yaml.model.YamlDocument;
import org.onap.validation.yaml.model.YamlDocumentFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class YamlSchemaBranchNode extends YamlSchemaNode {

    private final YamlDocument nextNodesInLazyForm;
    private Optional<List<YamlSchemaNode>> nextNodes;

    YamlSchemaBranchNode(String name, String path, boolean required, String comment,
                         YamlDocument nextNodesInLazyForm) {
        super(name, path, required, comment);
        this.nextNodesInLazyForm = nextNodesInLazyForm;
        this.nextNodes = Optional.empty();
    }

    @Override
    public boolean isContainingSubStructure() {
        return true;
    }

    @Override
    public List<String> getAcceptedValues() {
        return Collections.emptyList();
    }

    @Override
    public synchronized List<YamlSchemaNode> getNextNodes() throws YamlSchemaProcessingException {
        try {
            return nextNodes.orElseGet(this::loadNextNodes);
        } catch (YamlSchemaLazyLoadingException lazyLoadingException) {
            throw new YamlSchemaProcessingException(lazyLoadingException);
        }
    }

    private List<YamlSchemaNode> loadNextNodes() {
        try {
            List<YamlSchemaNode> loadedNextNodes = new ArrayList<>();
            for (String key : nextNodesInLazyForm.getYaml().keySet()) {
                YamlDocument substructure = new YamlDocumentFactory()
                    .createYamlDocument(nextNodesInLazyForm.getYaml().get(key));
                loadedNextNodes.add(new YamlSchemaNodeFactory().createNode(key, getPath() + getName() + "/", substructure));
            }
            nextNodes = Optional.of(loadedNextNodes);
            return loadedNextNodes;
        } catch (YamlProcessingException e) {
            throw new YamlSchemaLazyLoadingException("Lazy loading failed, due to yaml parsing exception.",e);
        }
    }

    static class YamlSchemaLazyLoadingException extends RuntimeException {
        YamlSchemaLazyLoadingException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
