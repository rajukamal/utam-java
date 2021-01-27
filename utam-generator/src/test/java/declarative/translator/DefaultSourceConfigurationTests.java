package declarative.translator;

import framework.consumer.UtamError;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static declarative.translator.DefaultSourceConfiguration.*;
import static declarative.translator.DefaultTranslatorRunner.DUPLICATE_PAGE_OBJECT_NAME;
import static declarative.translator.TranslatorMockUtilities.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.expectThrows;

/**
 * @author elizaveta.ivanova
 * @since 228
 */
public class DefaultSourceConfigurationTests {

  private static final String FAKE_IO_EXCEPTION_MESSAGE = "throwing fake IO exception";
  private static final String INTERFACE_ONLY_SOURCE =
      "{"
          + "  \"interface\" : true,\n"
          + "  \"methods\": [\n"
          + "    {\n"
          + "      \"name\" : \"testMethod\",\n"
          + "      \"return\" : \"string\"\n"
          + "    }"
          + "  ]\n"
          + "}";
  private static final String IMPL_ONLY_SOURCE =
      "{"
          + "  \"implements\": \"utam-test/pageObjects/test/testAbstractObject\",\n"
          + "  \"methods\": [\n"
          + "    {\n"
          + "      \"name\" : \"testMethod\",\n"
          + "      \"compose\": [{\"element\":\"root\", \"apply\" : \"getText\"}]"
          + "    }"
          + "  ]\n"
          + "}";

  public static TranslatorSourceConfig getSourceConfig(String jsonString) throws IOException {
    TranslatorSourceConfig mockConfig = mock(TranslatorSourceConfig.class);
    when(mockConfig.getPageObjects()).thenReturn(Collections.singletonList("test"));
    when(mockConfig.getDeclarationReader(any())).thenReturn(new JsonStringReaderMock(jsonString));
    return mockConfig;
  }

  @Test
  public void testRunWithDuplicatePageObjectsThrows() {
    TranslatorConfig configuration = new AbstractTranslatorConfigurationTests.Mock();
    configuration.setSourceConfig(new DuplicatePageObjects());
    DefaultTranslatorRunner translator = new DefaultTranslatorRunnerTests.Mock(configuration);
    UtamError e = expectThrows(UtamError.class, translator::run);
    assertThat(
        e.getMessage(), containsString(String.format(DUPLICATE_PAGE_OBJECT_NAME, PAGE_OBJECT_URI)));
  }

  @Test
  public void testMissingPageObjectThrows() {
    final String PAGE_OBJECT = "error";
    UtamError e =
        expectThrows(
            UtamError.class, () -> new DefaultSourceConfiguration() {}.getSourcePath(PAGE_OBJECT));
    assertThat(e.getMessage(), containsString(String.format(ERR_MISSING_SOURCE_PATH, PAGE_OBJECT)));
  }

  @Test
  public void testMissingPackageMappingThrows() {
    final DefaultSourceConfiguration sourceConfiguration = new DefaultSourceConfiguration() {};
    UtamError e = expectThrows(UtamError.class, () -> sourceConfiguration.getPackageMapping(null));
    assertThat(e.getMessage(), containsString(String.format(ERR_MISSING_PACKAGE_MAPPING, "null")));
    e = expectThrows(UtamError.class, () -> sourceConfiguration.getPackageMapping("error"));
    assertThat(e.getMessage(), containsString(String.format(ERR_MISSING_PACKAGE_MAPPING, "error")));
  }

  @Test
  public void testRecursiveScanThrows() {
    final String PATH = "path";
    UtamError e = expectThrows(UtamError.class, () -> recursiveScan(null, new File(PATH)));
    assertThat(e.getMessage(), containsString(String.format(ERR_SOURCE_FILES_NULL, PATH)));
  }

  @Test
  public void testRecursiveScan() {
    final DefaultSourceConfiguration sourceConfiguration =
        new DefaultSourceConfiguration() {
          @Override
          public String getPackageMapping(String folder) {
            return folder;
          }
        };
    sourceConfiguration.preProcess(null);
    sourceConfiguration.preProcess(new File[0]);
    sourceConfiguration.preProcess(new File[] {new File("folder"), new File("test")});
    sourceConfiguration.preProcess(new File[] {new File("folder"), new File("file")});
    sourceConfiguration.preProcess(new File[] {new File("folder"), new File("file.json")});
  }

  @Test
  public void testRecursiveScanDuplicateThrows() {
    final DefaultSourceConfiguration sourceConfiguration = new DefaultSourceConfiguration() {
      @Override
      public String getPackageMapping(String folder) {
        return folder;
      }
    };
    sourceConfiguration.preProcess(new File[] { new File("folder"), new File("file.utam.json")});
    UtamError e = expectThrows(UtamError.class,
            () -> sourceConfiguration.preProcess(new File[] { new File("folder"), new File("file.utam.json")}));
    assertThat(e.getMessage(), is(equalTo(String.format(ERR_DUPLICATE_PAGE_OBJECT, "folder/pageObjects/folder/file"))));
  }

  static class Mock implements TranslatorSourceConfig {

    private final Map<String, String> pageObjectsJSONString = new HashMap<>();

    DefaultTranslatorRunner getRunner() {
      return new DefaultTranslatorRunnerTests.Mock(getConfig());
    }

    AbstractTranslatorConfiguration getConfig() {
      DefaultTargetConfigurationTests.Mock configuration =
          new DefaultTargetConfigurationTests.Mock();
      return new AbstractTranslatorConfigurationTests.Mock(configuration, this);
    }

    @Override
    public String getPackageMapping(String folder) {
      throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Reader getDeclarationReader(String pageObjectURI) {
      return new JsonStringReaderMock(pageObjectsJSONString.get(pageObjectURI));
    }

    @Override
    public Collection<String> getPageObjects() {
      return pageObjectsJSONString.keySet();
    }

    final void setJSONSource(String pageObject, String path) {
      pageObjectsJSONString.put(pageObject, path);
    }

    final void setSources() {
      setJSONSource(PAGE_OBJECT_URI, PAGE_OBJECT_SOURCE);
      setJSONSource(INTERFACE_ONLY_URI, INTERFACE_ONLY_SOURCE);
      setJSONSource(IMPL_ONLY_URI, IMPL_ONLY_SOURCE);
    }
  }

  private static class DuplicatePageObjects extends DefaultSourceConfigurationTests.Mock {

    DuplicatePageObjects() {
      setSources();
    }

    @Override
    public Collection<String> getPageObjects() {
      List<String> pageObjectList = new ArrayList<>(super.getPageObjects());
      pageObjectList.add(PAGE_OBJECT_URI); // add duplicate
      return pageObjectList;
    }
  }

  static class JsonStringReaderMock extends Reader {

    final int currentPosition = 0;
    private final String jsonString;
    boolean isAtEndOfStream = false;

    JsonStringReaderMock(String jsonString) {
      this.jsonString = jsonString;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      if (jsonString == null) {
        throw new IOException(FAKE_IO_EXCEPTION_MESSAGE);
      }

      if (isAtEndOfStream) {
        return -1;
      }

      int copied = 0;
      int availableLength = cbuf.length < off + len ? cbuf.length - off : len - off;
      for (int i = currentPosition; i < availableLength; i++) {
        if (i >= jsonString.length()) {
          isAtEndOfStream = true;
          break;
        }
        cbuf[i + off] = jsonString.charAt(i);
        copied++;
      }
      return copied;
    }

    @Override
    public void close() {}
  }
}