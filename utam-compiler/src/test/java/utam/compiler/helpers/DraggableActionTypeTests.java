/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static utam.compiler.grammar.TestUtilities.getCssSelector;
import static utam.compiler.helpers.TypeUtilities.BASIC_ELEMENT;
import static utam.compiler.helpers.TypeUtilities.BasicElementInterface.draggable;
import static utam.compiler.helpers.TypeUtilities.VOID;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import org.testng.annotations.Test;
import utam.core.declarative.representation.TypeProvider;
import utam.core.element.Draggable;

/**
 * tests for draggable action types
 *
 * @author elizaveta.ivanova
 * @since 236
 */
public class DraggableActionTypeTests {

  @Test
  public void checkSupportedActions() {
    for (Method method : Draggable.class.getDeclaredMethods()) {
      checkTranslatorValue(method, DraggableActionType::valueOf);
    }
  }

  private void checkTranslatorValue(Method method, Consumer<String> consumer) {
    consumer.accept(method.getName());
  }

  @Test
  public void testDragAndDrop() {
    DraggableActionType action = DraggableActionType.dragAndDrop;
    assertThat(action.getReturnType().isSameType(VOID), is(true));
    assertThat(action.getApplyString(), is(equalTo(action.name())));
  }

  @Test
  public void testDragAndDropParametersOptions() {
    DraggableActionType action = DraggableActionType.dragAndDrop;
    List<List<TypeProvider>> parametersOptions = action.getParametersTypesOptions();
    assertThat(parametersOptions, hasSize(4));
    //1
    List<TypeProvider> parameters = parametersOptions.get(0);
    assertThat(parameters, hasSize(1));
    assertThat(parameters.get(0).isSameType(BASIC_ELEMENT), is(true));
    //2
    parameters = parametersOptions.get(1);
    assertThat(parameters, hasSize(2));
    assertThat(parameters.get(0).isSameType(BASIC_ELEMENT), is(true));
    assertThat(parameters.get(1).isSameType(PrimitiveType.NUMBER), is(true));
    //3
    parameters = parametersOptions.get(2);
    assertThat(parameters, hasSize(2));
    assertThat(parameters.get(0).isSameType(PrimitiveType.NUMBER), is(true));
    assertThat(parameters.get(1).isSameType(PrimitiveType.NUMBER), is(true));
    //4
    parameters = parametersOptions.get(3);
    assertThat(parameters, hasSize(3));
    assertThat(parameters.get(0).isSameType(PrimitiveType.NUMBER), is(true));
    assertThat(parameters.get(1).isSameType(PrimitiveType.NUMBER), is(true));
    assertThat(parameters.get(2).isSameType(PrimitiveType.NUMBER), is(true));
  }

  @Test
  public void testGetActionFromStringForActionable() {
    ActionType action = DraggableActionType.dragAndDrop;
    ElementContext elementContext = new ElementContext.Basic("element", draggable,
        getCssSelector("selector"));
    assertThat(
        BasicElementActionType.getActionType(
            action.getApplyString(), elementContext.getType(), elementContext.getName()),
        is(equalTo(action)));
  }
}
