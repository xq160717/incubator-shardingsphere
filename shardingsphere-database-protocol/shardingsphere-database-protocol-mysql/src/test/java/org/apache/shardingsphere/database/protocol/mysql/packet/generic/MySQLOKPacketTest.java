/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.database.protocol.mysql.packet.generic;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLOKPacketTest {
    
    @Mock
    private MySQLPacketPayload packetPayload;
    
    @Test
    public void assertNewOKPacketWithSequenceId() {
        MySQLOKPacket actual = new MySQLOKPacket(1);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(0L));
        assertThat(actual.getLastInsertId(), is(0L));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    public void assertNewOKPacketWithAffectedRowsAndLastInsertId() {
        MySQLOKPacket actual = new MySQLOKPacket(1, 100L, 9999L);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(100L));
        assertThat(actual.getLastInsertId(), is(9999L));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    public void assertWrite() {
        new MySQLOKPacket(1, 100L, 9999L).write(packetPayload);
        verify(packetPayload).writeInt1(MySQLOKPacket.HEADER);
        verify(packetPayload).writeIntLenenc(100L);
        verify(packetPayload).writeIntLenenc(9999L);
        verify(packetPayload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(packetPayload).writeInt2(0);
        verify(packetPayload).writeStringEOF("");
    }
}
