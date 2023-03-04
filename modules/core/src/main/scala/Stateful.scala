// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

/** Represents a stateful projection. <b>Beware</b> that this will force you to use <i>state</i> as the name of the
  * query function that will leave in the server.
  *
  * @tparam S
  *   the type of the state
  *
  * @example
  *
  * ==Definition==
  *
  * {{{
  *   class Counter extends Stateful[Int] {
  *     var state: Int = 0
  *     def getState: Int = state
  *     def setState(state: Int): Unit = this.state = state
  *   }
  * }}}
  * ==Query==
  * <b>The javascript function that will be used as a query </b>
  *
  * <br>
  * {{{
  * fromStream('ShoppingCart').
  *    when({
  *      "$init": function() {
  *        return {
  *          state: 0
  *        }
  *      },
  *      "$any": function(s, e) {
  *        s.state = s.state + 1;
  *      }
  *  }).outputState();
  * }}}
  * </br>
  */
trait Stateful[S] {

  /** Represents the state of the projection. Should be initialized to an empty value of the type `S`. */
  var state: S

  /** Returns the current state of the projection. */
  def getState: S

  /** Sets the current state of the projection. This method is called by the projection manager to build the state of
    * the projection.
    *
    * @param state
    *   the new state of the projection
    */
  def setState(state: S): Unit
}
