/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.dmn.api.definition.v1_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.dmn.api.property.dmn.Description;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.api.property.dmn.QName;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;

public abstract class DMNElement extends DMNModelInstrumentedBase {

    @Property
    @FormField(readonly = true)
    @Valid
    protected Id id;

    @Property
    @FormField(afterElement = "id")
    @Valid
    protected Description description;

    private Map<QName, String> otherAttributes = new HashMap<>();

    private DMNElement.ExtensionElements extensionElements;

    public DMNElement() {
    }

    public DMNElement(final Id id,
                      final Description description) {
        this.id = id;
        this.description = description;
    }

    // -----------------------
    // DMN properties
    // -----------------------

    public Id getId() {
        return id;
    }

    public void setId(final Id id) {
        this.id = id;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(final Description description) {
        this.description = description;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public ExtensionElements getExtensionElements() {
        return extensionElements;
    }

    public void setExtensionElements(final ExtensionElements extensionElements) {
        this.extensionElements = extensionElements;
    }

    //TODO {manstis} The DMN model uses Object not String
    @Portable
    public static class ExtensionElements extends DMNModelInstrumentedBase {

        protected List<String> any;

        public List<String> getAny() {
            if (any == null) {
                any = new ArrayList<>();
            }
            return this.any;
        }
    }
}