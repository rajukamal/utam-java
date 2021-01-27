package declarative.representation;

import declarative.helpers.ElementContext;
import declarative.helpers.MatcherType;
import declarative.helpers.TypeUtilities;
import selenium.element.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static declarative.helpers.ParameterUtils.getParametersValuesString;
import static declarative.representation.ComposeMethod.getElementGetterString;
import static declarative.representation.ElementMethod.getPredicateCode;
import static declarative.translator.TranslationUtilities.getElementGetterMethodName;

/**
 * generate code of getter method for custom element
 *
 * @author elizaveta.ivanova
 * @since 224
 */
public abstract class CustomElementMethod implements PageObjectMethod {

  static final String BASE_PAGE_OBJECT_METHOD = "inScope";
  static final String BUILDER_METHOD = "build";
  static final String LIST_BUILDER_METHOD = "buildList";
  private static final String TMP_VARIABLE = "instance";

  // BasePageObject.builder(BaseElement scopeElement, Locator selector, boolean isNullable)
  static String getBuilderPrefix(ElementContext scopeElement, Root root, boolean isNullable) {
    return String.format(
        "%s(%s, %s, %s)",
        BASE_PAGE_OBJECT_METHOD,
        getElementGetterString(scopeElement),
        root.getCodeString(),
        isNullable);
  }

  // BasePageObject.builder(BaseElement scopeElement, Locator selector, boolean isNullable)
  static String getExternalBuilderPrefix(ElementContext scopeElement, Root root) {
    return String.format(
        "%s(%s, %s)",
        BASE_PAGE_OBJECT_METHOD, getElementGetterString(scopeElement), root.getCodeString());
  }

  // <T extends PageObject> T build(Class<T> type);
  // or <T extends PageObject> List<T> buildList(Class<T> type)
  static String getBuilderSuffix(TypeProvider returnType, boolean isList) {
    if (isList) {
      return String.format("%s(%s.class)", LIST_BUILDER_METHOD, returnType.getSimpleName());
    } else {
      return String.format("%s(%s.class)", BUILDER_METHOD, returnType.getSimpleName());
    }
  }

  // <T extends PageObject> T build(Class<T> type, Predicate<T> filter);
  // or <T extends PageObject> List<T> buildList(Class<T> type, Predicate<T> filter)
  static String getFilteredBuilderSuffix(
      TypeProvider returnType, String predicateCode, boolean isList) {
    if (isList) {
      return String.format(
          "%s(%s.class, %s)", LIST_BUILDER_METHOD, returnType.getSimpleName(), predicateCode);
    } else {
      return String.format(
          "%s(%s.class, %s)", BUILDER_METHOD, returnType.getSimpleName(), predicateCode);
    }
  }

  public static final class Single implements PageObjectMethod {

    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>(Root.SELECTOR_IMPORTS);
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final List<MethodParameter> methodParameters = new ArrayList<>();
    private final TypeProvider returnType;

    public Single(
        boolean isPublic,
        String componentName,
        Root root,
        ElementContext scopeElement,
        boolean isExternalImplementation,
        TypeProvider returnType,
        boolean isNullable) {
      String builderPrefix =
          isExternalImplementation
              ? getExternalBuilderPrefix(scopeElement, root)
              : getBuilderPrefix(scopeElement, root, isNullable);
      String builderSuffix = getBuilderSuffix(returnType, false);
      String firstStatement =
          String.format(
              "%s %s = %s.%s",
              returnType.getSimpleName(), TMP_VARIABLE, builderPrefix, builderSuffix);
      codeLines.add(firstStatement);
      if (!isNullable && !isExternalImplementation) {
        codeLines.add(String.format("%s.load()", TMP_VARIABLE));
      }
      codeLines.add(TMP_VARIABLE);
      interfaceImports.add(returnType);
      classImports.add(returnType);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.methodParameters.addAll(scopeElement.getParameters());
      this.methodParameters.addAll(root.selectorParameters);
      this.returnType = returnType;
    }

    @Override
    public List<String> getCodeLines() {
      return codeLines;
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return classImports;
    }

    @Override
    public MethodDeclaration getDeclaration() {
      return new MethodDeclarationImpl(methodName, methodParameters, returnType, interfaceImports);
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }

