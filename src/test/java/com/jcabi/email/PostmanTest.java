/*
 * This file is part of Tprot.
 *
 * Tprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tprot.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jcabi.email;

import com.google.common.base.Joiner;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PostmanTest {
    /**
     * Test for issue #8.
     */
    @Test
    public void utf8Handling() throws MessagingException, IOException {
        final Transport transport = Mockito.mock(Transport.class);
        final Wire wire = Mockito.mock(Wire.class);
        Mockito.when(wire.connect()).thenReturn(transport);
        final Postman.Default postman = new Postman.Default(wire);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invoc) throws Throwable {
                final MimeMessage msg = (MimeMessage) invoc.getArguments()[0];
                final ByteArrayOutputStream stream =
                    new ByteArrayOutputStream();
                msg.writeTo(stream);
                final String msgtxt = stream.toString();
                Assert.assertTrue(msgtxt.contains(
                    "Content-Type: text/html;charset=\"utf-8\""));
                Assert.assertTrue(msgtxt.contains(
                    "Content-Transfer-Encoding: quoted-printable"));
                Assert.assertTrue(msgtxt.contains(
                    Joiner.on("").join("<html><body>",
                        "=D0=BF=D1=80=D0=B8=D0=B2=D0=B5=D1=82</body></html>")));
                return null;
            }
        }).when(transport).sendMessage(
            Mockito.isA(Message.class),
            Mockito.any(Address[].class)
        );
        final Envelope.MIME env = new Envelope.MIME()
                .with(new StSender("from <test-from@jcabi.com>"))
                .with(new StRecipient("to", "test-to@jcabi.com"))
                .with(new StSubject("test subject: test me"))
                .with(new EnHTML("<html><body>привет</body></html>"));
        postman.send(env);
    }
}
