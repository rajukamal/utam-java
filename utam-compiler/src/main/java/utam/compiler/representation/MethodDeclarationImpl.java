package utam.compiler.representation;

import utam.core.declarative.representation.MethodDeclaration;
import utam.core.declarative.representation.MethodParameter;
import utam.core.declarative.representation.TypeProvider;

import java.util.List;
import java.util.stream.Collectors;

import static utam.compiler.translator.TranslationUtilities.EMPTY_COMMENTS;

class MethodDeclarationImpl implements MethodDeclaration {

  private final String methodName;

  private final List<TypeProvider> imports;

  private final List<MethodParameter> parameters;

  private final TypeProvider returnType;

  private final String comments;

  MethodDeclarationImpl(
      String methodName,
      List<MethodParameter> parameters,
      TypeProvider returnType,
      List<TypeProvider> imports,
      String comments) {
    this.methodName = methodName;
    this.imports = imports;
    this.returnType = returnType;
    this.parameters = parameters;
    this.comments = comments;
  }

  MethodDeclarationImpl(
          String methodName,
          List<MethodParameter> parameters,
          TypeProvider returnType,
          List<TypeProvider> imports) {
    this(methodName, parameters, returnType, imports, EMPTY_COMMENTS);
  }

  @Override
  public final List<MethodParameter> getParameters() {
    return parameters;
  }

  public final List<TypeProvider> getImports() {
    return imports;
  }

  @Override
  public final String getName() {
    return methodName;
  }

  @Override
  public final TypeProvider getReturnType() {
    return returnType;
  }

  @Override
  public final String getCodeLine() {
    return getCode(getName(), getReturnType(), getParameters());
  }

  private static String getCode(String methodName, TypeProvider returns, List<MethodParameter> parameters) {
    String params =
            parameters.stream()
                    .map(MethodParameter::getDeclaration)
                    .filter(str -> !str.isEmpty()) // hardcoded values passed as empty string
                    .collect(Collectors.joining(", "));
    return String.format("%s %s(%s)", returns.getSimpleName(), methodName, params);
  }

  @Override
  public String getComments() {
    return comments;
  }
}