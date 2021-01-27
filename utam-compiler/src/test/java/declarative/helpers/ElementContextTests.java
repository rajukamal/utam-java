package declarative.helpers;

import declarative.helpers.Validation.ErrorType;
import declarative.representation.MethodParameter;
import declarative.representation.TypeProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static declarative.grammar.TestUtilities.getCssSelector;
import static declarative.helpers.ElementContext.EMPTY_SCOPE_ELEMENT_NAME;
import static declarative.helpers.ParameterUtils.EMPTY_PARAMETERS;
import static declarative.helpers.TypeUtilities.CONTAINER_ELEMENT;
import static declarative.helpers.TypeUtilities.Element.actionable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.expectThrows;

/**
 * Provides tests for the ElementContext class
 *
 * @author james.evans
 */
public class ElementContextTests {

  private static final String ELEMENT_NAME = "fakeElementName";
  private static final String SELECTOR_VALUE = ".fakeSelector";

  private static void validateElement(ElementContext actual, ElementInfo expected) {
    assertThat(actual.getName(), is(equalTo(expected.name)));
    if (expected.typeName == null) {
      assertThat(actual.getType(), is(nullValue()));
    } else {
      assertThat(actual.getType().getFullName(), is(equalTo(expected.typeName)));
    }
    assertThat(actual.isList(), is(equalTo(expected.isList)));
    assertThat(actual.isRootScope(), is(equalTo(expected.isRoot)));

    List<String> actualParameterNames =
        actual.getParameters().stream()
            .map(MethodParameter::getValue)
            .collect(Collectors.toList());
    assertThat(actualParameterNames, is(equalTo(expected.scopeParameterNames)));

    List<String> actualParameterTypes =
        actual.getParameters().stream()
            .map((parameter) -> parameter.getType().getFullName())
            .collect(Collectors.toList());
    assertThat(actualParameterTypes, is(equalTo(expected.scopeParameterTypes)));
  }

  private static ElementContext.Basic getSingleElementContext(TypeProvider typeProvider) {
    return new ElementContext.Basic(ELEMENT_NAME, typeProvider, getCssSelector(SELECTOR_VALUE));
  }

