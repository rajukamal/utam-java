/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.grammar;

import static utam.compiler.helpers.PrimitiveType.BOOLEAN;
import static utam.compiler.helpers.PrimitiveType.NUMBER;
import static utam.compiler.helpers.PrimitiveType.STRING;
import static utam.compiler.helpers.PrimitiveType.isPrimitiveType;
import static utam.compiler.helpers.TypeUtilities.BASIC_ELEMENT;
import static utam.compiler.helpers.TypeUtilities.FUNCTION;
import static utam.compiler.helpers.TypeUtilities.REFERENCE;
import static utam.compiler.helpers.TypeUtilities.SELECTOR;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import utam.compiler.UtamCompilationError;
import utam.compiler.grammar.UtamArgumentDeserializer.ElementReference;
import utam.compiler.helpers.LocatorCodeGeneration;
import utam.compiler.helpers.MethodContext;
import utam.compiler.helpers.ParameterUtils.Literal;
import utam.compiler.helpers.ParameterUtils.Regular;
import utam.compiler.helpers.PrimitiveType;
import utam.compiler.helpers.TranslationContext;
import utam.compiler.representation.ComposeMethodStatement;
import utam.core.declarative.representation.MethodParameter;
import utam.core.declarative.representation.TypeProvider;

/**
 * UTAM argument inside selector, method, filter, matcher etc.
 *
 * @author elizaveta.ivanova
 * @since 228
 */
@JsonDeserialize(using = UtamArgumentDeserializer.class)
class UtamArgument {

  static final String FUNCTION_TYPE_PROPERTY = "function";
  static final String SELECTOR_TYPE_PROPERTY = "locator";
  static final String ELEMENT_TYPE_PROPERTY = "element";
  static final String ERR_ARGS_WRONG_TYPE = "%s: expected type is '%s', actual was '%s'";
  static final String ERR_ARGS_DUPLICATE_NAMES = "%s: duplicate arguments names '%s'";
  static final String ERR_ARGS_WRONG_COUNT = "%s: expected %s parameters, provided %s";
  static final String ERR_WHILE_PARSING = "not reachable because of deserializer";
  final Object value;
  private final String name;
  private final String type;
  private final UtamMethodAction[] conditions;

  @JsonCreator
  UtamArgument(
      @JsonProperty(value = "value") Object value,
      @JsonProperty(value = "name") String name,
      @JsonProperty(value = "type") String type,
      @JsonProperty(value = "predicate") UtamMethodAction[] conditions) {
    this.name = name;
    this.type = type;
    this.value = value;
    this.conditions = conditions;
  }

  UtamArgument(UtamMethodAction[] conditions) {
    this(null, null, FUNCTION_TYPE_PROPERTY, conditions);
  }

  UtamArgument(String name, String type) {
    this(null, name, type, null);
  }

  UtamArgument(Object value) {
    this(value, null, null, null);
  }

  MethodParameter getArgByValue(TranslationContext translationContext) {
    if (value instanceof UtamSelector) {
      UtamSelector selector = (UtamSelector) value;
      LocatorCodeGeneration locatorCode = selector.getCodeGenerationHelper(translationContext);
      return locatorCode.getLiteralParameter();
    }
    if (value instanceof ElementReference) {
      ElementReference elementReference = (ElementReference) value;
      return elementReference.getElementGetterAsLiteralArg(translationContext);
    }
    if (value instanceof Boolean) {
      return new Literal(value.toString(), BOOLEAN);
    }
    if (value instanceof Number) {
      return new Literal(value.toString(), NUMBER);
    }
    if (value instanceof String) {
      return new Literal(value.toString(), STRING);
    }
    throw new UtamCompilationError(ERR_WHILE_PARSING);
  }

