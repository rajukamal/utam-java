package selenium.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

/**
 * Tests for AppiumCapabilityProvider
 * @author qren
 * @since 230
 */
public class AppiumCapabilityProviderTests { 
  /**
   * The setDesiredCapability method should set the target Appium capability to a string value
   */
  @Test
  public void testSetDesiredCapabilityString() {
    AppiumCapabilityProvider appiumCapabilityProvider = new AppiumCapabilityProvider();
    String capabilityName = "fakeCapabilityName";
    String capabilityValue = "fakeValue";
    appiumCapabilityProvider.setDesiredCapability(capabilityName, capabilityValue);
    DesiredCapabilities desiredCapabilities = appiumCapabilityProvider.getDesiredCapabilities();
    assertThat(desiredCapabilities.getCapability(capabilityName), is(equalTo(capabilityValue)));
  }

  /**
   * The setDesiredCapability method should set the target Appium capability to a boolean value
   */
  @Test
  public void testSetDesiredCapabilityBoolean() {
    AppiumCapabilityProvider appiumCapabilityProvider = new AppiumCapabilityProvider();
    String capabilityName = "fakeCapabilityName";
    Boolean capabilityValue = true;
    appiumCapabilityProvider.setDesiredCapability(capabilityName, capabilityValue);
    DesiredCapabilities desiredCapabilities = appiumCapabilityProvider.getDesiredCapabilities();
    assertThat(desiredCapabilities.getCapability(capabilityName), is(equalTo(capabilityValue)));
  }

  /**
   * The setDesiredCapability method should set the target Appium capability to a integer value
   */
  @Test
  public void testSetDesiredCapabilityInteger() {
    AppiumCapabilityProvider appiumCapabilityProvider = new AppiumCapabilityProvider();
    String capabilityName = "fakeCapabilityName";
    int capabilityValue = 10;
    appiumCapabilityProvider.setDesiredCapability(capabilityName, capabilityValue);
    DesiredCapabilities desiredCapabilities = appiumCapabilityProvider.getDesiredCapabilities();
    assertThat(desiredCapabilities.getCapability(capabilityName), is(equalTo(capabilityValue)));
  }
}