  /** An Element object representing a single element should be able to be created */
  @Test
  public void testSingleElement() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo(ELEMENT_NAME, elementType.getFullName());
    ElementContext element = getSingleElementContext(elementType);
    validateElement(element, expected);
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing an element list with the same selector
   */
  @Test
  public void testSingleElementIsDuplicateWithListElementHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element = getSingleElementContext(elementType);

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE), true);
    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with the same selector
   */
  @Test
  public void testSingleElementIsDuplicateWithComponentHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element = getSingleElementContext(elementType);

    ElementContext otherElement =
        new ElementContext.Custom(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE));

    assertThat(
        element.validate(otherElement),
        is(equalTo(Validation.ErrorType.COMPONENT_AND_ELEMENT_DUPLICATE_SELECTOR)));
  }

  /** An Element object representing a container should be able to be created */
  @Test
  public void testContainerElement() {
    ElementInfo expected = new ElementInfo("name", CONTAINER_ELEMENT.getFullName());
    ElementContext element = new ElementContext.Container("name");
    validateElement(element, expected);
  }

  /**
   * The isDuplicate method should return the proper value when comparing a container Element object
   * with one representing a single element with the different selectors
   */
  @Test
  public void testContainerIsDuplicateWithElement() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element = new ElementContext.Container("name");
    ElementContext otherElement = getSingleElementContext(elementType);
    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  /**
   * An Element object representing a root element should be able to be created with a specified
   * selector type
   */
  @Test
  public void testRootElementWithSelectorType() {
    TypeProvider type = new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    TypeProvider elementType = actionable.getType();
    ElementInfo expected = new ElementInfo("root", elementType.getFullName());
    expected.isRoot = true;
    ElementContext element = new ElementContext.Root(type, elementType, getCssSelector(SELECTOR_VALUE));
    validateElement(element, expected);
  }

  /** An Element object representing a root element with a null type should be able to be created */
  @Test
  public void testRootElementWithNullType() {
    ElementInfo expected = new ElementInfo(EMPTY_SCOPE_ELEMENT_NAME, null);
    expected.isRoot = true;
    ElementContext element = ElementContext.ROOT_SCOPE;
    validateElement(element, expected);
    expectThrows(IllegalStateException.class, () -> element.validate(null));
  }

  /** Sn Element object representing a component should be able to be created */
  @Test
  public void testComponent() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo("fakeElementName", elementType.getFullName());

    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    validateElement(element, expected);
  }

  /**
   * Sn Element object representing a component should be able to be created with a specified
   * selector type
   */
  @Test
  public void testComponentWithSelectorType() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo("fakeElementName", elementType.getFullName());

    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    validateElement(element, expected);
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with a component with different selectors
   */
  @Test
  public void testComponentIsDuplicateWithComponentHavingDifferentSelectors() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    ElementContext otherElement =
        new ElementContext.Custom(
            "fakeOtherElementName", elementType, getCssSelector(".fakeDifferentSelector"));

    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with a component with the same selector
   */
  @Test
  public void testComponentIsDuplicateWithComponentHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    ElementContext otherElement =
        new ElementContext.Custom(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE));

    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with a component with the same selector but a different type
   */
  @Test
  public void testComponentIsDuplicateWithComponentHavingSameSelectorDifferentType() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    TypeProvider otherElementType =
        new TypeUtilities.FromString("FakeOtherElementType", "test.FakeOtherElementType");
    ElementContext otherElement =
        new ElementContext.Custom(
            "fakeOtherElementName", otherElementType, getCssSelector(SELECTOR_VALUE));

    assertThat(
        element.validate(otherElement),
        is(equalTo(Validation.ErrorType.COMPONENTS_WITH_SAME_SELECTOR_BUT_DIFFERENT_TYPES)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with a single element with the same selector
   */
  @Test
  public void testComponentIsDuplicateWithSingleElementHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE));

    assertThat(
        element.validate(otherElement),
        is(equalTo(Validation.ErrorType.COMPONENT_AND_ELEMENT_DUPLICATE_SELECTOR)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with a custom element with the same selector
   */
  @Test
  public void testComponentIsDuplicateWithCustomElementHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext otherElement = new FakeElement(elementType);
    assertThat(otherElement.isList(), is(equalTo(false)));
    assertThat(otherElement.isRootElement(), is(equalTo(false)));
    assertThat(otherElement.isRootScope(), is(equalTo(false)));
    assertThat(otherElement.isCustom(), is(equalTo(false)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing a component with an element list with the same selector
   */
  @Test
  public void testComponentIsDuplicateWithElementListHavingSameSelectors() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE));

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    assertThat(
        element.validate(otherElement),
        is(equalTo(Validation.ErrorType.COMPONENT_AND_ELEMENT_DUPLICATE_SELECTOR)));
  }

  /** An Element object representing a component list should be able to be created */
  @Test
  public void testComponentList() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo("fakeElementName", elementType.getFullName());
    expected.isList = true;

    ElementContext element =
        new ElementContext.Custom(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    validateElement(element, expected);
  }

  /**
   * An Element object representing a component list should be able to be created with a specified
   * selector type
   */
  @Test
  public void testComponentListWithSelectorType() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo(ELEMENT_NAME, elementType.getFullName());
    expected.isList = true;
    ElementContext element =
        new ElementContext.Custom(
            ELEMENT_NAME, elementType, getCssSelector(SELECTOR_VALUE), true);
    validateElement(element, expected);
  }

  /** An Element object representing an element list should be able to be created */
  @Test
  public void testElementList() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo(ELEMENT_NAME, elementType.getFullName());
    expected.isList = true;
    ElementContext element =
        new ElementContext.Basic(ELEMENT_NAME, elementType, getCssSelector(SELECTOR_VALUE), true);
    validateElement(element, expected);
  }

  /**
   * An Element object representing an element list should be able to be created with a specified
   * selector type
   */
  @Test
  public void testElementListWithSelectorType() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementInfo expected = new ElementInfo(ELEMENT_NAME, elementType.getFullName());
    expected.isList = true;

    ElementContext element =
        new ElementContext.Basic(ELEMENT_NAME, elementType, getCssSelector(SELECTOR_VALUE), true);

    validateElement(element, expected);
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing an element list with an element list with different selectors
   */
  @Test
  public void testElementListIsDuplicateWithElementListHavingDifferentSelectors() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Basic(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(".fakeDifferentSelector"));

    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  @Test
  public void testElementListWithElementListHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Basic(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    assertThat(element.validate(otherElement), is(equalTo(ErrorType.NONE)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing an element list with a single element with the same selector
   */
  @Test
  public void testElementListIsDuplicateWithSingleElementHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Basic(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    ElementContext otherElement =
        new ElementContext.Basic(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE));

    assertThat(element.validate(otherElement), is(equalTo(Validation.ErrorType.NONE)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing an element list with a component with the same selector
   */
  @Test
  public void testElementListIsDuplicateWithComponentHavingSameSelector() {
    TypeProvider elementType =
        new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element =
        new ElementContext.Basic(
            "fakeElementName", elementType, getCssSelector(SELECTOR_VALUE), true);

    ElementContext otherElement =
        new ElementContext.Custom(
            "fakeOtherElementName", elementType, getCssSelector(SELECTOR_VALUE));

    assertThat(
        element.validate(otherElement),
        is(equalTo(Validation.ErrorType.COMPONENT_AND_ELEMENT_DUPLICATE_SELECTOR)));
  }

  /**
   * The isDuplicate method should return the proper value when comparing an Element object
   * representing an element list with a root element with the same selector
   */
  @Test
  public void testElementListWithRootElementHavingSameSelector() {
    TypeProvider type = new TypeUtilities.FromString("FakeElementType", "test.FakeElementType");
    ElementContext element = new ElementContext.Basic("fakeElementName", type, getCssSelector(SELECTOR_VALUE), true);
    ElementContext otherElement = new ElementContext.Root(type, actionable.getType(), getCssSelector(SELECTOR_VALUE));
    assertThat(element.validate(otherElement), is(equalTo(ErrorType.DUPLICATE_WITH_ROOT_SELECTOR)));
  }

  @Test
  public void basicElementWithoutGetterThrows() {
    ElementContext elementContext = getSingleElementContext(actionable.getType());
    assertThrows(NullPointerException.class, () -> elementContext.getElementMethod());
  }

  private static class FakeElement extends ElementContext {

    FakeElement(TypeProvider elementType) {
      super(
          null,
          "fakeOtherElementName",
          elementType,
          getCssSelector(SELECTOR_VALUE),
          false,
          EMPTY_PARAMETERS);
    }

    @Override
    ErrorType validate(ElementContext element) {
      return Validation.ErrorType.NONE;
    }
  }

  private static final class ElementInfo {
    private final String name;
    private final String typeName;
    private final List<String> scopeParameterNames = new ArrayList<>();
    private final List<String> scopeParameterTypes = new ArrayList<>();
    private boolean isList;
    private boolean isRoot;

    ElementInfo(String name, String typeName) {
      this.name = name;
      this.typeName = typeName;
    }
  }
}