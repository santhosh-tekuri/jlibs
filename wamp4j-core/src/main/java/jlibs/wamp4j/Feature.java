/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.wamp4j;

import java.util.Arrays;
import java.util.EnumSet;

import static jlibs.wamp4j.Role.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public enum Feature{
    subscriber_blackwhite_listing(false, publisher, broker),
    publisher_exclusion(false, publisher, broker),
    publisher_identification(false, publisher, broker, subscriber),
    publication_trustlevels(false, subscriber, broker),
    pattern_based_subscription(false, subscriber, broker),
    partitioned_pubsub(false, publisher, subscriber, broker),
    subscriber_metaevents(false, subscriber, broker),
    subscriber_list(false, subscriber, broker),
    event_history(false, subscriber, broker),
    progressive_call_results(false, caller, callee, dealer),
    call_canceling(false, caller, callee, dealer),
    call_timeout(false, caller, callee, dealer),
    callee_blackwhite_listing(false, caller, dealer),
    caller_exclusion(false, caller, dealer),
    caller_identification(false, caller, callee, dealer),
    call_trustlevels(false, callee, dealer),
    pattern_based_registration(false, callee, dealer),
    partitioned_rpc(false, callee, callee),
    ;

    public final EnumSet<Role> roles;
    public final boolean implemented;

    Feature(boolean implemented, Role... roles){
        this.implemented = implemented;
        this.roles = EnumSet.copyOf(Arrays.asList(roles));
    }
}