  public static class Filtered implements PageObjectMethod {

    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>(Root.SELECTOR_IMPORTS);
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final List<MethodParameter> methodParameters = new ArrayList<>();
    private final TypeProvider returnType;
    private final TypeProvider returnListType;

    public Filtered(
        boolean isPublic,
        String componentName,
        Root root,
        ElementContext scopeElement,
        TypeProvider returnType,
        boolean isNullable,
        String applyMethod,
        List<MethodParameter> applyParameters,
        MatcherType matcherType,
        List<MethodParameter> matcherParameters,
        boolean isFindFirst) {
      this.returnListType = isFindFirst ? null : new TypeUtilities.ListOf(returnType);
      this.interfaceImports.add(returnType);
      this.classImports.add(returnType);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.methodParameters.addAll(scopeElement.getParameters());
      this.methodParameters.addAll(root.selectorParameters);
      this.methodParameters.addAll(applyParameters);
      this.methodParameters.addAll(matcherParameters);
      this.returnType = returnType;
      if (returnListType != null) {
        interfaceImports.add(returnListType);
        classImports.add(returnListType);
      }
      // code
      String builderPrefix = getBuilderPrefix(scopeElement, root, isNullable);
      String predicate =
          getPredicateCode(applyMethod, applyParameters, matcherType, matcherParameters);
      String builderSuffix = getFilteredBuilderSuffix(returnType, predicate, !isFindFirst);
      codeLines.add(String.format("%s.%s", builderPrefix, builderSuffix));
    }

    @Override
    public List<String> getCodeLines() {
      return codeLines;
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return classImports;
    }

    @Override
    public MethodDeclaration getDeclaration() {
      TypeProvider returns =
          returnListType != null ? new TypeUtilities.ListOf(returnType) : returnType;
      return new MethodDeclarationImpl(methodName, methodParameters, returns, interfaceImports);
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }

  public static final class Multiple implements PageObjectMethod {

    private final TypeProvider listType;
    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>(Root.SELECTOR_IMPORTS);
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final List<MethodParameter> methodParameters = new ArrayList<>();

    public Multiple(
        boolean isPublic,
        String componentName,
        Root root,
        ElementContext scopeElement,
        TypeProvider returnType,
        boolean isNullable) {
      this.listType = new TypeUtilities.ListOf(returnType);
      String builderPrefix = getBuilderPrefix(scopeElement, root, isNullable);
      String builderSuffix = getBuilderSuffix(returnType, true);
      codeLines.add(String.format("%s.%s", builderPrefix, builderSuffix));
      interfaceImports.add(returnType);
      interfaceImports.add(listType);
      classImports.add(returnType);
      classImports.add(listType);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.methodParameters.addAll(scopeElement.getParameters());
      this.methodParameters.addAll(root.selectorParameters);
    }

    @Override
    public MethodDeclarationImpl getDeclaration() {
      return new MethodDeclarationImpl(methodName, methodParameters, listType, interfaceImports);
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return this.classImports;
    }

    @Override
    public List<String> getCodeLines() {
      return this.codeLines;
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }

  public static final class Root {

    static final List<TypeProvider> SELECTOR_IMPORTS =
        Stream.of(new TypeUtilities.FromClass(Selector.class)).collect(Collectors.toList());
    final List<MethodParameter> selectorParameters = new ArrayList<>();
    private final String selectorCodeString;

    public Root(
        Selector selector, boolean isExpandScope, List<MethodParameter> selectorParameters) {
      String selectorString;
      if (selectorParameters.isEmpty()) {
        selectorString = String.format("\"%s\"", escapeDoubleQuotes(selector.getValue()));
      } else {
        selectorString =
            String.format(
                "String.format(\"%s\", %s)",
                escapeDoubleQuotes(selector.getValue()),
                getParametersValuesString(selectorParameters));
      }
      this.selectorCodeString =
          String.format(
              "by(%s, Selector.Type.%s, %s)", selectorString, selector.getType(), isExpandScope);
      this.selectorParameters.addAll(selectorParameters);
    }

    private static String escapeDoubleQuotes(String selectorString) {
      return selectorString.replaceAll("\"", Matcher.quoteReplacement("\\\""));
    }

    String getCodeString() {
      return selectorCodeString;
    }
  }
}