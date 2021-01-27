package utam.core.framework.consumer;

import utam.core.framework.base.RootPageObject;
import utam.core.selenium.element.Actionable;

/**
 * used in tests
 *
 * @author elizaveta.ivanova
 * @since 230
 */
public interface PageObjectMock extends RootPageObject {
  Actionable getRoot();
}