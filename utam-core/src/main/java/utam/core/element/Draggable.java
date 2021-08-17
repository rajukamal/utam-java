/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.core.element;

/**
 * interaction methods for an element that can be "dragged"
 *
 * @author elizaveta.ivanova
 * @since 236
 */
public interface Draggable extends BasicElement {

  /**
   * drag and drop an element to the target location
   *
   * @param target location where to drop and
   */
  void dragAndDrop(BasicElement target, int holdDurationSec);

  void dragAndDrop(BasicElement target);

  /**
   * drag and drop an element by offset
   *
   * @param xOffset horizontal offset
   * @param yOffset vertical offset
   */
  void dragAndDrop(int xOffset, int yOffset, int holdDurationSec);

  void dragAndDrop(int xOffset, int yOffset);
}
