/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.core.framework.element;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;
import static utam.core.element.FindContext.Type.NULLABLE;
import static utam.core.element.FindContext.Type.NULLABLE_IN_SHADOW;

import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import utam.core.MockUtilities;
import utam.core.element.Element;
import utam.core.element.Element.GestureDirection;
import utam.core.element.Locator;
import utam.core.selenium.element.LocatorBy;

/**
 * @author elizaveta.ivanova
 * @since 234
 */
public class BasePageElementTests {

  @Test
  public void testWaitForAbsence() {
    MockUtilities mock = new MockUtilities();
    when(mock.getWebElementMock().isDisplayed()).thenThrow(StaleElementReferenceException.class)
        .thenReturn(true);
    mock.getUtamElement().waitForAbsence();
    assertThrows(TimeoutException.class, () -> mock.getUtamElement().waitForAbsence());
  }

  @Test
  public void testWaitForVisible() {
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().isDisplayed()).thenReturn(false).thenReturn(true);
    mock.getUtamElement().waitForVisible();
    verify(mock.getWebElementMock(), times(2)).isDisplayed();
  }

  @Test
  public void testWaitForInvisible() {
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().isDisplayed()).thenReturn(true).thenReturn(false);
    mock.getUtamElement().waitForInvisible();
    verify(mock.getWebElementMock(), times(2)).isDisplayed();
  }

  @Test
  public void testIsVisible() {
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().isDisplayed()).thenReturn(false);
    assertThat(mock.getUtamElement().isVisible(), is(false));
    verify(mock.getWebElementMock(), times(1)).isDisplayed();
  }

  @Test
  public void testIsEnabled() {
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().isEnabled()).thenReturn(false);
    assertThat(mock.getUtamElement().isEnabled(), is(false));
    verify(mock.getWebElementMock(), times(1)).isEnabled();
  }

  @Test
  public void testGetAttribute() {
    String attr = "nameAndValue";
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().getAttribute(attr)).thenReturn(attr);
    assertThat(mock.getUtamElement().getAttribute(attr), is(equalTo(attr)));
    verify(mock.getWebElementMock(), times(1)).getAttribute(attr);
  }

  @Test
  public void testGetClassAttribute() {
    String attr = "class";
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().getAttribute(attr)).thenReturn(attr);
    assertThat(mock.getUtamElement().getClassAttribute(), is(equalTo(attr)));
    verify(mock.getWebElementMock(), times(1)).getAttribute(attr);
  }

  @Test
  public void testGetText() {
    MockUtilities mock = new MockUtilities();
    when(mock.getWebElementMock().getText()).thenReturn("text");
    assertThat(mock.getUtamElement().getText(), is(equalTo("text")));
    verify(mock.getWebElementMock(), times(1)).getText();
  }

  @Test
  public void testSetText() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().setText("str");
    verify(mock.getWebElementMock(), times(1)).sendKeys("str");
  }

  @Test
  public void testGetTitle() {
    String attr = "title";
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().getAttribute(attr)).thenReturn(attr);
    assertThat(mock.getUtamElement().getTitle(), is(equalTo(attr)));
  }

  @Test
  public void testGetValue() {
    String attr = "value";
    MockUtilities mock = new MockUtilities();
    when(mock.getElementAdapter().getAttribute(attr)).thenReturn(attr);
    assertThat(mock.getUtamElement().getValue(), is(equalTo(attr)));
  }

  @Test
  public void testMoveTo() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().moveTo();
  }

  @Test
  public void testScrollToCenter() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().scrollToCenter();
  }

  @Test
  public void testScrollToTop() {
    MockUtilities mock = new MockUtilities();
    assertThrows(ElementNotVisibleException.class, () -> mock.getUtamElement().scrollToTop());
    when(mock.getElementAdapter().isDisplayed()).thenReturn(true);
    mock.getUtamElement().scrollToTop();
  }

  @Test
  public void testIsFocused() {
    MockUtilities mock = new MockUtilities();
    WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
    when(mock.getWebDriverMock().switchTo()).thenReturn(targetLocator);
    when(targetLocator.activeElement()).thenReturn(mock.getWebElementMock());
    assertThat(mock.getUtamElement().isFocused(), is(true));
  }

  @Test
  public void testFocus() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().focus();
  }

  @Test
  public void testBlur() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().blur();
  }

  @Test
  public void testContainsElement() {
    MockUtilities mock = new MockUtilities.MockAdapter();
    Locator locator = LocatorBy.byCss("css");
    assertThat(mock.getUtamElement().containsElement(locator, false), is(false));
    assertThat(mock.getUtamElement().containsElement(locator, true), is(false));
    when(mock.getElementAdapter().findElements(locator, NULLABLE))
        .thenReturn(Collections.singletonList(mock(
            Element.class)));
    assertThat(mock.getUtamElement().containsElement(locator), is(true));
    when(mock.getElementAdapter().findElements(locator, NULLABLE_IN_SHADOW))
        .thenReturn(Collections.singletonList(mock(
            Element.class)));
    assertThat(mock.getUtamElement().containsElement(locator, true), is(true));
  }

  @Test
  public void testClear() {
    MockUtilities mock = new MockUtilities.MockAdapter();
    mock.getUtamElement().clear();
    verify(mock.getElementAdapter(), times(1)).clear();
  }

  @Test
  public void testClearAndType() {
    String text = "text";
    MockUtilities mock = new MockUtilities.MockAdapter();
    mock.getUtamElement().clearAndType(text);
    verify(mock.getElementAdapter(), times(1)).clear();
    verify(mock.getElementAdapter(), times(1)).setText(text);
  }

  @Test
  public void testPress() {
    MockUtilities mock = new MockUtilities();
    final String KEY = "Enter";
    mock.getUtamElement().press(KEY);
    verify(mock.getWebElementMock(), times(1)).sendKeys(Keys.ENTER.toString());
  }

  @Test
  public void testClick() {
    MockUtilities mock = new MockUtilities();
    mock.getUtamElement().click();
    verify(mock.getWebElementMock(), times(1)).click();
  }

  @Test
  public void testJavascriptClick() {
    MockUtilities mock = new MockUtilities();
    when(mock.getExecutorMock().executeScript(any(), any())).thenReturn(true);
    mock.getUtamElement().javascriptClick();
    verify(mock.getExecutorMock(), times(1))
        .executeScript(any(), any());
  }

  @Test
  public void testFlick() {
    MockUtilities mock = new MockUtilities();
    assertThrows(() -> mock.getUtamElement().flick(0, 0));
  }

  @Test
  public void testFlickMobile() {
    MockUtilities mock = new MockUtilities.MockDriver(AppiumDriver.class);
    when(mock.getMobileDriverAdapter().isNative()).thenReturn(false);
    mock.getUtamElement().flick(1, 1);
  }

  @Test
  public void testFlickItems() {
    MockUtilities mock = new MockUtilities();
    assertThrows(() -> mock.getUtamElement().flickItems(GestureDirection.DOWN));
  }

  @Test
  public void testFlickItemsMobile() {
    MockUtilities mock = new MockUtilities.MockAdapter(AppiumDriver.class);
    assertThat(mock.getUtamElement().flickItems(GestureDirection.DOWN), is(false));
    verify(mock.getElementAdapter(), times(1)).flickItems(GestureDirection.DOWN);
  }
}
