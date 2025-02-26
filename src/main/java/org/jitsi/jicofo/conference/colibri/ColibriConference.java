/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015-Present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.jicofo.conference.colibri;

import org.jetbrains.annotations.*;
import org.jitsi.jicofo.conference.source.*;
import org.jitsi.utils.*;
import org.jitsi.xmpp.extensions.colibri.*;
import org.jitsi.xmpp.extensions.jingle.*;

import org.jxmpp.jid.*;

import java.util.*;

/**
 * This is Colibri conference allocated on the videobridge. It exposes
 * operations like allocating/expiring channels, updating channel transport
 * and so on.
 *
 * @author Pawel Domas
 */
public interface ColibriConference
{
    /**
     * Sets Jitsi videobridge XMPP address to be used to allocate
     * the conferences.
     *
     * @param videobridgeJid the videobridge address to be set.
     */
    void setJitsiVideobridge(Jid videobridgeJid);

    void setMeetingId(@NotNull String meetingId);

    /**
     * Returns the identifier assigned for our conference by the videobridge.
     * Will returns <tt>null</tt> if no conference has been allocated yet for
     * this instance.
     */
    String getConferenceId();

    /**
     * Sets world readable name that identifies the conference.
     * @param name the new name.
     */
    void setName(EntityBareJid name);

    /**
     * Gets world readable name that identifies the conference.
     * @return the name.
     */
    EntityBareJid getName();

    /**
     * Creates channels on the videobridge for given parameters.
     *
     * @param endpointId the ID of the Colibri endpoint.
     * @param statsId the statistics Id to use if any.
     * @param peerIsInitiator <tt>true</tt> if peer is ICE an initiator
     * of ICE session.
     * @param contents content list that describes peer media.
     * @return <tt>ColibriConferenceIQ</tt> that describes allocated channels.
     */
    default ColibriConferenceIQ createColibriChannels(
        String endpointId,
        String statsId,
        boolean peerIsInitiator,
        List<ContentPacketExtension> contents)
        throws ColibriException
    {
        return createColibriChannels(
            endpointId,
            statsId,
            peerIsInitiator,
            contents,
            new ConferenceSourceMap() /* sources */,
            null /* relays */);
    }

    /**
     * Creates channels on the videobridge for given parameters.
     *
     * @param endpointId the ID of the Colibri endpoint.
     * @param statsId the statistics Id to use if any.
     * @param peerIsInitiator <tt>true</tt> if peer is ICE an initiator
     * of ICE session.
     * @param contents content list that describes peer media.
     * @param sources the sources to include with the channel creation request,
     * if any.
     * @param relays the Octo relay IDs to include in the channel creation
     * request, if any.
     * @return <tt>ColibriConferenceIQ</tt> that describes allocated channels.
     *
     * @throws ColibriException if channel allocation fails.
     */
    ColibriConferenceIQ createColibriChannels(
            String endpointId,
            String statsId,
            boolean peerIsInitiator,
            List<ContentPacketExtension> contents,
            @NotNull ConferenceSourceMap sources,
            List<String> relays)
        throws ColibriException;

    /**
     * Does Colibri channels update of RTP description, SSRC and transport
     * information. This is a combined request and what it will contain depends
     * which parameters are provided. Most of them is optional here. Request
     * will be sent only if any data has been provided. Sends a colibri request,
     * but does not wait for a response.
     *
     * @param localChannelsInfo (mandatory) <tt>ColibriConferenceIQ</tt> that
     * contains the description of the channels for which update request will be
     * sent to the bridge.
     * @param rtpInfoMap (optional) the map of Colibri content name to
     * <tt>RtpDescriptionPacketExtension</tt> which will be used to update
     * the RTP description of the channel in corresponding content described by
     * <tt>localChannelsInfo</tt>.
     * @param sources the set of sources to be included in the Colibri channel update.
     */
    default void updateChannelsInfo(
        ColibriConferenceIQ localChannelsInfo,
        Map<String, RtpDescriptionPacketExtension> rtpInfoMap,
        @NotNull ConferenceSourceMap sources)
    {
        updateChannelsInfo(
            localChannelsInfo,
            rtpInfoMap,
            sources,
            null, null, null);
    }

    /**
     * Does Colibri channels update of RTP description, SSRC and transport
     * information. This is a combined request and what it will contain depends
     * which parameters are provided. Most of them is optional here. Request
     * will be sent only if any data has been provided. Sends a colibri request,
     * but does not wait for a response.
     *
     * @param localChannelsInfo (mandatory) <tt>ColibriConferenceIQ</tt> that
     * contains the description of the channels for which update request will be
     * sent to the bridge.
     * @param rtpInfoMap (optional) the map of Colibri content name to
     * <tt>RtpDescriptionPacketExtension</tt> which will be used to update
     * the RTP description of the channel in corresponding content described by
     * <tt>localChannelsInfo</tt>.
     * @param sources the sett of sources to include in the Colibri channel update request.
     * @param bundleTransport (mandatory) the
     * <tt>IceUdpTransportPacketExtension</tt> which will be used to set
     * "bundle" transport of the first channel bundle from
     * <tt>localChannelsInfo</tt>.
     * @param endpointId the ID of the endpoint for which the update applies
     * (it is implicit that the update only works for channels of a single
     * @param relays the Octo relay IDs to set.
     * participant/endpoint).
     */
    void updateChannelsInfo(
            ColibriConferenceIQ localChannelsInfo,
            Map<String, RtpDescriptionPacketExtension> rtpInfoMap,
            @NotNull ConferenceSourceMap sources,
            IceUdpTransportPacketExtension bundleTransport,
            String endpointId,
            List<String> relays);

    /**
     * Updates simulcast layers on the bridge.
     * @param localChannelsInfo <tt>ColibriConferenceIQ</tt> that contains
     * the description of the channel for which SSRC groups information will be
     * updated on the bridge.
     */
    void updateSourcesInfo(ConferenceSourceMap sources, ColibriConferenceIQ localChannelsInfo);

    /**
     * Updates the transport of a specific channel bundle.
     *
     * @param transport the transport packet extension that contains channel
     * bundle transport candidates.
     * @param channelBundleId the ID of the channel bundle for which to update
     * the transport.
     */
    void updateBundleTransportInfo(
            IceUdpTransportPacketExtension transport,
            String channelBundleId);

    /**
     * Expires the channels described by given <tt>ColibriConferenceIQ</tt>.
     *
     * @param channelInfo the <tt>ColibriConferenceIQ</tt> that contains
     * information about the channel to be expired.
     */
    void expireChannels(ColibriConferenceIQ channelInfo);

    /**
     * Expires all channels in current conference and this instance goes into
     * disposed state(like calling {@link #dispose()} method). It must not be
     * used anymore.
     */
    void expireConference();

    /**
     * Mutes audio  or video channels described in given IQ by changing their media
     * direction to {@code sendonly}.
     * @param channelsInfo the IQ that describes the channels to be muted.
     * @param mute <tt>true</tt> to mute or <tt>false</tt> to unmute channels 
     * described in <tt>channelsInfo</tt>.
     * @param mediaType optional mediaType of the channel to mute; defaults to audio.
     * @return <tt>true</tt> if the operation has succeeded or <tt>false</tt>
     * otherwise.
     */
    boolean muteParticipant(ColibriConferenceIQ channelsInfo, boolean mute, MediaType mediaType);
    
    /**
     * Disposes of any resources allocated by this instance. Once disposed this
     * instance must not be used anymore.
     */
    void dispose();

    /**
     * Sets the "global" id of the conference.
     */
    void setGID(String gid) ;
}
