package utam.compiler.helpers;

import utam.core.declarative.representation.TypeProvider;
import org.testng.annotations.Test;
import utam.core.selenium.element.Clickable;
import utam.core.selenium.expectations.DriverExpectationsUtil;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utam.core.framework.UtamLogger.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Provides tests for the TranslatableAction enum
 *
 * @author james.evans
 */
public class ClickableActionTypeTests {

  private static final String VOID_TYPE_NAME = "void";

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Method getMethod(Class clazz, String methodName, Class[] parameters) {
    try {
      return clazz.getDeclaredMethod(methodName, parameters);
    } catch (Exception e) {
      throw new AssertionError(
          String.format("method '%s' not found in class %s", methodName, clazz.getName()), e);
    }
  }

  @Test
  public void checkSupportedActions() {
    for (Method method : Clickable.class.getDeclaredMethods()) {
      checkTranslatorValue(method, ClickableActionType::valueOf);
    }
  }

  private void checkTranslatorValue(Method method, Consumer<String> consumer) {
    consumer.accept(method.getName());
  }

  /** The click member should return the proper value */
  @Test
  public void testClick() {
    validateAction(ClickableActionType.click, new ArrayList<>());
  }
  
  /** The javascriptClick member should return the proper value */
  @Test
  public void testJavascriptClick() {
    validateAction(ClickableActionType.javascriptClick, new ArrayList<>());
  }

  private void validateAction(ActionType action, List<String> parameterTypes) {
    Set<String> parameterTypeStrings =
        action.getParametersTypes().stream()
            .filter((type) -> !type.getSimpleName().isEmpty())
            .map(TypeProvider::getSimpleName)
            .collect(Collectors.toSet());

    assertThat(parameterTypeStrings, containsInAnyOrder(parameterTypes.toArray()));
    assertThat(parameterTypeStrings, hasSize(parameterTypes.size()));
    assertThat(action.getReturnType().getSimpleName(), is(equalTo(ClickableActionTypeTests.VOID_TYPE_NAME)));
    assertThat(action.isListAction(), is(equalTo(false)));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void driverActions() {
    for (DriverExpectationsUtil.Type action : DriverExpectationsUtil.Type.values()) {
      Method method =
          getMethod(DriverExpectationsUtil.class, action.name(), action.getParameterTypes());
      // method returns expectations, so we need generic type parameter
      Class expectationReturns =
          (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
      // check that code is set
      // N.B., currently, all actions return a valid type; if an action is added that
      // will return null, something similar to the following will be required:
      // Class expected = action.getReturnType() == null ? WebElement.class : action.getReturnType();
      Class expected = action.getReturnType();
      assertThat(expected, is(equalTo(expectationReturns)));
    }
  }

  @Test
  @SuppressWarnings({"rawtypes"})
  public void testStandardActionsMethods() {
    Stream.of(ClickableActionType.values())
        .forEach(
            action -> {
              info(String.format("test element action '%s'", action));
              Method method =
                  getMethod(action.getElementClass(), action.name(), action.getParameterClasses());
              assertThat(
                  String.format(
                      "action '%s' returns '%s', method returns '%s'",
                      action.name(),
                      action.getReturnType().name(),
                      method.getReturnType().getName()),
                  action.getReturnType().equals(method.getReturnType()),
                  is(true));
              Class[] params = action.getParameterClasses();
              assertThat(
                  String.format("number of actual parameters is %d", params.length),
                  params.length,
                  is(equalTo(method.getParameterCount())));
              // For now, all clickable element actions have zero parameters. Should
              // a method exist that requires parameters the block of code below will
              // be needed to validate their types.
              // for (int i = 0; i < params.length; i++) {
              //   assertThat(params[i], is(equalTo(method.getParameterTypes()[i])));
              // }
            });
  }
}