  MethodParameter getArgByNameType() {
    if (isPrimitiveType(type)) {
      return new Regular(name, PrimitiveType.fromString(type));
    }
    if (SELECTOR_TYPE_PROPERTY.equals(type)) {
      return new Regular(name, SELECTOR);
    }
    if (REFERENCE.getSimpleName().equals(type)) {
      return new Regular(name, REFERENCE);
    }
    if (ELEMENT_TYPE_PROPERTY.equals(type)) {
      return new Regular(name, BASIC_ELEMENT);
    }
    throw new UtamCompilationError(ERR_WHILE_PARSING);
  }

  List<ComposeMethodStatement> getPredicate(TranslationContext context,
      MethodContext methodContext) {
    List<ComposeMethodStatement> predicateStatements = new ArrayList<>();
    for (int i = 0; i < conditions.length; i++) {
      boolean isLastPredicateStatement = i == conditions.length - 1;
      predicateStatements
          .add(conditions[i].getComposeAction(context, methodContext, isLastPredicateStatement));
    }
    return predicateStatements;
  }

  /**
   * helper class: holds information for a one time args processing. Includes: translation context,
   * validation message and list of expected args types (if known)
   *
   * @since 236
   */
  static class ArgsProcessor {

    final String validationString;
    private final List<MethodParameter> parameters = new ArrayList<>();
    private final TranslationContext translationContext;
    private final List<TypeProvider> expectedParametersTypes;

    ArgsProcessor(TranslationContext translationContext, String validationString,
        List<TypeProvider> expectedParametersTypes) {
      this.validationString = validationString;
      this.translationContext = translationContext;
      this.expectedParametersTypes = expectedParametersTypes;
    }

    ArgsProcessor(TranslationContext context, MethodContext methodContext,
        List<TypeProvider> expectedParametersTypes) {
      this(context, String.format("method '%s'", methodContext.getName()), expectedParametersTypes);
    }

    ArgsProcessor(TranslationContext context, MethodContext methodContext) {
      this(context, String.format("method '%s'", methodContext.getName()), null);
    }

    List<MethodParameter> getParameters(UtamArgument[] args) {
      int actualCnt = args == null ? 0 : args.length;
      if (expectedParametersTypes != null && expectedParametersTypes.size() != actualCnt) {
        throw new UtamCompilationError(String.format(ERR_ARGS_WRONG_COUNT,
            validationString,
            expectedParametersTypes.size(),
            actualCnt));
      }
      if (args == null) {
        return parameters;
      }
      for (int i = 0; i < args.length; i++) {
        UtamArgument arg = args[i];
        TypeProvider expectedType =
            expectedParametersTypes == null ? null : expectedParametersTypes.get(i);
        getParameter(arg, expectedType);
      }
      return parameters;
    }

    void checkExpectedType(TypeProvider expectedType, TypeProvider actualType) {
      if (expectedType != null && !actualType.isSameType(expectedType)) {
        throw new UtamCompilationError(String.format(ERR_ARGS_WRONG_TYPE,
            validationString,
            expectedType.getSimpleName(),
            actualType.getSimpleName()));
      }
    }

    MethodParameter getParameter(UtamArgument utamArgument, TypeProvider expectedType) {
      // predicate is not used as a parameter
      if (FUNCTION_TYPE_PROPERTY.equals(utamArgument.type)) {
        checkExpectedType(expectedType, FUNCTION);
        return null;
      }

      MethodParameter parameter = utamArgument.value == null ?
          utamArgument.getArgByNameType()
          : utamArgument.getArgByValue(translationContext);
      checkExpectedType(expectedType, parameter.getType());

      if (!parameter.isLiteral()) {
        parameters.forEach(arg -> {
          // compare non literal names to avoid collisions
          if (!arg.isLiteral() && parameter.getValue().equals(arg.getValue())) {
            throw new UtamCompilationError(
                String.format(ERR_ARGS_DUPLICATE_NAMES, validationString,
                    parameter.getValue()));
          }
        });
      }
      parameters.add(parameter);
      return parameter;
    }
  }
}
