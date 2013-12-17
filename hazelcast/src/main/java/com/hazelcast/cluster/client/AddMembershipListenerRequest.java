/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.cluster.client;

import com.hazelcast.client.CallableClientRequest;
import com.hazelcast.client.ClientEndpoint;
import com.hazelcast.client.ClientEngine;
import com.hazelcast.client.ClientPortableHook;
import com.hazelcast.cluster.ClusterServiceImpl;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.SerializationService;
import com.hazelcast.spi.impl.SerializableCollection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author mdogan 5/13/13
 */
public final class AddMembershipListenerRequest extends CallableClientRequest implements Portable {

    @Override
    public Object call() throws Exception {
        final ClusterServiceImpl service = getService();
        final ClientEndpoint endpoint = getEndpoint();
        final ClientEngine clientEngine = getClientEngine();

        final String registration = service.addMembershipListener(new MembershipListener() {
            public void memberAdded(MembershipEvent membershipEvent) {
                if (endpoint.live()) {
                    final MemberImpl member = (MemberImpl) membershipEvent.getMember();
                    clientEngine.sendResponse(endpoint, new ClientMembershipEvent(member, MembershipEvent.MEMBER_ADDED));
                }
            }

            public void memberRemoved(MembershipEvent membershipEvent) {
                if (endpoint.live()) {
                    final MemberImpl member = (MemberImpl) membershipEvent.getMember();
                    clientEngine.sendResponse(endpoint, new ClientMembershipEvent(member, MembershipEvent.MEMBER_REMOVED));
                }
            }
        });

        final String name = ClusterServiceImpl.SERVICE_NAME;
        endpoint.setListenerRegistration(name, name, registration);

        final Collection<MemberImpl> memberList = service.getMemberList();
        final Collection<Data> response = new ArrayList<Data>(memberList.size());
        final SerializationService serializationService = getClientEngine().getSerializationService();
        for (MemberImpl member : memberList) {
            response.add(serializationService.toData(member));
        }
        return new SerializableCollection(response);
    }

    public String getServiceName() {
        return ClusterServiceImpl.SERVICE_NAME;
    }

    public int getFactoryId() {
        return ClientPortableHook.ID;
    }

    public int getClassId() {
        return ClientPortableHook.MEMBERSHIP_LISTENER;
    }

}
