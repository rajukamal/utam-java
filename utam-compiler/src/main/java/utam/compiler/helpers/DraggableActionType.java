/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.helpers;

import static utam.compiler.helpers.TypeUtilities.VOID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utam.core.declarative.representation.TypeProvider;
import utam.core.element.Draggable;

/**
 * Draggable action types enum links element actions with translator code
 *
 * @author elizaveta.ivanova
 * @since 236
 */
public enum DraggableActionType implements ActionType {
  /**
   * drag and drop current element to the location provided as an element parameter
   */
  dragAndDrop;

  private static final List<List<TypeProvider>> POSSIBLE_PARAMETERS = new ArrayList<>();
  private static final List<TypeProvider> ELEMENT_PARAMETERS = Collections
      .singletonList(TypeUtilities.BASIC_ELEMENT);
  private static final List<TypeProvider> ELEMENT_WITH_DURATION_PARAMETERS = new ArrayList<>(
      ELEMENT_PARAMETERS);
  private static final List<TypeProvider> OFFSET_PARAMETERS = new ArrayList<>();
  private static final List<TypeProvider> OFFSET_WITH_DURATION_PARAMETERS = new ArrayList<>();

  static {
    ELEMENT_WITH_DURATION_PARAMETERS.add(PrimitiveType.NUMBER);
    OFFSET_PARAMETERS.add(PrimitiveType.NUMBER);
    OFFSET_PARAMETERS.add(PrimitiveType.NUMBER);
    OFFSET_WITH_DURATION_PARAMETERS.addAll(OFFSET_PARAMETERS);
    OFFSET_WITH_DURATION_PARAMETERS.add(PrimitiveType.NUMBER);
    POSSIBLE_PARAMETERS.add(ELEMENT_PARAMETERS);
    POSSIBLE_PARAMETERS.add(ELEMENT_WITH_DURATION_PARAMETERS);
    POSSIBLE_PARAMETERS.add(OFFSET_PARAMETERS);
    POSSIBLE_PARAMETERS.add(OFFSET_WITH_DURATION_PARAMETERS);
  }

  @Override
  public TypeProvider getReturnType() {
    return VOID;
  }

  @Override
  public List<TypeProvider> getParametersTypes() {
    throw new IllegalStateException(
        "Draggable method supports more than one parameters combination");
  }

  @Override
  public List<List<TypeProvider>> getParametersTypesOptions() {
    return POSSIBLE_PARAMETERS;
  }

  @Override
  public String getApplyString() {
    return this.name();
  }
}
